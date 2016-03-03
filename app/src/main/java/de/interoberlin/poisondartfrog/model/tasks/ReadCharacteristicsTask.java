package de.interoberlin.poisondartfrog.model.tasks;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.AsyncTask;

import de.interoberlin.poisondartfrog.model.BluetoothLeService;
import de.interoberlin.poisondartfrog.model.ExtendedBluetoothDevice;

public class ReadCharacteristicsTask extends AsyncTask<ExtendedBluetoothDevice, Void, Void> {
    public static final String TAG = ReadCharacteristicsTask.class.getSimpleName();
    private static final int INTERVAL = 2500;

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

            Thread.sleep(INTERVAL);
        }
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onFinished();
    }
}