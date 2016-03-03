package de.interoberlin.poisondartfrog.model.tasks;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import de.interoberlin.poisondartfrog.App;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.BluetoothLeService;
import de.interoberlin.poisondartfrog.model.ExtendedBluetoothDevice;
import de.interoberlin.poisondartfrog.util.Configuration;

public class ReadCharacteristicsTask extends AsyncTask<ExtendedBluetoothDevice, Void, Void> {
    public static final String TAG = ReadCharacteristicsTask.class.getSimpleName();

    private BluetoothLeService bluetoothLeService;
    private OnCompleteListener ocListener;

    // --------------------
    // Constructors
    // --------------------

    public ReadCharacteristicsTask(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(ExtendedBluetoothDevice... params) {
        ExtendedBluetoothDevice device = params[0];

        if (device != null) {
            try {
                readCharacteristics(device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    // --------------------
    // Methods
    // --------------------

    /**
     * Reads characteristics of a given {@code device}
     *
     * @param device device
     * @throws Exception
     */
    private void readCharacteristics(ExtendedBluetoothDevice device) throws Exception {
        while (true) {
            for (BluetoothGattService service : device.getGattServices()) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    bluetoothLeService.readCharacteristic(characteristic);
                }
            }

            Context c = App.getContext();
            int SCAN_PERIOD = Configuration.getIntProperty(c, c.getResources().getString(R.string.scan_period));

            Log.v(TAG, "Sleep for " + SCAN_PERIOD + " millseconds");
            Thread.sleep(SCAN_PERIOD);
        }
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onFinished();
    }
}