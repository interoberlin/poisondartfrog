package de.interoberlin.poisondartfrog.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
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
import java.util.List;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.config.ECharacteristic;
import de.interoberlin.poisondartfrog.model.config.EDevice;
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

        if (v != null) {
            // Load views
            v = vi.inflate(R.layout.card_device, parent, false);
            final TextView tvName = (TextView) v.findViewById(R.id.tvName);
            final TextView tvAddress = (TextView) v.findViewById(R.id.tvAddress);
            final ImageView ivIcon = (ImageView) v.findViewById(R.id.ivIcon);
            final LinearLayout llComponents = (LinearLayout) v.findViewById(R.id.llComponents);
            final ImageView ivDetach = (ImageView) v.findViewById(R.id.ivDetach);
            final ImageView ivSubscribe = (ImageView) v.findViewById(R.id.ivSubscribeData);
            final ImageView ivLedState = (ImageView) v.findViewById(R.id.ivLedState);

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
                        device.write(ECharacteristic.LED_STATE.getId(), true);
                    }
                });
            } else {
                ((ViewManager) ivLedState.getParent()).removeView(ivLedState);
            }
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