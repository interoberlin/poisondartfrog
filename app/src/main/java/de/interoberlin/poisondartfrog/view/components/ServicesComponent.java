package de.interoberlin.poisondartfrog.view.components;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.ExtendedBluetoothDevice;
import de.interoberlin.poisondartfrog.model.devices.PropertyMapper;

public class ServicesComponent extends TableLayout {
    // --------------------
    // Constructors
    // --------------------

    public ServicesComponent(Context context) {
        super(context);
    }

    public ServicesComponent(Context context, List<BluetoothGattService> services) {
        super(context);

        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int) context.getResources().getDimension(R.dimen.card_margin), 0, 0);
        setLayoutParams(lp);

        for (BluetoothGattService service : services) {
            TableRow trService = new TableRow(context);
            trService.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView tvService = new TextView(context);

            String serviceId = service.getUuid().toString();
            if (PropertyMapper.getInstance().isKnownService(serviceId))
                tvService.setText(PropertyMapper.getInstance().getServiceById(serviceId).getName());
            else
                tvService.setText(serviceId);

            tvService.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            tvService.setTextAppearance(context, android.R.style.TextAppearance_Small);
            trService.addView(tvService);

            addView(trService);

            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                TableRow trCharacteristic = new TableRow(context);
                trCharacteristic.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView tvCharacteristic = new TextView(context);
                TextView tvValue = new TextView(context);

                String characteristicId = characteristic.getUuid().toString();
                if (PropertyMapper.getInstance().isKnownCharacteristic(characteristicId)) {
                    tvCharacteristic.setText("  " + PropertyMapper.getInstance().getCharacteristicById(characteristicId).getName());
                } else {
                    tvCharacteristic.setText("  " + characteristicId.substring(0, 18) + "...");
                }
                tvCharacteristic.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                tvCharacteristic.setTextAppearance(context, android.R.style.TextAppearance_Small);

                if (characteristic.getValue() != null && characteristic.getValue().length != 0) {
                    String characteristicValue = ExtendedBluetoothDevice.parseValue(characteristic);
                    tvValue.setText(" " + characteristicValue);
                    tvValue.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                    tvValue.setTextAppearance(context, android.R.style.TextAppearance_Small);
                } else {
                    tvValue.setText(R.string.no_value);
                }

                trCharacteristic.addView(tvCharacteristic);
                trCharacteristic.addView(tvValue);

                addView(trCharacteristic);
            }
        }
    }
}