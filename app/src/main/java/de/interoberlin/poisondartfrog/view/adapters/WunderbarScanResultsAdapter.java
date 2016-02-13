package de.interoberlin.poisondartfrog.view.adapters;

import android.app.Activity;
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
import java.util.concurrent.ExecutionException;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.WunderbarDevicesController;
import de.interoberlin.poisondartfrog.model.wunderbar.BleDeviceReading;
import de.interoberlin.poisondartfrog.model.wunderbar.tasks.SubscribeWunderbarTask;
import io.relayr.android.ble.BleDevice;

public class WunderbarScanResultsAdapter extends ArrayAdapter<BleDevice> {
    public static final String TAG = ScanResultFilter.class.getCanonicalName();

    // Context
    private Context context;
    private Activity activity;

    // Controllers
    WunderbarDevicesController wunderbarDevicesController;

    // Filter
    private List<BleDevice> filteredItems = new ArrayList<>();
    private List<BleDevice> originalItems = new ArrayList<>();
    private ScanResultFilter scanResultFilter;
    private final Object lock = new Object();

    // --------------------
    // Constructors
    // --------------------

    public WunderbarScanResultsAdapter(Context context, Activity activity, int resource, List<BleDevice> items) {
        super(context, resource, items);
        wunderbarDevicesController = WunderbarDevicesController.getInstance(activity);

        this.filteredItems = items;
        this.originalItems = items;

        this.context = context;
        this.activity = activity;

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
        final BleDevice bleDevice = getItem(position);

        return getScanResultView(position, bleDevice, parent);
    }

    private View getScanResultView(final int position, final BleDevice device, final ViewGroup parent) {
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

        switch (device.getType()) {
            case WunderbarHTU: {
                ivIcon.setImageResource(R.drawable.ic_invert_colors_black_48dp);
                break;
            }
            case WunderbarGYRO: {
                ivIcon.setImageResource(R.drawable.ic_vibration_black_48dp);
                break;
            }
            case WunderbarLIGHT: {
                ivIcon.setImageResource(R.drawable.ic_lightbulb_outline_black_48dp);
                break;
            }
            case WunderbarMIC: {
                ivIcon.setImageResource(R.drawable.ic_mic_black_48dp);
                break;
            }
        }

        // Add actions
        rlScanResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity instanceof SubscribeWunderbarTask.OnCompleteListener) {
                    wunderbarDevicesController.getScannedDevices().remove(device.getAddress());
                    wunderbarDevicesController.getSubscribedDevices().put(device.getAddress(), new BleDeviceReading(device));

                    try {
                        new SubscribeWunderbarTask((SubscribeWunderbarTask.OnCompleteListener) activity).execute(device).get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                if (activity instanceof OnCompleteListener) {
                    ((OnCompleteListener) activity).onSelectedItem();
                }
            }
        });

        return rlScanResult;
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
    // Methods - Util
    // --------------------

    private Resources getResources() {
        return activity.getResources();
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onSelectedItem();
    }

    // --------------------
    // Inner classes
    // --------------------

    public class ScanResultFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            // Copy items
            originalItems = wunderbarDevicesController.getScannedDevicesAsList();

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