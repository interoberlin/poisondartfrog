package de.interoberlin.poisondartfrog.view.adapters;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.config.ECharacteristic;
import de.interoberlin.poisondartfrog.model.config.EDevice;
import de.interoberlin.poisondartfrog.model.config.EService;
import de.interoberlin.poisondartfrog.model.tasks.EHttpParameter;
import de.interoberlin.poisondartfrog.model.tasks.HttpGetTask;
import de.interoberlin.poisondartfrog.view.activities.DevicesActivity;
import de.interoberlin.poisondartfrog.view.components.AccelerometerGyroscopeComponent;
import de.interoberlin.poisondartfrog.view.components.DataComponent;
import de.interoberlin.poisondartfrog.view.components.LightProximityComponent;
import de.interoberlin.poisondartfrog.view.components.LineChartComponent;
import de.interoberlin.poisondartfrog.view.components.MicrophoneComponent;
import de.interoberlin.poisondartfrog.view.components.SentientLightComponent;
import de.interoberlin.poisondartfrog.view.diagrams.BatteryDiagram;
import de.interoberlin.poisondartfrog.view.dialogs.CharacteristicsDialog;
import de.interoberlin.poisondartfrog.view.dialogs.ScanResultsDialog;

public class DevicesAdapter extends ArrayAdapter<BleDevice> {
    public static final String TAG = DevicesAdapter.class.getSimpleName();

    // Context
    private final Context context;
    private final Activity activity;
    private OnCompleteListener ocListener;

    // Controllers
    private DevicesController devicesController;

    private SharedPreferences prefs;
    private Resources res;

    // Filter
    private List<BleDevice> filteredItems = new ArrayList<>();
    private List<BleDevice> originalItems = new ArrayList<>();
    private BluetoothDeviceReadingFilter bluetoothDeviceReadingFilter;

    // Timers
    private Timer timer;

    // Tasks
    private HttpGetTask httpGetTask;

    // Properties
    private static int VIBRATION_DURATION;
    private static int GOLEM_SEND_PERIOD;

    private final Object lock = new Object();

    // --------------------
    // Constructors
    // --------------------

    public DevicesAdapter(Context context, Activity activity, int resource, List<BleDevice> items) {
        super(context, resource, items);
        devicesController = DevicesController.getInstance();

        this.prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        this.res = activity.getResources();

        this.filteredItems = items;
        this.originalItems = items;

        this.context = context;
        this.activity = activity;
        this.ocListener = (OnCompleteListener) activity;

        this.timer = new Timer();

        VIBRATION_DURATION = activity.getResources().getInteger(R.integer.vibration_duration);
        GOLEM_SEND_PERIOD = Integer.parseInt(prefs.getString(res.getString(R.string.pref_golem_temperature_send_period), "5"));
        if (GOLEM_SEND_PERIOD < 1) GOLEM_SEND_PERIOD = 5;

        filter();
    }

    // --------------------
    // Methods
    // --------------------

    @Override
    public int getCount() {
        return filteredItems != null ? filteredItems.size() : 0;
    }

    @Override
    public BleDevice getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final BleDevice device = getItem(position);

        // Layout inflater
        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());

        // Load views
        final LinearLayout llCardDevice = (LinearLayout) vi.inflate(R.layout.card_device, parent, false);
        final TextView tvName = (TextView) llCardDevice.findViewById(R.id.tvName);
        final TextView tvAddress = (TextView) llCardDevice.findViewById(R.id.tvAddress);
        final ImageView ivIcon = (ImageView) llCardDevice.findViewById(R.id.ivIcon);
        final LinearLayout llComponents = (LinearLayout) llCardDevice.findViewById(R.id.llComponents);
        final ImageView ivConnected = (ImageView) llCardDevice.findViewById(R.id.ivConnected);

        final LinearLayout llBatteryLevel = (LinearLayout) llCardDevice.findViewById(R.id.llBatteryLevel);
        final TextView tvBatteryLevelValue = (TextView) llCardDevice.findViewById(R.id.tvBatteryLevelValue);
        final BatteryDiagram bdBattery = (BatteryDiagram) llCardDevice.findViewById(R.id.bdBattery);

        final ImageView ivDetach = (ImageView) llCardDevice.findViewById(R.id.ivDetach);
        final ImageView ivSubscribe = (ImageView) llCardDevice.findViewById(R.id.ivSubscribeData);
        final ImageView ivLedState = (ImageView) llCardDevice.findViewById(R.id.ivLedState);
        final ImageView ivSendTemperature = (ImageView) llCardDevice.findViewById(R.id.ivSendTemperature);
        final ImageView ivMore = (ImageView) llCardDevice.findViewById(R.id.ivMore);

        // Set values
        tvName.setText(device.getName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvName.setTextAppearance(android.R.style.TextAppearance_Material_Title);
        }

        tvAddress.setText(device.getAddress());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvAddress.setTextAppearance(android.R.style.TextAppearance_Material_Subhead);
        }

        if (device.getName() == null || device.getName().isEmpty())
            tvName.setText(R.string.unknown_device);

        llComponents.removeAllViews();

        if (EDevice.fromString(device.getName()) != null) {
            switch (EDevice.fromString(device.getName())) {
                case WUNDERBAR_HTU: {
                    ivIcon.setImageResource(R.drawable.ic_invert_colors_black_48dp);
                    break;
                }
                case WUNDERBAR_GYRO: {
                    ivIcon.setImageResource(R.drawable.ic_vibration_black_48dp);
                    break;
                }
                case WUNDERBAR_LIGHT: {
                    ivIcon.setImageResource(R.drawable.ic_lightbulb_outline_black_48dp);
                    break;
                }
                case WUNDERBAR_MIC: {
                    ivIcon.setImageResource(R.drawable.ic_mic_black_48dp);
                    break;
                }
                case NRFDUINO: {
                    ivIcon.setImageResource(R.drawable.ic_panorama_fish_eye_black_48dp);
                    break;
                }
                default: {
                    ivIcon.setImageResource(R.drawable.ic_bluetooth_connected_black_48dp);
                    break;
                }
            }
        } else {
            ivIcon.setImageResource(R.drawable.ic_bluetooth_connected_black_48dp);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ivConnected.getDrawable().setTint(ContextCompat.getColor(context, device.isConnected() ? R.color.colorAccent : R.color.md_grey_400));
        } else {
            if (!device.isConnected())
                ((ViewManager) ivConnected.getParent()).removeView(ivConnected);
        }

        // Battery level
        if (device.containsCharacteristic(ECharacteristic.BATTERY_LEVEL)) {
            String value = device.getCharacteristic(ECharacteristic.BATTERY_LEVEL).getStringValue(0);

            if (value != null) {
                tvBatteryLevelValue.setText(value + "%");
                bdBattery.setValue(Integer.parseInt(value));
            }
            llBatteryLevel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vibrate(VIBRATION_DURATION);
                    device.read(ECharacteristic.BATTERY_LEVEL);
                }
            });
        } else {
            ((ViewManager) llBatteryLevel.getParent()).removeView(llBatteryLevel);
        }

        ivSubscribe.setImageDrawable(device.isSubscribing() ? ContextCompat.getDrawable(activity, R.drawable.ic_pause_black_36dp) : ContextCompat.getDrawable(activity, R.drawable.ic_play_arrow_black_36dp));

        llComponents.addView(new DataComponent(context, activity, device));

        switch (EDevice.fromString(device.getName())) {
            case WUNDERBAR_LIGHT: {
                llComponents.addView(new LightProximityComponent(context, activity, device));
                break;
            }
            case WUNDERBAR_GYRO: {
                llComponents.addView(new AccelerometerGyroscopeComponent(context, activity, device));
                break;
            }
            case WUNDERBAR_MIC: {
                llComponents.addView(new MicrophoneComponent(context, activity, device));
                llComponents.addView(new LineChartComponent(context, activity, device));
                break;
            }
            case INTEROBERLIN_SENTIENT_LIGHT: {
                llComponents.addView(new SentientLightComponent(context, activity, device));
                break;
            }
            default: {
                llComponents.addView(new LineChartComponent(context, activity, device));
                break;
            }
        }

        // Add actions
        ivDetach.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (timer != null) timer.cancel();
                                            if (httpGetTask != null) httpGetTask.cancel(true);

                                            ocListener.onDetachDevice(device);
                                        }
                                    }
        );

        // DATA
        if (device.containsCharacteristic(ECharacteristic.DATA)) {
            ivSubscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vibrate(VIBRATION_DURATION);
                    if (activity instanceof DevicesActivity) {
                        DevicesActivity devicesActivity = ((DevicesActivity) activity);

                        if (!device.isSubscribing()) {
                            device.subscribe(ECharacteristic.DATA);
                            devicesActivity.updateView();
                            devicesActivity.snack(R.string.started_subscription);
                        } else {
                            device.unsubscribe(ECharacteristic.DATA);
                            device.disconnect();

                            devicesActivity.updateView();
                            devicesActivity.snack(R.string.stopped_subscription);

                            if (timer != null) timer.cancel();
                            if (httpGetTask != null) httpGetTask.cancel(true);
                        }
                    }
                }
            });
        } else {
            ((ViewManager) ivSubscribe.getParent()).removeView(ivSubscribe);
        }

        // LED state
        if (device.containsCharacteristic(ECharacteristic.LED_STATE)) {
            ivLedState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vibrate(VIBRATION_DURATION);
                    device.write(EService.DIRECT_CONNECTION, ECharacteristic.LED_STATE, true);
                }
            });
        } else {
            ((ViewManager) ivLedState.getParent()).removeView(ivLedState);
        }

        // Send temperature
        if ((EDevice.fromString(device.getName()) != null) && EDevice.fromString(device.getName()).equals(EDevice.WUNDERBAR_HTU)
                && device.getLatestReadings() != null && device.getLatestReadings().containsKey("temperature")) {
            ivSendTemperature.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vibrate(VIBRATION_DURATION);

                    timer = new Timer();
                    timer.purge();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        synchronized public void run() {
                            Location location = null;
                            if (activity instanceof DevicesActivity) {
                                DevicesActivity devicesActivity = ((DevicesActivity) activity);
                                devicesActivity.snack("Started timer");

                                // Update location
                                devicesActivity.getSingleLocation();
                                location = devicesActivity.getCurrentLocation();
                            }

                            if (activity instanceof HttpGetTask.OnCompleteListener) {
                                HttpGetTask.OnCompleteListener listener = (HttpGetTask.OnCompleteListener) activity;
                                String url = prefs.getString(res.getString(R.string.pref_golem_temperature_url), null);

                                if (device.getLatestReadings() != null && device.getLatestReadings().containsKey("temperature")) {
                                    String temperature = device.getLatestReadings().get("temperature").value.toString();

                                    // Add parameters
                                    Map<EHttpParameter, String> parameters = new LinkedHashMap<>();

                                    parameters.put(EHttpParameter.DBG, String.valueOf(prefs.getBoolean(res.getString(R.string.pref_golem_temperature_dbg), true) ? "1" : "0"));
                                    parameters.put(EHttpParameter.TOKEN, prefs.getString(res.getString(R.string.pref_golem_temperature_token), null));
                                    parameters.put(EHttpParameter.TYPE, prefs.getString(res.getString(R.string.pref_golem_temperature_type), res.getString(R.string.pref_default_golem_temperature_type)));
                                    parameters.put(EHttpParameter.COUNTRY, prefs.getString(res.getString(R.string.pref_golem_temperature_country), null));
                                    parameters.put(EHttpParameter.CITY, prefs.getString(res.getString(R.string.pref_golem_temperature_city), null));
                                    parameters.put(EHttpParameter.ZIP, prefs.getString(res.getString(R.string.pref_golem_temperature_zip), null));
                                    parameters.put(EHttpParameter.TEMP, temperature);

                                    // Add location parameters
                                    if (location != null) {
                                        parameters.put(EHttpParameter.LAT, String.valueOf(round(location.getLatitude(), 2)));
                                        parameters.put(EHttpParameter.LONG, String.valueOf(round(location.getLongitude(), 2)));
                                    }

                                    // Call webservice
                                    try {
                                        httpGetTask = new HttpGetTask(activity, listener, url);
                                        httpGetTask.execute(parameters).get();
                                    } catch (InterruptedException | ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                    }, TimeUnit.MINUTES.toMillis(0), TimeUnit.MINUTES.toMillis(GOLEM_SEND_PERIOD));
                }
            });
        } else {
            ((ViewManager) ivSendTemperature.getParent()).removeView(ivSendTemperature);
        }

        // Characteristics
        if (device != null && device.getCharacteristics() != null) {
            ivMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vibrate(VIBRATION_DURATION);
                    CharacteristicsDialog dialog = new CharacteristicsDialog();
                    Bundle b = new Bundle();
                    b.putCharSequence(activity.getResources().getString(R.string.bundle_dialog_title), activity.getResources().getString(R.string.characteristics));
                    b.putCharSequence(activity.getResources().getString(R.string.bundle_device_address), device.getAddress());
                    dialog.setArguments(b);
                    dialog.show(activity.getFragmentManager(), ScanResultsDialog.TAG);
                }
            });
        } else {
            ((ViewManager) ivMore.getParent()).removeView(ivMore);
        }


        return llCardDevice;
    }

    private void vibrate() {
        vibrate(Notification.DEFAULT_VIBRATE);
    }

    private void vibrate(int VIBRATION_DURATION) {
        ((Vibrator) activity.getSystemService(Activity.VIBRATOR_SERVICE)).vibrate(VIBRATION_DURATION);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // --------------------
    // Methods - Filter
    // --------------------

    /*
    public List<BleDevice> getFilteredItems() {
        return filteredItems;
    }
    */

    public void filter() {
        getFilter().filter("");
    }

    @Override
    public Filter getFilter() {
        if (bluetoothDeviceReadingFilter == null) {
            bluetoothDeviceReadingFilter = new BluetoothDeviceReadingFilter();
        }
        return bluetoothDeviceReadingFilter;
    }

    /**
     * Determines if a bluetooth device reading shall be displayed
     *
     * @param reading reading
     * @return true if item is visible
     */
    protected boolean filterBluetoothDeviceReading(BleDevice reading) {
        return reading != null;
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onDetachDevice(BleDevice device);
    }

    // --------------------
    // Inner classes
    // --------------------

    public class BluetoothDeviceReadingFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            // Copy items
            originalItems = devicesController.getAttachedDevicesAsList();

            ArrayList<BleDevice> values;
            synchronized (lock) {
                values = new ArrayList<>(originalItems);
            }

            final int count = values.size();
            final ArrayList<BleDevice> newValues = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final BleDevice value = values.get(i);
                if (filterBluetoothDeviceReading(value)) {
                    newValues.add(value);
                }
            }

            results.values = newValues;
            results.count = newValues.size();

            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (List<BleDevice>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}