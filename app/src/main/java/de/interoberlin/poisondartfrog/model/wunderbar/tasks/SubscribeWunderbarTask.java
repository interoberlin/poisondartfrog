package de.interoberlin.poisondartfrog.model.wunderbar.tasks;

import android.os.AsyncTask;
import android.util.Log;

import de.interoberlin.poisondartfrog.App;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.util.Configuration;
import io.relayr.android.ble.BleDevice;
import io.relayr.android.ble.service.BaseService;
import io.relayr.android.ble.service.DirectConnectionService;
import io.relayr.java.model.action.Reading;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;

public class SubscribeWunderbarTask extends AsyncTask<BleDevice, Void, Void> {
    public static final String TAG = SubscribeWunderbarTask.class.getCanonicalName();

    public OnCompleteListener ocListener;

    // --------------------
    // Constructors
    // --------------------

    public SubscribeWunderbarTask(OnCompleteListener ocListener) {
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
    protected Void doInBackground(BleDevice... params) {
        try {
            subscribe(params[0]);
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
    }

    // --------------------
    // Methods
    // --------------------

    /**
     * Subscribes values of {@code device}
     *
     * @param device device
     * @throws Exception
     */
    public void subscribe(final BleDevice device) throws Exception {
        device.connect()
                .flatMap(new Func1<BaseService, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(BaseService baseService) {
                        return ((DirectConnectionService) baseService).getReadings();
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        device.disconnect();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Reading>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Reading reading) {
                        Log.d(TAG, "Read " + reading.toString());
                        ocListener.onReceivedReading(device, reading);
                    }
                });
    }

    public interface OnCompleteListener {
        void onReceivedReading(BleDevice device, Reading reading);
    }
}