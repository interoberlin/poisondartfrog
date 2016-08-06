package de.interoberlin.poisondartfrog.model.mapping;

import android.util.Log;

import de.interoberlin.poisondartfrog.model.IDisplayable;
import de.interoberlin.poisondartfrog.model.ble.BleDevice;
import de.interoberlin.poisondartfrog.model.service.Reading;
import rx.Observer;
import rx.Subscription;

public class Mapping implements IDisplayable {
    // <editor-fold defaultstate="expanded" desc="Members">

    public static final String TAG = Mapping.class.getSimpleName();

    private String name;
    private String source;
    private String sink;
    private transient boolean sourceAttached;
    private transient boolean sourceSubscribed;
    private transient boolean sinkAttached;
    private transient boolean triggered;

    private Subscription subscription;

    private OnChangeListener ocListener;

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Methods">

    public Mapping() {
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Methods">

    public void subscribeTo(BleDevice device) {
        Log.d(TAG, name + " subscribeTo " + device.getAddress() + " / " + device.getReadingObservable());

        sourceSubscribed = true;
        subscription = device.getReadingObservable()
                .subscribe(new Observer<Reading>() {
                    @Override
                    public void onNext(Reading reading) {
                        Log.d(TAG, "onNext / reading " + reading.meaning + " : " + reading.value);
                    }

                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.getMessage());
                    }
                });
    }

    public void unsubscribeFrom(BleDevice device) {
        Log.d(TAG, name + " unsubscribeFrom " + device.getAddress() + " / " + device.getReadingObservable());

        sourceSubscribed = false;
        if (subscription != null)
            subscription.unsubscribe();
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Getters / Setters">

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSink() {
        return sink;
    }

    public void setSink(String sink) {
        this.sink = sink;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setSourceAttached(boolean sourceAttached) {
        this.sourceAttached = sourceAttached;
    }

    public boolean isSinkAttached() {
        return sinkAttached;
    }

    public boolean isSourceSubscribed() {
        return sourceSubscribed;
    }

    public void setSourceSubscribed(boolean sourceSubscribed) {
        this.sourceSubscribed = sourceSubscribed;
    }

    public void setSinkAttached(boolean sinkAttached) {
        this.sinkAttached = sinkAttached;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    public boolean isSourceAttached() {
        return sourceAttached;
    }

    // </editor-fold>

    // --------------------
    // Callback interfaces
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Callback interfaces">

    public interface OnChangeListener {
        void onChange(Mapping mapping);
    }

    public void registerOnChangeListener(OnChangeListener ocListener) {
        this.ocListener = ocListener;
    }

    // </editor-fold>
}
