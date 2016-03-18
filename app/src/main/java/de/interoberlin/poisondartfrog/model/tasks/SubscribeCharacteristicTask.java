package de.interoberlin.poisondartfrog.model.tasks;


import android.bluetooth.BluetoothGattCharacteristic;
import android.os.AsyncTask;
import android.util.Log;

import de.interoberlin.poisondartfrog.model.BluetoothLeService;

public class SubscribeCharacteristicTask extends AsyncTask<BluetoothGattCharacteristic, Void, Void> {
    public static final String TAG = SubscribeCharacteristicTask.class.getSimpleName();

    private BluetoothLeService bluetoothLeService;
    private OnCompleteListener ocListener;

    // --------------------
    // Constructors
    // --------------------

    public SubscribeCharacteristicTask(BluetoothLeService bluetoothLeService) {
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
                subscribeCharacteristic(characteristic);
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
     * Subscribes a given {@code characteristic}
     *
     * @param characteristic characteristic
     * @throws Exception
     */
    private void subscribeCharacteristic(BluetoothGattCharacteristic characteristic) throws Exception {
        Log.i(TAG, "Subscribe " + characteristic.getUuid());
        bluetoothLeService.setCharacteristicNotification(characteristic, true);
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onFinished();
    }
}