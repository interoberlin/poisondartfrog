package de.interoberlin.poisondartfrog.model;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import de.interoberlin.poisondartfrog.App;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.util.Configuration;
import io.relayr.android.RelayrSdk;
import io.relayr.android.ble.BleDevice;
import io.relayr.android.ble.BleDeviceMode;
import io.relayr.java.ble.BleDeviceType;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

public class ScanTask extends AsyncTask<BleDeviceType, Void, Void> {
    public static final String TAG = ScanTask.class.getCanonicalName();

    public Context context;
    public OnCompleteListener ocListener;

    // --------------------
    // Constructors
    // --------------------

    public ScanTask(OnCompleteListener ocListener) {
        this.ocListener = ocListener;
        this.context = App.getContext();
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
        Log.i(TAG, "Start ScanTask");
        try {
            int scanPeriod = Configuration.getIntProperty(context, context.getResources().getString(R.string.scan_period));
            scan(params, scanPeriod);
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
     * @param types      types to scan for
     * @param scanPeriod scanPeriod in seconds
     * @throws Exception
     */
    public static void scan(final BleDeviceType[] types, int scanPeriod) throws Exception {
        Log.i(TAG, "Started Scanning ... (scanPeriod " + scanPeriod + ")");

        RelayrSdkInitializer.initSdk(App.getContext());

        RelayrSdk.getRelayrBleSdk()
                .scan(Arrays.asList(types), scanPeriod)
                .filter(new Func1<List<BleDevice>, Boolean>() {
                    @Override
                    public Boolean call(List<BleDevice> bleDevices) {
                        Log.i(TAG, "call");
                        DevicesController devicesController = DevicesController.getInstance(null);
                        devicesController.getScannedDevices().clear();

                        for (BleDevice device : bleDevices) {
                            if (device.getMode() == BleDeviceMode.DIRECT_CONNECTION ) {

                                // Add found device to list
                                if (!devicesController.getScannedDevices().contains(device))
                                {
                                    devicesController.getScannedDevices().add(device);
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
                                     Log.i(TAG, "mapped " + device.getType() + " " + device.getAddress());
                                     return device;
                                 }
                             }
                             return null;
                         }
                     }
                )
                // .take(100)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BleDevice>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(BleDevice device) {
                        Log.i(TAG, "next " + device.getType() + " " + device.getAddress());
                    }
                });

    }

    public interface OnCompleteListener {
        void onFinished();
    }
}