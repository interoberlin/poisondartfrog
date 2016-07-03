package de.interoberlin.poisondartfrog.view.adapters;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.config.EDevice;

public class ScanResultsAdapter extends ArrayAdapter<BleDevice> {
    public static final String TAG = ScanResultFilter.class.getSimpleName();

    // Context
    private Context context;
    private Activity activity;
    private DialogFragment dialog;

    // View
    static class ViewHolder {
        private TextView tvName;
        private TextView tvAddress;
        private ImageView ivIcon;
    }

    // Controllers
    DevicesController devicesController;

    // Filter
    private List<BleDevice> filteredItems = new ArrayList<>();
    private List<BleDevice> originalItems = new ArrayList<>();
    private ScanResultFilter scanResultFilter;
    private final Object lock = new Object();

    // --------------------
    // Constructors
    // --------------------

    public ScanResultsAdapter(Context context, Activity activity, DialogFragment dialog, int resource, List<BleDevice> items) {
        super(context, resource, items);
        devicesController = DevicesController.getInstance();

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
        v = vi.inflate(R.layout.item_scan_result, parent, false);
            viewHolder.tvName = (TextView) v.findViewById(R.id.tvName);
            viewHolder.tvAddress = (TextView) v.findViewById(R.id.tvAddress);
            viewHolder.ivIcon = (ImageView) v.findViewById(R.id.ivIcon);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        // Set values
        viewHolder.tvName.setText(device.getName());
        viewHolder.tvAddress.setText(device.getAddress());

        if (device.getName() == null || device.getName().isEmpty())
            viewHolder.tvName.setText(R.string.unknown_device);
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
                case NRFDUINO : {
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

        // Add actions
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity instanceof OnCompleteListener) {
                    ((OnCompleteListener) activity).onAttachDevice(device);
                }

                devicesController.stopScan();
                dialog.dismiss();
            }
        });

        return v;
    }

    // --------------------
    // Methods - Filter
    // --------------------

    public List<BleDevice> getFilteredItems() {
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
    protected boolean filterBleDevice(BleDevice bleDevice) {
        return bleDevice != null;
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onAttachDevice(BleDevice device);
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

            ArrayList<BleDevice> values;
            synchronized (lock) {
                values = new ArrayList<>(originalItems);
            }

            final int count = values.size();
            final ArrayList<BleDevice> newValues = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final BleDevice value = values.get(i);
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
            filteredItems = (List<BleDevice>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}