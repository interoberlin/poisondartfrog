package de.interoberlin.poisondartfrog.view.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.config.ECharacteristic;
import de.interoberlin.poisondartfrog.model.config.EDevice;
import de.interoberlin.poisondartfrog.model.config.EService;
import de.interoberlin.poisondartfrog.view.components.AccelerometerGyroscopeComponent;
import de.interoberlin.poisondartfrog.view.components.DataComponent;
import de.interoberlin.poisondartfrog.view.components.LightProximityComponent;
import de.interoberlin.poisondartfrog.view.components.LineChartComponent;
import de.interoberlin.poisondartfrog.view.components.MicrophoneComponent;
import de.interoberlin.poisondartfrog.view.components.SentientLightComponent;
import de.interoberlin.poisondartfrog.view.diagrams.BatteryDiagram;

public class DevicesAdapter extends ArrayAdapter<BleDevice> {
    public static final String TAG = DevicesAdapter.class.getSimpleName();

    // Context
    private final Context context;
    private OnCompleteListener ocListener;

    // View
    static class ViewHolder {
        private TextView tvName;
        private TextView tvAddress;
        private ImageView ivIcon;
        private LinearLayout llComponents;
        private ImageView ivConnected;

        private LinearLayout llBatteryLevel;
        private TextView tvBatteryLevelValue;
        private BatteryDiagram bdBattery;

        private ImageView ivDetach;
        private ImageView ivSubscribe;
        private ImageView ivLedState;
        private ImageView ivSendTemperature;
        private ImageView ivAutoConnect;
        private ImageView ivMore;
    }

    // Controllers
    private DevicesController devicesController;

    private Resources res;

    // Filter
    private List<BleDevice> filteredItems = new ArrayList<>();
    private List<BleDevice> originalItems = new ArrayList<>();
    private BluetoothDeviceReadingFilter bluetoothDeviceReadingFilter;

    // Timers
    private Timer timer;

    private final Object lock = new Object();

    // --------------------
    // Constructors
    // --------------------

    public DevicesAdapter(Context context, OnCompleteListener ocListener, int resource, List<BleDevice> items) {
        super(context, resource, items);
        devicesController = DevicesController.getInstance();

        this.res = context.getResources();

        this.filteredItems = items;
        this.originalItems = items;

        this.context = context;
        this.ocListener = ocListener;

        this.timer = new Timer();

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

        ViewHolder viewHolder;

        if (v == null) {
            viewHolder = new ViewHolder();

            // Layout inflater
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());

            // Load views
            v = vi.inflate(R.layout.card_device, parent, false);
            viewHolder.tvName = (TextView) v.findViewById(R.id.tvName);
            viewHolder.tvAddress = (TextView) v.findViewById(R.id.tvAddress);
            viewHolder.ivIcon = (ImageView) v.findViewById(R.id.ivIcon);
            viewHolder.llComponents = (LinearLayout) v.findViewById(R.id.llComponents);
            viewHolder.ivConnected = (ImageView) v.findViewById(R.id.ivConnected);

            viewHolder.llBatteryLevel = (LinearLayout) v.findViewById(R.id.llBatteryLevel);
            viewHolder.tvBatteryLevelValue = (TextView) v.findViewById(R.id.tvBatteryLevelValue);
            viewHolder.bdBattery = (BatteryDiagram) v.findViewById(R.id.bdBattery);

            viewHolder.ivDetach = (ImageView) v.findViewById(R.id.ivDetach);
            viewHolder.ivSubscribe = (ImageView) v.findViewById(R.id.ivSubscribeData);
            viewHolder.ivLedState = (ImageView) v.findViewById(R.id.ivLedState);
            viewHolder.ivSendTemperature = (ImageView) v.findViewById(R.id.ivSendTemperature);
            viewHolder.ivAutoConnect = (ImageView) v.findViewById(R.id.ivAutoConnect);
            viewHolder.ivMore = (ImageView) v.findViewById(R.id.ivMore);

            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        // Set values
        viewHolder.tvName.setText(device.getName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewHolder.tvName.setTextAppearance(android.R.style.TextAppearance_Material_Title);
        }

        viewHolder.tvAddress.setText(device.getAddress());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewHolder.tvAddress.setTextAppearance(android.R.style.TextAppearance_Material_Subhead);
        }

        if (device.getName() == null || device.getName().isEmpty())
            viewHolder.tvName.setText(R.string.unknown_device);

        viewHolder.llComponents.removeAllViews();

        if (EDevice.fromString(device.getName()) != null) {
            switch (EDevice.fromString(device.getName())) {
                case WUNDERBAR_HTU: {
                    viewHolder.ivIcon.setImageResource(R.drawable.ic_invert_colors_black_48dp);
                    break;
                }
                case WUNDERBAR_GYRO: {
                    viewHolder.ivIcon.setImageResource(R.drawable.ic_vibration_black_48dp);
                    break;
                }
                case WUNDERBAR_LIGHT: {
                    viewHolder.ivIcon.setImageResource(R.drawable.ic_lightbulb_outline_black_48dp);
                    break;
                }
                case WUNDERBAR_MIC: {
                    viewHolder.ivIcon.setImageResource(R.drawable.ic_mic_black_48dp);
                    break;
                }
                case NRFDUINO: {
                    viewHolder.ivIcon.setImageResource(R.drawable.ic_panorama_fish_eye_black_48dp);
                    break;
                }
                default: {
                    viewHolder.ivIcon.setImageResource(R.drawable.ic_bluetooth_connected_black_48dp);
                    break;
                }
            }
        } else {
            viewHolder.ivIcon.setImageResource(R.drawable.ic_bluetooth_connected_black_48dp);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            viewHolder.ivConnected.setVisibility(View.VISIBLE);
            viewHolder.ivConnected.getDrawable().setTint(ContextCompat.getColor(context, device.isConnected() ? R.color.colorAccent : R.color.md_grey_400));
        } else {
            if (!device.isConnected())
                viewHolder.ivConnected.setVisibility(View.GONE);
        }

        // Battery level
        if (device.containsCharacteristic(ECharacteristic.BATTERY_LEVEL)) {
            String value = device.getCharacteristic(ECharacteristic.BATTERY_LEVEL).getStringValue(0);

            if (value != null) {
                viewHolder.tvBatteryLevelValue.setText(String.format(res.getString(R.string.percentage), value));
                viewHolder.bdBattery.setValue(Integer.parseInt(value));
            }
            viewHolder.llBatteryLevel.setVisibility(View.VISIBLE);
            viewHolder.llBatteryLevel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ocListener.onRead();
                    device.read(ECharacteristic.BATTERY_LEVEL);
                }
            });
        } else {
            viewHolder.llBatteryLevel.setVisibility(View.GONE);
        }

        viewHolder.ivSubscribe.setImageDrawable(device.isSubscribing() ? ContextCompat.getDrawable(context, R.drawable.ic_pause_black_36dp) : ContextCompat.getDrawable(context, R.drawable.ic_play_arrow_black_36dp));

        if (!device.getReadings().isEmpty()) {
            viewHolder.llComponents.addView(new DataComponent(context, device));

            switch (EDevice.fromString(device.getName())) {
                case WUNDERBAR_LIGHT: {
                    viewHolder.llComponents.addView(new LightProximityComponent(context, device));
                    break;
                }
                case WUNDERBAR_GYRO: {
                    viewHolder.llComponents.addView(new AccelerometerGyroscopeComponent(context, device));
                    break;
                }
                case WUNDERBAR_MIC: {
                    viewHolder.llComponents.addView(new MicrophoneComponent(context, device));
                    viewHolder.llComponents.addView(new LineChartComponent(context, device));
                    break;
                }
                case INTEROBERLIN_SENTIENT_LIGHT: {
                    viewHolder.llComponents.addView(new SentientLightComponent(context, device));
                    break;
                }
                default: {
                    viewHolder.llComponents.addView(new LineChartComponent(context, device));
                    break;
                }
            }
        }

        // Add actions
        viewHolder.ivDetach.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       if (timer != null) timer.cancel();

                                                       ocListener.onDetachDevice(device);
                                                   }
                                               }
        );

        // Subscribe data
        if (device.containsCharacteristic(ECharacteristic.DATA)) {
            viewHolder.ivSubscribe.setVisibility(View.VISIBLE);
            viewHolder.ivSubscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ocListener.onSubscribe();
                    if (!device.isSubscribing()) {
                        device.subscribe(ECharacteristic.DATA);
                        ocListener.onChange(device, R.string.started_subscription);
                    } else {
                        device.unsubscribe(ECharacteristic.DATA);
                        device.disconnect();

                        ocListener.onChange(device, R.string.stopped_subscription);

                        if (timer != null) timer.cancel();
                    }
                }
            });
        } else {
            viewHolder.ivSubscribe.setVisibility(View.GONE);
        }

        // LED state
        if (device.containsCharacteristic(ECharacteristic.LED_STATE)) {
            viewHolder.ivLedState.setVisibility(View.VISIBLE);
            viewHolder.ivLedState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ocListener.onSend();
                    device.write(EService.DIRECT_CONNECTION, ECharacteristic.LED_STATE, true);
                }
            });
        } else {
            viewHolder.ivLedState.setVisibility(View.GONE);
        }

        // Send temperature
        if ((EDevice.fromString(device.getName()) != null) && EDevice.fromString(device.getName()).equals(EDevice.WUNDERBAR_HTU)
                && device.getLatestReadings() != null && device.getLatestReadings().containsKey("temperature")) {
            viewHolder.ivSendTemperature.setVisibility(View.VISIBLE);
            viewHolder.ivSendTemperature.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ocListener.onSendLocation(device);


                }
            });
        } else {
            viewHolder.ivSendTemperature.setVisibility(View.GONE);
        }

        // Auto connect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            viewHolder.ivAutoConnect.getDrawable().setTint(ContextCompat.getColor(context, device.isAutoConnectEnabled() ? R.color.colorAccent : R.color.md_grey_400));
        }
        viewHolder.ivAutoConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocListener.onToggleAutoConnect();
                devicesController.toggleAutoConnect(device);
            }
        });

        // Characteristics
        if (device.getCharacteristics() != null) {
            viewHolder.ivMore.setVisibility(View.VISIBLE);
            viewHolder.ivMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ocListener.onOpenCharacteristicsDialog(device);
                }
            });
        } else {
            viewHolder.ivMore.setVisibility(View.GONE);
        }

        return v;
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
        void onChange(BleDevice device, int text);

        void onDetachDevice(BleDevice device);

        void onSendLocation(BleDevice device);

        void onOpenCharacteristicsDialog(BleDevice device);

        void onRead();

        void onSend();

        void onSubscribe();

        void onToggleAutoConnect();
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