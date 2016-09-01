package de.interoberlin.poisondartfrog.view.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import de.interoberlin.merlot_android.controller.DevicesController;
import de.interoberlin.merlot_android.controller.MappingController;
import de.interoberlin.merlot_android.model.IDisplayable;
import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.mapping.Mapping;
import de.interoberlin.merlot_android.model.repository.ECharacteristic;
import de.interoberlin.merlot_android.model.repository.EDevice;
import de.interoberlin.merlot_android.model.repository.EService;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.view.components.AccelerometerGyroscopeComponent;
import de.interoberlin.poisondartfrog.view.components.DataComponent;
import de.interoberlin.poisondartfrog.view.components.LightProximityComponent;
import de.interoberlin.poisondartfrog.view.components.LineChartComponent;
import de.interoberlin.poisondartfrog.view.components.MicrophoneComponent;
import de.interoberlin.poisondartfrog.view.components.SentientLightComponent;
import de.interoberlin.poisondartfrog.view.diagrams.BatteryDiagram;
import de.interoberlin.poisondartfrog.view.layouts.CollapsableLinearLayout;

public class DevicesAdapter extends ArrayAdapter<IDisplayable> {
    // <editor-fold defaultstate="extended" desc="Members">

    public static final String TAG = DevicesAdapter.class.getSimpleName();

    // Context
    private final Context context;
    private OnCompleteListener ocListener;

    // View
    static class ViewHolderDevice {
        @BindView(R.id.tvName) TextView tvName;
        @BindView(R.id.tvAddress) TextView tvAddress;
        @BindView(R.id.ivIcon) ImageView ivIcon;
        @BindView(R.id.cllComponents) CollapsableLinearLayout cllComponents;
        @BindView(R.id.ivConnected) ImageView ivConnected;

        @BindView(R.id.tvShowLess) TextView tvShowLess;

        @BindView(R.id.llBatteryLevel) LinearLayout llBatteryLevel;
        @BindView(R.id.tvBatteryLevelValue) TextView tvBatteryLevelValue;
        @BindView(R.id.bdBattery) BatteryDiagram bdBattery;

        @BindView(R.id.ivDetach) ImageView ivDetach;
        @BindView(R.id.ivSubscribeData) ImageView ivSubscribe;
        @BindView(R.id.ivLedState) ImageView ivLedState;
        @BindView(R.id.ivInteroberlinUartRx) ImageView ivInteroberlinUartRx;
        @BindView(R.id.ivInteroberlinUartTx) ImageView ivInteroberlinUartTx;
        @BindView(R.id.ivSentientLightFloorSensorRx) ImageView ivSentientLightFloorSensorRx;
        @BindView(R.id.ivSentientLightFloorSensorTx) ImageView ivSentientLightFloorSensorTx;
        @BindView(R.id.ivSentientLightLedRx) ImageView ivSentientLightLedRx;
        @BindView(R.id.ivSentientLightLedTx) ImageView ivSentientLightLedTx;
        @BindView(R.id.ivSendTemperature) ImageView ivSendTemperature;
        @BindView(R.id.ivAutoConnect) ImageView ivAutoConnect;
        @BindView(R.id.ivMore) ImageView ivMore;

        public ViewHolderDevice(View v) {
            ButterKnife.bind(this, v);
        }
    }

    static class ViewHolderMapping {
        @BindView(R.id.tvName) TextView tvName;
        @BindView(R.id.ivIcon) ImageView ivIcon;
        @BindView(R.id.ivTriggered) ImageView ivTriggered;
        @BindView(R.id.ivSource) ImageView ivSource;
        @BindView(R.id.tvSource) TextView tvSource;
        @BindView(R.id.ivSink) ImageView ivSink;
        @BindView(R.id.tvSink) TextView tvSink;

        public ViewHolderMapping(View v) {
            ButterKnife.bind(this, v);
        }
    }

    private boolean collapsed;

    // Controller
    private DevicesController devicesController;
    private MappingController mappingController;

    private Resources res;

    // Filter
    private List<IDisplayable> filteredItems = new ArrayList<>();
    private List<IDisplayable> originalItems = new ArrayList<>();
    private DevicesFilter devicesFilter;

    // Timers
    private Timer timer;

    private final Object lock = new Object();

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="extended" desc="Constructors">

    public DevicesAdapter(Context context, OnCompleteListener ocListener, int resource, List<IDisplayable> items) {
        super(context, resource, items);
        devicesController = DevicesController.getInstance();
        mappingController = MappingController.getInstance(getContext());

        this.res = context.getResources();

        this.filteredItems = items;
        this.originalItems = items;

        this.context = context;
        this.ocListener = ocListener;

        this.timer = new Timer();

        filter();
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="extended" desc="Methods">

    @Override
    public int getCount() {
        return filteredItems != null ? filteredItems.size() : 0;
    }

    @Override
    public IDisplayable getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final IDisplayable item = getItem(position);

        if (item instanceof BleDevice) {
            // <editor-fold defaultstate="collapsed" desc="BleDevice">

            final BleDevice device = (BleDevice) item;

            final ViewHolderDevice viewHolder;

            if (v == null || (v.getTag() != null && !(v.getTag() instanceof ViewHolderDevice))) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.card_device, parent, false);
                viewHolder = new ViewHolderDevice(v);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolderDevice) v.getTag();
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

            viewHolder.cllComponents.removeAllViews();

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
                        viewHolder.ivIcon.setImageResource(R.drawable.ic_panorama_fish_eye_black_36dp);
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

            // Show more
            viewHolder.tvShowLess.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (collapsed) {
                        viewHolder.cllComponents.expandVertically();
                        viewHolder.tvShowLess.setText(R.string.show_less);
                        collapsed = false;
                    } else {
                        viewHolder.cllComponents.collapseVertically();
                        viewHolder.tvShowLess.setText(R.string.show_more);
                        collapsed = true;
                    }
                }
            });

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
            viewHolder.ivInteroberlinUartRx.setImageDrawable(device.isSubscribing() ? ContextCompat.getDrawable(context, R.drawable.ic_pause_black_36dp) : ContextCompat.getDrawable(context, R.drawable.ic_play_arrow_black_36dp));
            viewHolder.ivSentientLightFloorSensorRx.setImageDrawable(device.isSubscribing() ? ContextCompat.getDrawable(context, R.drawable.ic_pause_black_36dp) : ContextCompat.getDrawable(context, R.drawable.ic_play_arrow_black_36dp));
            viewHolder.ivSentientLightLedRx.setImageDrawable(device.isSubscribing() ? ContextCompat.getDrawable(context, R.drawable.ic_pause_black_36dp) : ContextCompat.getDrawable(context, R.drawable.ic_play_arrow_black_36dp));

            if (!device.getReadings().isEmpty()) {
                viewHolder.cllComponents.addView(new DataComponent(context, device));

                switch (EDevice.fromString(device.getName())) {
                    case WUNDERBAR_LIGHT: {
                        viewHolder.cllComponents.addView(new LightProximityComponent(context, device));
                        break;
                    }
                    case WUNDERBAR_GYRO: {
                        viewHolder.cllComponents.addView(new AccelerometerGyroscopeComponent(context, device));
                        break;
                    }
                    case WUNDERBAR_MIC: {
                        viewHolder.cllComponents.addView(new MicrophoneComponent(context, device));
                        viewHolder.cllComponents.addView(new LineChartComponent(context, device));
                        break;
                    }
                    case INTEROBERLIN_SENTIENT_LIGHT: {
                        viewHolder.cllComponents.addView(new SentientLightComponent(context, device));
                        break;
                    }
                    default: {
                        viewHolder.cllComponents.addView(new LineChartComponent(context, device));
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

            // <editor-fold defaultstate="extended" desc="Subscribe DATA">
            if (device.containsCharacteristic(ECharacteristic.DATA)) {
                viewHolder.ivSubscribe.setVisibility(View.VISIBLE);
                viewHolder.ivSubscribe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Clicked subscribe");
                        ocListener.onSubscribe();
                        if (!device.isSubscribing()) {
                            device.subscribe(context, ECharacteristic.DATA);
                            ocListener.onChange(device, R.string.started_subscription);
                        } else {
                            device.unsubscribe(context, ECharacteristic.DATA);
                            device.disconnect();

                            ocListener.onChange(device, R.string.stopped_subscription);

                            if (timer != null) timer.cancel();
                        }

                        viewHolder.tvShowLess.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                viewHolder.ivSubscribe.setVisibility(View.GONE);
            }
            // </editor-fold>

            // <editor-fold defaultstate="extended" desc="Write LED">
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
            // </editor-fold>

            // <editor-fold defaultstate="extended" desc="Subscribe Interoberlin UART RX">
            if (device.containsCharacteristic(ECharacteristic.INTEROBERLIN_UART_RX)) {
                viewHolder.ivInteroberlinUartRx.getDrawable().setTint(ContextCompat.getColor(context, R.color.colorAccent));
                viewHolder.ivInteroberlinUartRx.setVisibility(View.VISIBLE);
                viewHolder.ivInteroberlinUartRx.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ocListener.onSubscribe();

                        if (!device.isSubscribing()) {
                            device.subscribe(context, ECharacteristic.INTEROBERLIN_UART_RX, true);
                            ocListener.onChange(device, R.string.started_subscription);
                        } else {
                            device.unsubscribe(context, ECharacteristic.INTEROBERLIN_UART_RX);
                            device.disconnect();

                            ocListener.onChange(device, R.string.stopped_subscription);

                            if (timer != null) timer.cancel();
                        }

                    }
                });
            } else {
                viewHolder.ivInteroberlinUartRx.setVisibility(View.GONE);
            }
            // </editor-fold>

            // <editor-fold defaultstate="extended" desc="Write Interoberlin UART TX">
            if (device.containsCharacteristic(ECharacteristic.INTEROBERLIN_UART_TX)) {
                viewHolder.ivInteroberlinUartTx.getDrawable().setTint(ContextCompat.getColor(context, R.color.colorAccent));
                viewHolder.ivInteroberlinUartTx.setVisibility(View.VISIBLE);
                viewHolder.ivInteroberlinUartTx.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ocListener.onOpenSendValueDialog(device, EService.INTEROBERLIN_UART, ECharacteristic.INTEROBERLIN_UART_TX);

                    }
                });
            } else {
                viewHolder.ivInteroberlinUartTx.setVisibility(View.GONE);
            }
            // </editor-fold>

            // <editor-fold defaultstate="extended" desc="Subscribe Sentient Light floor sensor RX">
            if (device.containsCharacteristic(ECharacteristic.SENTIENT_LIGHT_FLOOR_SENSOR_RX)) {
                viewHolder.ivSentientLightFloorSensorRx.getDrawable().setTint(ContextCompat.getColor(context, R.color.colorAccent));
                viewHolder.ivSentientLightFloorSensorRx.setVisibility(View.VISIBLE);
                viewHolder.ivSentientLightFloorSensorRx.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ocListener.onSubscribe();

                        if (!device.isSubscribing()) {
                            device.subscribe(context, ECharacteristic.SENTIENT_LIGHT_FLOOR_SENSOR_RX, true);
                            ocListener.onChange(device, R.string.started_subscription);
                        } else {
                            device.unsubscribe(context, ECharacteristic.SENTIENT_LIGHT_FLOOR_SENSOR_RX);
                            device.disconnect();

                            ocListener.onChange(device, R.string.stopped_subscription);

                            if (timer != null) timer.cancel();
                        }
                    }
                });
            } else {
                viewHolder.ivSentientLightFloorSensorRx.setVisibility(View.GONE);
            }
            // </editor-fold>

            // <editor-fold defaultstate="extended" desc="Write Sentient Light floor sensor TX">
            if (device.containsCharacteristic(ECharacteristic.SENTIENT_LIGHT_FLOOR_SENSOR_TX)) {
                viewHolder.ivSentientLightFloorSensorTx.getDrawable().setTint(ContextCompat.getColor(context, R.color.colorAccent));
                viewHolder.ivSentientLightFloorSensorTx.setVisibility(View.VISIBLE);
                viewHolder.ivSentientLightFloorSensorTx.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ocListener.onOpenSendValueDialog(device, EService.SENTIENT_LIGHT_FLOOR_SENSOR, ECharacteristic.SENTIENT_LIGHT_FLOOR_SENSOR_TX);
                    }
                });
            } else {
                viewHolder.ivSentientLightFloorSensorTx.setVisibility(View.GONE);
            }
            // </editor-fold>

            // <editor-fold defaultstate="extended" desc="Subscribe Sentient Light LED RX">
            if (device.containsCharacteristic(ECharacteristic.SENTIENT_LIGHT_LED_RX)) {
                viewHolder.ivSentientLightLedRx.getDrawable().setTint(ContextCompat.getColor(context, R.color.colorAccent));
                viewHolder.ivSentientLightLedRx.setVisibility(View.VISIBLE);
                viewHolder.ivSentientLightLedRx.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ocListener.onSubscribe();

                        if (!device.isSubscribing()) {
                            device.subscribe(context, ECharacteristic.SENTIENT_LIGHT_LED_RX, true);
                            ocListener.onChange(device, R.string.started_subscription);
                        } else {
                            device.unsubscribe(context, ECharacteristic.SENTIENT_LIGHT_LED_RX);
                            device.disconnect();

                            ocListener.onChange(device, R.string.stopped_subscription);

                            if (timer != null) timer.cancel();
                        }
                    }
                });
            } else {
                viewHolder.ivSentientLightLedRx.setVisibility(View.GONE);
            }
            // </editor-fold>

            // <editor-fold defaultstate="extended" desc="Write Sentient Light LED TX">
            if (device.containsCharacteristic(ECharacteristic.SENTIENT_LIGHT_LED_TX)) {
                viewHolder.ivSentientLightLedTx.getDrawable().setTint(ContextCompat.getColor(context, R.color.colorAccent));
                viewHolder.ivSentientLightLedTx.setVisibility(View.VISIBLE);
                viewHolder.ivSentientLightLedTx.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ocListener.onOpenSendValueDialog(device, EService.SENTIENT_LIGHT_LED, ECharacteristic.SENTIENT_LIGHT_LED_TX);
                    }
                });
            } else {
                viewHolder.ivSentientLightLedTx.setVisibility(View.GONE);
            }
            // </editor-fold>

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
                    devicesController.toggleAutoConnect(getContext(), device);
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
            // </editor-fold>
        } else if (item instanceof Mapping) {
            // <editor-fold defaultstate="collapsed" desc="Mapping">

            final Mapping mapping = (Mapping) item;

            final ViewHolderMapping viewHolder;

            if (v == null || (v.getTag() != null && !(v.getTag() instanceof ViewHolderMapping))) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.card_mapping, parent, false);
                viewHolder = new ViewHolderMapping(v);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolderMapping) v.getTag();
            }

            // Set values
            viewHolder.tvName.setText(mapping.getName());
            viewHolder.tvSource.setText(mapping.getSource().getAddress());
            viewHolder.tvSink.setText(mapping.getSink().getAddress());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                viewHolder.tvName.setTextAppearance(android.R.style.TextAppearance_Material_Title);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                viewHolder.ivTriggered.getDrawable().setTint(ContextCompat.getColor(context, mapping.isTriggered() ? R.color.colorAccentDark : R.color.md_grey_400));
                viewHolder.ivSource.getDrawable().setTint(ContextCompat.getColor(context, mapping.isSourceAttached() ? R.color.colorAccentDark : R.color.md_grey_400));
                viewHolder.ivSink.getDrawable().setTint(ContextCompat.getColor(context, mapping.isSinkAttached() ? R.color.colorAccentDark : R.color.md_grey_400));
            }

            // Add actions
            viewHolder.ivIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ocListener.onDetachMapping(mapping);
                }
            });

            return v;
            // </editor-fold>
        }

        return new View(context);
    }

    // </editor-fold>

    // --------------------
    // Methods - Filter
    // --------------------

    // <editor-fold defaultstate="extended" desc="Filter">

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
        if (devicesFilter == null) {
            devicesFilter = new DevicesFilter();
        }
        return devicesFilter;
    }

    /**
     * Determines if a displayable reading shall be displayed
     *
     * @param displayable displayable
     * @return true if item is visible
     */
    protected boolean filterBluetoothDeviceReading(IDisplayable displayable) {
        return displayable != null;
    }

    // </editor-fold>

    // --------------------
    // Callback interfaces
    // --------------------

    // <editor-fold defaultstate="extended" desc="Callback interfaces">

    public interface OnCompleteListener {
        void onChange(BleDevice device, int text);

        void onDetachDevice(BleDevice device);

        void onSendLocation(BleDevice device);

        void onOpenCharacteristicsDialog(BleDevice device);

        void onRead();

        void onSend();

        void onSubscribe();

        void onToggleAutoConnect();

        void onDetachMapping(Mapping mapping);

        void onOpenSendValueDialog(BleDevice device, EService service, ECharacteristic characteristic);
    }

    // </editor-fold>

    // --------------------
    // Inner classes
    // --------------------

    // <editor-fold defaultstate="extended" desc="Inner classes">

    public class DevicesFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            // Copy items
            originalItems = new ArrayList<>();
            originalItems.addAll(mappingController.getActiveMappingsAsList());
            originalItems.addAll(devicesController.getAttachedDevicesAsList());

            ArrayList<IDisplayable> values;
            synchronized (lock) {
                values = new ArrayList<>(originalItems);
            }

            final int count = values.size();
            final ArrayList<IDisplayable> newValues = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final IDisplayable value = values.get(i);
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
            filteredItems = (List<IDisplayable>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    // </editor-fold>
}