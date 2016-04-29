package de.interoberlin.poisondartfrog.view.adapters;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.Vibrator;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.config.ECharacteristic;
import de.interoberlin.poisondartfrog.model.config.EDevice;
import de.interoberlin.poisondartfrog.model.tasks.EHttpParameter;
import de.interoberlin.poisondartfrog.model.tasks.HttpGetTask;
import de.interoberlin.poisondartfrog.view.activities.DevicesActivity;
import de.interoberlin.poisondartfrog.view.components.DataComponent;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class DevicesAdapter extends ArrayAdapter<BleDevice> {
    public static final String TAG = DevicesAdapter.class.getSimpleName();

    // Context
    private final Context context;
    private final Activity activity;
    private OnCompleteListener ocListener;

    // Model
    private Subscription deviceSubscription = Subscriptions.empty();

    // Controllers
    DevicesController devicesController;

    // Filter
    private List<BleDevice> filteredItems = new ArrayList<>();
    private List<BleDevice> originalItems = new ArrayList<>();
    private BluetoothDeviceReadingFilter bluetoothDeviceReadingFilter;

    // Properties
    private static int VIBRATION_DURATION;

    private final Object lock = new Object();

    // --------------------
    // Constructors
    // --------------------

    public DevicesAdapter(Context context, Activity activity, int resource, List<BleDevice> items) {
        super(context, resource, items);
        devicesController = DevicesController.getInstance();

        this.filteredItems = items;
        this.originalItems = items;

        this.context = context;
        this.activity = activity;
        this.ocListener = (OnCompleteListener) activity;

        VIBRATION_DURATION = activity.getResources().getInteger(R.integer.vibration_duration);

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
        final ImageView ivDetach = (ImageView) llCardDevice.findViewById(R.id.ivDetach);
        final ImageView ivSubscribe = (ImageView) llCardDevice.findViewById(R.id.ivSubscribeData);
        final ImageView ivLedState = (ImageView) llCardDevice.findViewById(R.id.ivLedState);
        final ImageView ivSendTemperature = (ImageView) llCardDevice.findViewById(R.id.ivSendTemperature);

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
                default: {
                    ivIcon.setImageResource(R.drawable.ic_bluetooth_connected_black_48dp);
                    break;
                }
            }
        } else {
            ivIcon.setImageResource(R.drawable.ic_bluetooth_connected_black_48dp);
        }

        llComponents.addView(new DataComponent(context, device));
        // llComponents.addView(new ServicesComponent(context, device));

        ivSubscribe.setImageDrawable(device.isSubscribing() ? ContextCompat.getDrawable(activity, R.drawable.ic_pause_black_36dp) : ContextCompat.getDrawable(activity, R.drawable.ic_play_arrow_black_36dp));

        // Add actions
        ivDetach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocListener.onDetachDevice(device);
            }
        });

        // DATA
        if (device.containsCharacteristic(ECharacteristic.DATA)) {
            ivSubscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vibrate(VIBRATION_DURATION);
                    DevicesActivity devicesActivity = ((DevicesActivity) activity);

                    if (!device.isSubscribing()) {
                        device.setSubscribing(true);
                        deviceSubscription = device.subscribe(ECharacteristic.DATA.getId());
                        devicesActivity.updateListView();
                        devicesActivity.snack("Started subscription");
                    } else {
                        device.setSubscribing(false);
                        devicesActivity.updateListView();
                        devicesActivity.snack("Stopped subscription");
                        deviceSubscription.unsubscribe();
                    }
                }
            });
        } else {
            ((ViewManager) ivSubscribe.getParent()).removeView(ivSubscribe);
        }

        // LED STATE
        if (device.containsCharacteristic(ECharacteristic.LED_STATE)) {
            ivLedState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vibrate(VIBRATION_DURATION);
                    device.write(ECharacteristic.LED_STATE.getId(), true);
                }
            });
        } else {
            ((ViewManager) ivLedState.getParent()).removeView(ivLedState);
        }

        // Send temperature
        if ((EDevice.fromString(device.getName()) != null) && EDevice.fromString(device.getName()).equals(EDevice.WUNDERBAR_HTU) && device.getLatestReadings() != null && device.getLatestReadings().containsKey("temperature")) {
            ivSendTemperature.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vibrate(VIBRATION_DURATION);

                    if (activity instanceof HttpGetTask.OnCompleteListener) {
                        HttpGetTask.OnCompleteListener listener = (HttpGetTask.OnCompleteListener) activity;
                        String url = activity.getResources().getString(R.string.golem_temperature_url);

                        if (device.getLatestReadings() != null && device.getLatestReadings().containsKey("temperature")) {
                            String temperature = device.getLatestReadings().get("temperature").value.toString();

                            Map<EHttpParameter, String> values = new LinkedHashMap<>();
                            values.put(EHttpParameter.DBG, "0");
                            values.put(EHttpParameter.TOKEN, activity.getResources().getString(R.string.golem_temperature_token));
                            values.put(EHttpParameter.CITY, "Berlin");
                            values.put(EHttpParameter.COUNTRY, "DE");
                            values.put(EHttpParameter.TYPE, "other");
                            values.put(EHttpParameter.TEMP, temperature);

                            try {
                                new HttpGetTask(listener, url).execute(values).get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } else {
            ((ViewManager) ivSendTemperature.getParent()).removeView(ivSendTemperature);
        }

        return llCardDevice;
    }

    private void vibrate() {
        vibrate(Notification.DEFAULT_VIBRATE);
    }

    private void vibrate(int VIBRATION_DURATION) {
        ((Vibrator) activity.getSystemService(Activity.VIBRATOR_SERVICE)).vibrate(VIBRATION_DURATION);
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