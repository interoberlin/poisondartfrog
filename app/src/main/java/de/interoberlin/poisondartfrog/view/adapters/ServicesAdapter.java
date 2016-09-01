package de.interoberlin.poisondartfrog.view.adapters;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.interoberlin.merlot_android.controller.DevicesController;
import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.repository.ECharacteristic;
import de.interoberlin.merlot_android.model.repository.EService;
import de.interoberlin.poisondartfrog.R;

public class ServicesAdapter extends ArrayAdapter<BluetoothGattService> {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = ServicesAdapter.class.getSimpleName();

    // Model
    private BleDevice device;
    private List<BluetoothGattService> items = new ArrayList<>();

    //View
    static class ViewHolder {
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvUuid)
        TextView tvUuid;
        @BindView(R.id.tvType)
        TextView tvType;
        @BindView(R.id.llCharacteristics)
        LinearLayout llCharacteristics;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }

    // Controllers
    DevicesController devicesController;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public ServicesAdapter(Context context, int resource, BleDevice device, List<BluetoothGattService> items) {
        super(context, resource, items);
        this.devicesController = DevicesController.getInstance();
        this.device = device;
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
    public BluetoothGattService getItem(int position) {
        return items.get(position);
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final BluetoothGattService service = getItem(position);

        final ViewHolder viewHolder;

        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.item_service, parent, false);
            viewHolder = new ViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final EService s = EService.fromId(service.getUuid().toString());

        int type = service.getType();

        StringBuilder sbType = new StringBuilder();
        if (matchesMask(type, BluetoothGattService.SERVICE_TYPE_PRIMARY))
            sbType.append(", PRIMARY");
        if (matchesMask(type, BluetoothGattService.SERVICE_TYPE_SECONDARY))
            sbType.append(", SECONDARY");

        // Set values
        viewHolder.tvName.setText(s != null ? s.getName() : getContext().getString(R.string.unknown_service));
        viewHolder.tvUuid.setText(service.getUuid().toString().substring(4, 8));
        viewHolder.tvType.setText(sbType.toString().replaceFirst(", ", ""));
        viewHolder.llCharacteristics.removeAllViews();

        for (final BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
            View vCharacteristic = LayoutInflater.from(getContext()).inflate(R.layout.item_characteristic, parent, false);

            TextView tvName = (TextView) vCharacteristic.findViewById(R.id.tvName);
            TextView tvUuid = (TextView) vCharacteristic.findViewById(R.id.tvUuid);
            TextView tvProperties = (TextView) vCharacteristic.findViewById(R.id.tvProperties);
            TextView tvPermissions = (TextView) vCharacteristic.findViewById(R.id.tvPermissions);
            ImageView ivRead = (ImageView) vCharacteristic.findViewById(R.id.ivRead);
            ImageView ivWrite = (ImageView) vCharacteristic.findViewById(R.id.ivWrite);
            ImageView ivSubscribe = (ImageView) vCharacteristic.findViewById(R.id.ivSubscribe);

            final ECharacteristic c = ECharacteristic.fromId(characteristic.getUuid().toString());

            int properties = characteristic.getProperties();
            int permissions = characteristic.getPermissions();

            StringBuilder sbProperties = new StringBuilder();
            if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_BROADCAST))
                sbProperties.append(", BROADCAST");
            if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS))
                sbProperties.append(", EXTENDED PROPS");
            if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_INDICATE))
                sbProperties.append(", INDICATE");
            if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_NOTIFY))
                sbProperties.append(", NOTIFY");
            if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_READ))
                sbProperties.append(", READ");
            if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE))
                sbProperties.append(", SIGNED WRITE");
            if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_WRITE))
                sbProperties.append(", WRITE");
            if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE))
                sbProperties.append(", WRITE NO RESPONSE");

            StringBuilder sbPermissions = new StringBuilder();
            if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_READ))
                sbPermissions.append(", READ");
            if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED))
                sbPermissions.append(", READ ENCRYPTED");
            if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM))
                sbPermissions.append(", READ ENCRYPTED MITM");
            if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_WRITE))
                sbPermissions.append(", WRITE");
            if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED))
                sbPermissions.append(", WRITE ENCRYPTED");
            if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM))
                sbPermissions.append(", WRITE ENCRYPTED MITM");
            if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED))
                sbPermissions.append(", WRITE SIGNED");
            if (matchesMask(permissions, BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM))
                sbPermissions.append(", WRITE SIGNED MITM");

            // Set values
            tvName.setText(c != null ? c.getName() : getContext().getString(R.string.unknown_characteristic));
            tvUuid.setText(characteristic.getUuid().toString().substring(4, 8));
            tvProperties.setText(sbProperties.toString().replaceFirst(", ", ""));
            tvPermissions.setText(sbPermissions.toString().replaceFirst(", ", ""));

            // Add actions
            if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_READ)) {
                ivRead.setVisibility(View.VISIBLE);
                ivRead.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        device.read(c);
                    }
                });
            } else {
                ivRead.setVisibility(View.GONE);
            }

            if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_WRITE) || matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) {
                ivWrite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (c != null) {
                            device.write(s, c, "hello");
                        }
                    }
                });
            } else {
                ivWrite.setVisibility(View.GONE);
            }

            if (matchesMask(properties, BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
                ivSubscribe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        device.subscribe(getContext(), c);
                    }
                });
            } else {
                ivSubscribe.setVisibility(View.GONE);
            }


            viewHolder.llCharacteristics.addView(vCharacteristic);
        }

        return v;
    }

    private boolean matchesMask(int properties, int mask) {
        return (properties & mask) == mask;
    }

    // </editor-fold>
}