package de.interoberlin.poisondartfrog.view.adapters;

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
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

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.EBluetoothDeviceType;
import de.interoberlin.poisondartfrog.model.ExtendedBluetoothDevice;
import de.interoberlin.poisondartfrog.view.components.ServicesComponent;

public class DevicesAdapter extends ArrayAdapter<ExtendedBluetoothDevice> {
    public static final String TAG = DevicesAdapter.class.getCanonicalName();

    // Context
    private final Context context;
    private final Activity activity;
    private OnCompleteListener ocListener;

    // Controllers
    DevicesController devicesController;

    // Filter
    private List<ExtendedBluetoothDevice> filteredItems = new ArrayList<>();
    private List<ExtendedBluetoothDevice> originalItems = new ArrayList<>();
    private BluetoothDeviceReadingFilter bluetoothDeviceReadingFilter;
    private final Object lock = new Object();

    // --------------------
    // Constructors
    // --------------------

    public DevicesAdapter(Context context, Activity activity, int resource, List<ExtendedBluetoothDevice> items) {
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
    public ExtendedBluetoothDevice getItem(int position) {
        return filteredItems.get(position);
    }


    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final ExtendedBluetoothDevice reading = getItem(position);

        return getCardView(position, reading, parent);
    }

    private View getCardView(final int position, final ExtendedBluetoothDevice device, final ViewGroup parent) {
        final List<BluetoothGattService> services = device.getGattServices();

        // Layout inflater
        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());

        // Load views
        final LinearLayout llCard = (LinearLayout) vi.inflate(R.layout.card_device, parent, false);
        final TextView tvName = (TextView) llCard.findViewById(R.id.tvName);
        final TextView tvAddress = (TextView) llCard.findViewById(R.id.tvAddress);
        final ImageView ivIcon = (ImageView) llCard.findViewById(R.id.ivIcon);
        final LinearLayout llComponents = (LinearLayout) llCard.findViewById(R.id.llComponents);
        final ImageView ivDetach = (ImageView) llCard.findViewById(R.id.ivDetach);
        final ImageView ivConnect = (ImageView) llCard.findViewById(R.id.ivConnect);

        // Set values
        tvName.setText(device.getName());
        tvAddress.setText(device.getAddress());

        if (device.getName() == null || device.getName().isEmpty())
            tvName.setText(R.string.unknown_device);

        if (EBluetoothDeviceType.fromString(device.getName()) != null) {
            switch (EBluetoothDeviceType.fromString(device.getName())) {
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

        llComponents.addView(new ServicesComponent(context, activity, services));

        ivConnect.setImageDrawable(device.isConnected() ? ContextCompat.getDrawable(activity, R.drawable.ic_pause_black_36dp) : ContextCompat.getDrawable(activity, R.drawable.ic_play_arrow_black_36dp));

        // Add actions
        ivDetach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocListener.onDetachDevice(device);
            }
        });
        ivConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (device.isConnected())
                    ocListener.onDisconnectDevice(device);
                else
                    ocListener.onConnectDevice(device);
            }
        });

        return llCard;
    }

    // --------------------
    // Methods - Filter
    // --------------------

    public List<ExtendedBluetoothDevice> getFilteredItems() {
        return filteredItems;
    }

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
    protected boolean filterBluetoothDeviceReading(ExtendedBluetoothDevice reading) {
        return reading != null;
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onConnectDevice(ExtendedBluetoothDevice device);

        void onDisconnectDevice(ExtendedBluetoothDevice device);

        void onDetachDevice(ExtendedBluetoothDevice device);
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

            ArrayList<ExtendedBluetoothDevice> values;
            synchronized (lock) {
                values = new ArrayList<>(originalItems);
            }

            final int count = values.size();
            final ArrayList<ExtendedBluetoothDevice> newValues = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final ExtendedBluetoothDevice value = values.get(i);
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
            filteredItems = (List<ExtendedBluetoothDevice>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}