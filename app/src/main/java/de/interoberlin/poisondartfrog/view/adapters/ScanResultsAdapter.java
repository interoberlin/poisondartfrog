package de.interoberlin.poisondartfrog.view.adapters;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.EBluetoothDeviceType;

public class ScanResultsAdapter extends ArrayAdapter<BluetoothDevice> {
    public static final String TAG = ScanResultFilter.class.getCanonicalName();

    // Context
    private Context context;
    private Activity activity;
    private DialogFragment dialog;

    // Controllers
    DevicesController devicesController;

    // Filter
    private List<BluetoothDevice> filteredItems = new ArrayList<>();
    private List<BluetoothDevice> originalItems = new ArrayList<>();
    private ScanResultFilter scanResultFilter;
    private final Object lock = new Object();

    // --------------------
    // Constructors
    // --------------------

    public ScanResultsAdapter(Context context, Activity activity, DialogFragment dialog, int resource, List<BluetoothDevice> items) {
        super(context, resource, items);
        devicesController = DevicesController.getInstance(activity);

        this.filteredItems = items;
        this.originalItems = items;

        this.context = context;
        this.activity = activity;
        this.dialog = dialog;

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
    public BluetoothDevice getItem(int position) {
        return filteredItems.get(position);
    }


    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final BluetoothDevice bleDevice = getItem(position);

        return getScanResultView(position, bleDevice, parent);
    }

    private View getScanResultView(final int position, final BluetoothDevice device, final ViewGroup parent) {
        // Layout inflater
        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());

        // Load views
        final RelativeLayout rlScanResult = (RelativeLayout) vi.inflate(R.layout.item_scan_result, parent, false);
        final TextView tvName = (TextView) rlScanResult.findViewById(R.id.tvName);
        final TextView tvAddress = (TextView) rlScanResult.findViewById(R.id.tvAddress);
        final ImageView ivIcon = (ImageView) rlScanResult.findViewById(R.id.ivIcon);

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

        // Add actions
        rlScanResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity instanceof OnCompleteListener) {
                    ((OnCompleteListener) activity).onSelectedScanResult(device);
                }

                dialog.dismiss();
            }
        });

        return rlScanResult;
    }

    // --------------------
    // Methods - Filter
    // --------------------

    public List<BluetoothDevice> getFilteredItems() {
        return filteredItems;
    }

    public void filter() {
        getFilter().filter("");
    }

    @Override
    public Filter getFilter() {
        if (scanResultFilter == null) {
            scanResultFilter = new ScanResultFilter();
        }
        return scanResultFilter;
    }

    /**
     * Determines if a bleDevice shall be displayed
     *
     * @param bleDevice bleDevice
     * @return true if item is visible
     */
    protected boolean filterBleDevice(BluetoothDevice bleDevice) {
        return bleDevice != null;
    }

    // --------------------
    // Methods - Util
    // --------------------

    private Resources getResources() {
        return activity.getResources();
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onSelectedScanResult(BluetoothDevice device);
    }

    // --------------------
    // Inner classes
    // --------------------

    public class ScanResultFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            // Copy items
            originalItems = devicesController.getScannedDevicesAsList();

            ArrayList<BluetoothDevice> values;
            synchronized (lock) {
                values = new ArrayList<>(originalItems);
            }

            final int count = values.size();
            final ArrayList<BluetoothDevice> newValues = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final BluetoothDevice value = values.get(i);
                if (filterBleDevice(value)) {
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
            filteredItems = (List<BluetoothDevice>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}