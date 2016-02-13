package de.interoberlin.poisondartfrog.model.wunderbar.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.interoberlin.poisondartfrog.App;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.WunderbarDevicesController;
import de.interoberlin.poisondartfrog.model.wunderbar.BleDeviceReading;
import de.interoberlin.poisondartfrog.model.wunderbar.RelayrSdkInitializer;
import de.interoberlin.poisondartfrog.util.Configuration;
import io.relayr.android.RelayrSdk;
import io.relayr.android.ble.BleDevice;
import io.relayr.android.ble.BleDeviceMode;
import io.relayr.java.ble.BleDeviceType;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

public class ScanWunderbarTask extends AsyncTask<BleDeviceType, Void, Void> {
    public static final String TAG = ScanWunderbarTask.class.getCanonicalName();

    private OnCompleteListener ocListener;

    // --------------------
    // Constructors
    // --------------------

    public ScanWunderbarTask(OnCompleteListener ocListener) {
        this.ocListener = ocListener;
    }

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(BleDeviceType... params) {
        try {
            scan(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        int scanPeriod = Configuration.getIntProperty(App.getContext(), App.getContext().getResources().getString(R.string.scan_period));

        try {
            Thread.sleep(scanPeriod);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.onPostExecute(aVoid);

        ocListener.onFinished();
    }

    // --------------------
    // Methods
    // --------------------

    /**
     * Scans for BLE devices of specific types
     *
     * @param types types to scan for
     * @throws Exception
     */
    public void scan(final BleDeviceType[] types) throws Exception {
        RelayrSdkInitializer.initSdk(App.getContext());
        RelayrSdk.getRelayrBleSdk()
                .scan(Arrays.asList(types))
                .filter(new Func1<List<BleDevice>, Boolean>() {
                    @Override
                    public Boolean call(List<BleDevice> bleDevices) {
                        WunderbarDevicesController wunderbarDevicesController = WunderbarDevicesController.getInstance(null);
                        wunderbarDevicesController.getScannedDevices().clear();

                        for (BleDevice device : bleDevices) {
                            if (device.getMode() == BleDeviceMode.DIRECT_CONNECTION) {
                                // Add found device to list
                                String address = device.getAddress();
                                Map<String, BleDevice> scannedDevices = wunderbarDevicesController.getScannedDevices();
                                Map<String, BleDeviceReading> subscribedDevices = wunderbarDevicesController.getSubscribedDevices();

                                if (!scannedDevices.containsKey(address) && !subscribedDevices.containsKey(address)) {
                                    wunderbarDevicesController.getScannedDevices().put(address, device);
                                    Log.i(TAG, "Found " + address);
                                }
                            }
                        }

                        return false;
                    }
                })
                .map(new Func1<List<BleDevice>, BleDevice>() {
                         @Override
                         public BleDevice call(List<BleDevice> bleDevices) {
                             for (BleDevice device : bleDevices) {
                                 if (device.getMode() == BleDeviceMode.DIRECT_CONNECTION) {
                                     return device;
                                 }
                             }
                             return null;
                         }
                     }
                )
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BleDevice>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(BleDevice device) {
                    }
                });

    }

    public interface OnCompleteListener {
        void onFinished();
    }
}