package de.interoberlin.poisondartfrog.view.components;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.graphics.Typeface;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.UUID;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.config.repository.RepositoryMapper;

public class ServicesComponent extends TableLayout {
    // --------------------
    // Constructors
    // --------------------

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
            if (RepositoryMapper.getInstance().isKnownService(serviceId.toString()))
                tvService.setText(RepositoryMapper.getInstance().getServiceById(serviceId.toString()).getName());
            else
                tvService.setText(serviceId.toString());

            tvService.setPadding(0, 15, 0, 0);
            tvService.setTypeface(null, Typeface.BOLD);
            tvService.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            tvService.setTextAppearance(context, android.R.style.TextAppearance_Small);
            trService.addView(tvService);

            addView(trService);

            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                TableRow trCharacteristic = new TableRow(context);
                trCharacteristic.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView tvCharacteristic = new TextView(context);
                TextView tvValue = new TextView(context);

                UUID characteristicId = characteristic.getUuid();
                if (RepositoryMapper.getInstance().isKnownCharacteristic(characteristicId.toString())) {
                    tvCharacteristic.setText(RepositoryMapper.getInstance().getCharacteristicById(characteristicId.toString()).getName());
                } else {
                    tvCharacteristic.setText(characteristicId.toString().substring(0, 18));
                }
                tvCharacteristic.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                tvCharacteristic.setTextAppearance(context, android.R.style.TextAppearance_Small);
                tvCharacteristic.setPadding(20, 0, 40, 0);

                if (characteristic.getValue() != null && characteristic.getValue().length != 0) {
                    String characteristicValue = null; // TODO
                    tvValue.setText(characteristicValue);
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