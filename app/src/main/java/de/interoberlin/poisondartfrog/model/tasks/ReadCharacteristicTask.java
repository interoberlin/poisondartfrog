package de.interoberlin.poisondartfrog.model.tasks;


import android.bluetooth.BluetoothGattCharacteristic;
import android.os.AsyncTask;

import de.interoberlin.poisondartfrog.model.BluetoothLeService;

public class ReadCharacteristicTask extends AsyncTask<BluetoothGattCharacteristic, Void, Void> {
    public static final String TAG = ReadCharacteristicTask.class.getSimpleName();

    private BluetoothLeService bluetoothLeService;
    private OnCompleteListener ocListener;

    // --------------------
    // Constructors
    // --------------------

    public ReadCharacteristicTask(BluetoothLeService bluetoothLeService) {
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
    protected Void doInBackground(BluetoothGattCharacteristic... params) {
        BluetoothGattCharacteristic characteristic = params[0];

        if (characteristic != null) {
            try {
                readCharacteristic(characteristic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    // --------------------
    // Methods
    // --------------------

    /**
     * Reads a given {@code characteristic}
     *
     * @param characteristic characteristic
     * @throws Exception
     */
    private void readCharacteristic(BluetoothGattCharacteristic characteristic) throws Exception {
        bluetoothLeService.readCharacteristic(characteristic);
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onFinished();
    }
}