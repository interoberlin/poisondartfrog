package de.interoberlin.poisondartfrog.view.adapters;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.interoberlin.merlot_android.controller.DevicesController;
import de.interoberlin.merlot_android.model.config.repository.Characteristic;
import de.interoberlin.merlot_android.model.config.repository.RepositoryMapper;
import de.interoberlin.poisondartfrog.R;

public class CharacteristicsAdapter extends ArrayAdapter<BluetoothGattCharacteristic> {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = CharacteristicsAdapter.class.getSimpleName();

    //View
    static class ViewHolder {
        @BindView(R.id.tvId) TextView tvId;
        @BindView(R.id.tvName) TextView tvName;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }

    // Controllers
    DevicesController devicesController;

    // Filter
    private List<BluetoothGattCharacteristic> items = new ArrayList<>();

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public CharacteristicsAdapter(Context context, int resource, List<BluetoothGattCharacteristic> items) {
        super(context, resource, items);
        this.devicesController = DevicesController.getInstance();
        this.items = items;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

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

        final ViewHolder viewHolder;

        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.item_characteristic, parent, false);
            viewHolder = new ViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        Characteristic c = RepositoryMapper.getInstance(getContext()).getCharacteristicById(characteristic.getUuid().toString());

        // Set values
        viewHolder.tvId.setText(characteristic.getUuid().toString().substring(4, 8));
        viewHolder.tvName.setText(c != null ? c.getName() : getContext().getString(R.string.unknown_characteristic));

        return v;
    }

    // </editor-fold>
}