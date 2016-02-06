package de.interoberlin.poisondartfrog.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.BleDeviceReading;
import de.interoberlin.poisondartfrog.model.EMeaning;
import de.interoberlin.poisondartfrog.model.tasks.SubscribeTask;
import de.interoberlin.poisondartfrog.view.components.LightProximityComponent;
import io.relayr.android.ble.BleDevice;

public class DevicesAdapter extends ArrayAdapter<BleDeviceReading> {
    // Context
    private Context context;
    private Activity activity;

    // Controllers
    DevicesController devicesController;

    // Filter
    private List<BleDeviceReading> filteredItems = new ArrayList<>();
    private List<BleDeviceReading> originalItems = new ArrayList<>();
    private BleDeviceReadingFilter bleDeviceReadingFilter;
    private final Object lock = new Object();

    // --------------------
    // Constructors
    // --------------------

    public DevicesAdapter(Context context, Activity activity, int resource, List<BleDeviceReading> items) {
        super(context, resource, items);
        devicesController = DevicesController.getInstance(activity);

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
    public BleDeviceReading getItem(int position) {
        return filteredItems.get(position);
    }


    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final BleDeviceReading bleDeviceReading = getItem(position);

        return getCardView(position, bleDeviceReading, parent);
    }

    private View getCardView(final int position, final BleDeviceReading bleDeviceReading, final ViewGroup parent) {
        final BleDevice device = bleDeviceReading.getDevice();
        final Map<EMeaning, String> readings = bleDeviceReading.getReadings();

        // Layout inflater
        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());

        // Load views
        final LinearLayout llCard = (LinearLayout) vi.inflate(R.layout.card_device, parent, false);
        final RelativeLayout rlMain = (RelativeLayout) llCard.findViewById(R.id.rlMain);

        final TextView tvName = (TextView) llCard.findViewById(R.id.tvName);
        final TextView tvAddress = (TextView) llCard.findViewById(R.id.tvAddress);
        final ImageView ivIcon = (ImageView) llCard.findViewById(R.id.ivIcon);

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
                String luminosity = readings.get(EMeaning.LUMNINOSITY);
                String proximity = readings.get(EMeaning.PROXIMITY);
                String color = readings.get(EMeaning.COLOR);
                rlMain.addView(new LightProximityComponent(context, activity, luminosity, proximity, color));
                break;
            }
            case WunderbarMIC: {
                ivIcon.setImageResource(R.drawable.ic_mic_black_48dp);
                break;
            }
        }

        // Add actions
        llCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new SubscribeTask((SubscribeTask.OnCompleteListener) activity).execute(device).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        return llCard;
    }


    // --------------------
    // Methods - Filter
    // --------------------

    public List<BleDeviceReading> getFilteredItems() {
        return filteredItems;
    }

    public void filter() {
        getFilter().filter("");
    }

    @Override
    public Filter getFilter() {
        if (bleDeviceReadingFilter == null) {
            bleDeviceReadingFilter = new BleDeviceReadingFilter();
        }
        return bleDeviceReadingFilter;
    }

    /**
     * Determines if a BLE device reading shall be displayed
     *
     * @param bleDeviceReading BLE device reading
     * @return true if item is visible
     */
    protected boolean filterBleDeviceReading(BleDeviceReading bleDeviceReading) {
        return bleDeviceReading != null;
    }

    // --------------------
    // Inner classes
    // --------------------

    public class BleDeviceReadingFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            // Copy items
            originalItems = devicesController.getSubscribedDevicesAsList();

            ArrayList<BleDeviceReading> values;
            synchronized (lock) {
                values = new ArrayList<>(originalItems);
            }

            final int count = values.size();
            final ArrayList<BleDeviceReading> newValues = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final BleDeviceReading value = values.get(i);
                if (filterBleDeviceReading(value)) {
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
            filteredItems = (List<BleDeviceReading>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}