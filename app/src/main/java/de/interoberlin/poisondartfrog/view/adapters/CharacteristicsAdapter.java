package de.interoberlin.poisondartfrog.view.adapters;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.config.repository.Characteristic;
import de.interoberlin.poisondartfrog.model.config.repository.RepositoryMapper;

public class CharacteristicsAdapter extends ArrayAdapter<BluetoothGattCharacteristic> {
    public static final String TAG = CharacteristicsAdapter.class.getSimpleName();

    // Controllers
    DevicesController devicesController;

    // Filter
    private List<BluetoothGattCharacteristic> items = new ArrayList<>();

    // --------------------
    // Constructors
    // --------------------

    public CharacteristicsAdapter(Context context, Activity activity, DialogFragment dialog, int resource, List<BluetoothGattCharacteristic> items) {
        super(context, resource, items);
        this.devicesController = DevicesController.getInstance();
        this.items = items;
    }

    // --------------------
    // Methods
    // --------------------

    @Override
    public int getCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public BluetoothGattCharacteristic getItem(int position) {
        return items.get(position);
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final BluetoothGattCharacteristic characteristic = getItem(position);

        // Layout inflater
        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());

        // Load views
        final LinearLayout llItemCharacteristic = (LinearLayout) vi.inflate(R.layout.item_characteristic, parent, false);
        final TextView tvId = (TextView) llItemCharacteristic.findViewById(R.id.tvId);
        final TextView tvName = (TextView) llItemCharacteristic.findViewById(R.id.tvName);

        Characteristic c = RepositoryMapper.getInstance().getCharacteristicById(characteristic.getUuid().toString());

        // Set values
        tvId.setText(characteristic.getUuid().toString().substring(4,8));
        tvName.setText(c != null ? c.getName() : getContext().getString(R.string.unknown_characteristic));

        return llItemCharacteristic;
    }
}