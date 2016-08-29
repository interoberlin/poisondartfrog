package de.interoberlin.poisondartfrog.view.components;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.UUID;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.repository.ECharacteristic;
import de.interoberlin.merlot_android.model.repository.EService;
import de.interoberlin.poisondartfrog.R;

public class ServicesComponent extends TableLayout {
    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public ServicesComponent(Context context) {
        super(context);
    }

    public ServicesComponent(Context context, BleDevice device) {
        super(context);

        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int) context.getResources().getDimension(R.dimen.card_margin), 0, 0);
        setLayoutParams(lp);

        for (BluetoothGattService service : device.getServices()) {
            TableRow trService = new TableRow(context);
            trService.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView tvService = new TextView(context);

            UUID serviceId = service.getUuid();
            tvService.setText(EService.fromId(serviceId.toString()) != null ? EService.fromId(serviceId.toString()).getName() : serviceId.toString().substring(0, 18));

            tvService.setPadding(0, 15, 0, 0);
            tvService.setTypeface(null, Typeface.BOLD);
            tvService.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tvService.setTextAppearance(android.R.style.TextAppearance_Small);
            }
            trService.addView(tvService);

            addView(trService);

            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                TableRow trCharacteristic = new TableRow(context);
                trCharacteristic.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView tvCharacteristic = new TextView(context);
                TextView tvValue = new TextView(context);

                UUID characteristicId = characteristic.getUuid();
                tvCharacteristic.setText(ECharacteristic.fromId(characteristicId.toString()) != null ? ECharacteristic.fromId(characteristicId.toString()).getName() : characteristicId.toString().substring(0, 18));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvCharacteristic.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    tvCharacteristic.setTextAppearance(android.R.style.TextAppearance_Small);
                }
                tvCharacteristic.setPadding(20, 0, 40, 0);

                if (characteristic.getValue() != null && characteristic.getValue().length != 0) {
                    // String characteristicValue = null; // TODO
                    // tvValue.setText(characteristicValue);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        tvValue.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        tvValue.setTextAppearance(android.R.style.TextAppearance_Small);
                    }
                } else {
                    tvValue.setText(R.string.no_value);
                }

                trCharacteristic.addView(tvCharacteristic);
                trCharacteristic.addView(tvValue);

                addView(trCharacteristic);
            }
        }
    }

    // </editor-fold>
}