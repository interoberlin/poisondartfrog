package de.interoberlin.poisondartfrog.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.WunderbarDevicesController;
import de.interoberlin.poisondartfrog.model.wunderbar.BleDeviceReading;
import de.interoberlin.poisondartfrog.model.wunderbar.EReadingType;
import de.interoberlin.poisondartfrog.model.wunderbar.reading.LightProximity;
import de.interoberlin.poisondartfrog.model.wunderbar.tasks.SubscribeWunderbarTask;
import de.interoberlin.poisondartfrog.view.components.ReadingComponent;
import io.relayr.android.ble.BleDevice;

public class DevicesAdapter extends ArrayAdapter<BleDeviceReading> {
    public static final String TAG = DevicesAdapter.class.getCanonicalName();

    // Context
    private Context context;
    private Activity activity;

    // Controllers
    WunderbarDevicesController wunderbarDevicesController;

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
        final Map<EReadingType, String> readings = bleDeviceReading.getReadings();

        // Layout inflater
        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());

        // Load views
        final LinearLayout llCard = (LinearLayout) vi.inflate(R.layout.card_device, parent, false);
        final LinearLayout llComponents = (LinearLayout) llCard.findViewById(R.id.llComponents);

        final TextView tvName = (TextView) llCard.findViewById(R.id.tvName);
        final TextView tvAddress = (TextView) llCard.findViewById(R.id.tvAddress);
        final ImageView ivIcon = (ImageView) llCard.findViewById(R.id.ivIcon);

        // Set values
        tvName.setText(device.getName());
        tvAddress.setText(device.getAddress());

        switch (device.getType()) {
            case WunderbarHTU: {
                ivIcon.setImageResource(R.drawable.ic_invert_colors_black_48dp);
                String temperature = readings.get(EReadingType.TEMPERATURE);
                String humidity = readings.get(EReadingType.HUMIDITY);

                double temp = temperature != null ? Double.parseDouble(temperature) : 0.0;
                double humi = humidity != null ? Double.parseDouble(humidity) : 0.0;

                Paint paintTemperatureMin = new Paint();
                Paint paintTemperatureMax = new Paint();
                Paint paintHumidityMin = new Paint();
                Paint paintHumidityMax = new Paint();
                paintTemperatureMin.setARGB(255, 0, 0, 255);
                paintTemperatureMax.setARGB(255, 255, 0, 0);
                paintHumidityMin.setARGB(0, 0, 0, 255);
                paintHumidityMax.setARGB(255, 0, 0, 255);

                llComponents.addView(new ReadingComponent(context, activity, EReadingType.TEMPERATURE, temp, paintTemperatureMin, paintTemperatureMax));
                llComponents.addView(new ReadingComponent(context, activity, EReadingType.HUMIDITY, humi, paintHumidityMin, paintHumidityMax));
                break;
            }
            case WunderbarGYRO: {
                ivIcon.setImageResource(R.drawable.ic_vibration_black_48dp);
                break;
            }
            case WunderbarLIGHT: {
                ivIcon.setImageResource(R.drawable.ic_lightbulb_outline_black_48dp);
                String luminosity = readings.get(EReadingType.LUMINOSITY);
                String proximity = readings.get(EReadingType.PROXIMITY);
                String color = readings.get(EReadingType.COLOR);

                double prox = proximity != null ? Double.parseDouble(proximity) : 0.0;
                double lumi = luminosity != null ? Double.parseDouble(luminosity) : 0.0;
                LightProximity.Color colo = color != null ? new Gson().fromJson(color, LightProximity.Color.class) : new LightProximity.Color();

                Paint paintLuminosityMin = new Paint();
                Paint paintLuminosityMax = new Paint();
                Paint paintColor = new Paint();
                paintLuminosityMin.setARGB(255, 5, 5, 5);
                paintLuminosityMax.setARGB(255, 250, 250, 250);
                paintColor.setARGB(255, colo.getRed(), colo.getGreen(), colo.getBlue());

                llComponents.addView(new ReadingComponent(context, activity, EReadingType.LUMINOSITY, lumi, paintLuminosityMin, paintLuminosityMax));
                llComponents.addView(new ReadingComponent(context, activity, EReadingType.PROXIMITY, prox, 0.2, 1.0));
                llComponents.addView(new ReadingComponent(context, activity, EReadingType.COLOR, colo.getRed() + " " + colo.getGreen() + " " +  colo.getBlue(), paintColor));
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
                    new SubscribeWunderbarTask((SubscribeWunderbarTask.OnCompleteListener) activity).execute(device).get();
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
            originalItems = wunderbarDevicesController.getSubscribedDevicesAsList();

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