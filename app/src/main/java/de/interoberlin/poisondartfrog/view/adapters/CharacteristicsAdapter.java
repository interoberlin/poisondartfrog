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
import de.interoberlin.merlot_android.model.repository.ECharacteristic;
import de.interoberlin.poisondartfrog.R;

public class CharacteristicsAdapter extends ArrayAdapter<BluetoothGattCharacteristic> {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = CharacteristicsAdapter.class.getSimpleName();

    //View
    static class ViewHolder {
        @BindView(R.id.tvName) TextView tvName;
        @BindView(R.id.tvUuid) TextView tvUuid;
        @BindView(R.id.tvProperties) TextView tvProperties;
        @BindView(R.id.tvPermissions) TextView tvPermissions;

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

        ECharacteristic c = ECharacteristic.fromId(characteristic.getUuid().toString());

        int properties = characteristic.getProperties();
        int permissions = characteristic.getPermissions();

        StringBuilder sbProperties = new StringBuilder();
        if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_BROADCAST)) sbProperties.append(", BROADCAST");
        if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS)) sbProperties.append(", EXTENDED PROPS");
        if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_INDICATE)) sbProperties.append(", INDICATE");
        if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_NOTIFY)) sbProperties.append(", NOTIFY");
        if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_READ)) sbProperties.append(", READ");
        if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE)) sbProperties.append(", SIGNED WRITE");
        if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_WRITE)) sbProperties.append(", WRITE");
        if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) sbProperties.append(", WRITE NO RESPONSE");

        StringBuilder sbPermissions = new StringBuilder();
        if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_READ)) sbPermissions.append(", READ");
        if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED)) sbPermissions.append(", READ ENCRYPTED");
        if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM)) sbPermissions.append(", READ ENCRYPTED MITM");
        if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_WRITE)) sbPermissions.append(", WRITE");
        if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED)) sbPermissions.append(", WRITE ENCRYPTED");
        if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM)) sbPermissions.append(", WRITE ENCRYPTED MITM");
        if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED)) sbPermissions.append(", WRITE SIGNED");
        if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM)) sbPermissions.append(", WRITE SIGNED MITM");

        // Set values
        viewHolder.tvName.setText(c != null ? c.getName() : getContext().getString(R.string.unknown_characteristic));
        viewHolder.tvUuid.setText(characteristic.getUuid().toString().substring(4, 8));
        viewHolder.tvProperties.setText(sbProperties.toString().replaceFirst(", ", ""));
        viewHolder.tvPermissions.setText(sbPermissions.toString().replaceFirst(", ", ""));

        return v;
    }

    private boolean matchesMask(int properties, int mask) {
        return (properties & mask) == mask;
    }

    // </editor-fold>
}