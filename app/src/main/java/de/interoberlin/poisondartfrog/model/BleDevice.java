package de.interoberlin.poisondartfrog.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.google.common.collect.EvictingQueue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.interoberlin.poisondartfrog.model.config.ECharacteristic;
import de.interoberlin.poisondartfrog.model.config.EDevice;
import de.interoberlin.poisondartfrog.model.config.EService;
import de.interoberlin.poisondartfrog.model.parser.BleDataParser;
import de.interoberlin.poisondartfrog.model.service.BaseService;
import de.interoberlin.poisondartfrog.model.service.BleDeviceManager;
import de.interoberlin.poisondartfrog.model.service.DirectConnectionService;
import de.interoberlin.poisondartfrog.model.service.Reading;
import de.interoberlin.poisondartfrog.model.util.BleUtils;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * A class representing a BLE Device
 */
public class BleDevice {
    public static final String TAG = BleDevice.class.getSimpleName();

    public static final int READING_HISTORY = 50;

    private OnChangeListener ocListener;
    private final BluetoothDevice device;
    private final String name;
    private final String address;
    private final EDevice type;

    private final BleDeviceManager deviceManager;
    private final Observable<? extends BaseService> serviceObservable;
    private BluetoothGatt gatt;

    private List<BluetoothGattService> services;
    private List<BluetoothGattCharacteristic> characteristics;

    private Map<String, Queue<Reading>> readings;
    private Map<String, Reading> latestReadings;
    private Map<ECharacteristic, Subscription> subscriptions;

    private boolean connected;
    private boolean reading;
    private boolean subscribing;
    private boolean notifyEnabled;

    // --------------------
    // Constructors
    // --------------------

    public BleDevice(BluetoothDevice device, BleDeviceManager manager) {
        this.device = device;
        this.name = device.getName();
        this.address = device.getAddress();
        this.type = EDevice.fromString(device.getName());

        this.deviceManager = manager;
        this.serviceObservable = DirectConnectionService.connect(this, device).cache();

        this.services = new ArrayList<>();
        this.characteristics = new ArrayList<>();
        this.readings = new HashMap<>();
        this.latestReadings = new HashMap<>();
        this.subscriptions = new HashMap<>();

        this.connected = false;
        this.reading = false;
        this.subscribing = false;
    }

    // --------------------
    // Methods
    // --------------------

    /**
     * Connects the device
     *
     * @return observable containing a base service
     */
    public Observable<? extends BaseService> connect() {
        Log.d(TAG, "Connect");

        setConnected(true);
        return serviceObservable;
    }

    /**
     * Disconnects the device
     *
     * @return observable containing the device
     */
    public Observable<BleDevice> disconnect() {
        Log.d(TAG, "Disconnect");

        setConnected(false);
        deviceManager.removeDevice(this);
        return serviceObservable
                .flatMap(new Func1<BaseService, Observable<BleDevice>>() {
                    @Override
                    public Observable<BleDevice> call(BaseService service) {
                        return service.disconnect();
                    }
                });
    }

    /**
     * Disconnects gatt connection and closes it
     */
    public void close() {
        Log.d(TAG, "Close");

        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
        }
    }

    /**
     * Reads a single value from a characteristic
     *
     * @param characteristic characteristic
     * @return subscription
     */
    public void read(final ECharacteristic characteristic) {
        Log.d(TAG, "Read " + characteristic.getId());

        connect()
                .flatMap(new Func1<BaseService, Observable<BluetoothGattCharacteristic>>() {
                    @Override
                    public Observable<BluetoothGattCharacteristic> call(BaseService baseService) {
                        return baseService.readCharacteristic(characteristic.getService().getId(), characteristic.getId(), "");
                    }
                }).subscribe(new Observer<BluetoothGattCharacteristic>() {
            @Override
            public void onCompleted() {
                reading = false;
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onNext(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                disconnect();

                String value = BleDataParser.getFormattedValue(null, characteristic, bluetoothGattCharacteristic.getValue());
                Log.d(TAG, "Read " + characteristic.getId() + " : " + value);
                setCharacteristicValue(characteristic.getId(), value);
            }
        });
    }

    /**
     * Subscribes to a characteristic with a given {@code id}
     *
     * @param characteristic characteristic
     * @return subscription
     */
    public Subscription subscribe(final ECharacteristic characteristic) {
        Log.d(TAG, "Subscribe " + characteristic.getId());

        Subscription subscription = connect()
                .flatMap(new Func1<BaseService, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(BaseService baseService) {
                        return ((DirectConnectionService) baseService).subscribe(characteristic);
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        disconnect();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Reading>() {
                    @Override
                    public void onCompleted() {
                        reading = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Reading reading) {
                        Queue<Reading> queue;
                        if (readings.containsKey(reading.meaning)) {
                            queue = readings.get(reading.meaning);
                        } else {
                            // Init queue and add dummy values
                            queue = EvictingQueue.create(READING_HISTORY);
                            for (int i = 0; i < READING_HISTORY; i++) {
                                queue.add(new Reading(0, 0, reading.meaning, reading.path, ""));
                            }
                        }

                        queue.add(reading);
                        readings.put(reading.meaning, queue);
                        latestReadings.put(reading.meaning, reading);

                        setCharacteristicValue(characteristic.getId(), reading.toString());
                    }
                });

        subscriptions.put(characteristic, subscription);
        setSubscribing(true);

        return subscription;
    }

    /**
     * Stops subscription to a characteristic with a given {@code id}
     *
     * @param characteristic characteristic
     */
    public void unsubscribe(ECharacteristic characteristic) {
        Log.d(TAG, "Unsubscribe " + characteristic.getId());

        Subscription subscription = subscriptions.get(characteristic);

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscriptions.remove(characteristic.getId());
        }

        setSubscribing(false);
    }

    public Subscription write(final EService service, final ECharacteristic characteristic, final String value) {
        Log.d(TAG, "Write " + characteristic.getId() + " : " + value);

        return write(service, characteristic, value.getBytes());
    }

    public Subscription write(final EService service, final ECharacteristic characteristic, final boolean value) {
        Log.d(TAG, "Write " + characteristic.getId() + " : " + bytesToHex(value ? new byte[]{0x01} : new byte[]{0x00}));

        return write(service, characteristic, value ? new byte[]{0x01} : new byte[]{0x00});
    }

    /*
     * Writes a single value to a characteristic
     *
     * @param serviceId uuid of the service
     * @param characteristicId uuid of the characteristic
     * @param value to send
     * @return subscription
     */
    public Subscription write(final EService service, final ECharacteristic characteristic, final byte[] value) {
        return connect()
                .flatMap(new Func1<BaseService, Observable<BluetoothGattCharacteristic>>() {
                    @Override
                    public Observable<BluetoothGattCharacteristic> call(BaseService baseService) {
                        return baseService.write(value, service.getId(), characteristic.getId());
                    }
                }).doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        disconnect();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BluetoothGattCharacteristic>() {
                    @Override
                    public void onCompleted() {
                        reading = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(BluetoothGattCharacteristic characteristic) {
                        Log.d(TAG, characteristic.getUuid() + " " + characteristic.getValue());
                    }
                });
    }

    /**
     * Notifies a characteristic
     *
     * @param service        service
     * @param characteristic characteristic
     * @param enabled        true if notification shall be enabled
     */
    public boolean sendNotify(final EService service, final ECharacteristic characteristic, final boolean enabled) {
        BluetoothGattCharacteristic c = BleUtils.getCharacteristicInServices(gatt.getServices(), service.getId(), characteristic.getId());
        BluetoothGattDescriptor descriptor = c.getDescriptors().get(0);
        return descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
    }

    /**
     * Clears ble cache
     */
    public void refreshCache() {
        try {
            if (gatt != null) {
                Method localMethod = gatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    ocListener.onCacheCleared(((Boolean) localMethod.invoke(gatt, new Object[0])).booleanValue());
                }
            }
        } catch (Exception localException) {
            Log.e(TAG, "An exception occured while refreshing device");
        }

        ocListener.onCacheCleared(false);
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(this.getName()).append(", \n");
        sb.append("type=").append(this.getType()).append(", \n");
        sb.append("address=").append(this.getAddress()).append(", \n");
        sb.append("services=\n");

        for (BluetoothGattService service : getServices()) {
            sb.append("  service ").append(service.getUuid().toString().substring(4, 8)).append("\n");
            for (BluetoothGattCharacteristic chara : service.getCharacteristics()) {
                sb.append("  characteristic ").append(chara.getUuid().toString().substring(4, 8)).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleDevice bleDevice = (BleDevice) o;

        if (type != bleDevice.type) return false;
        if (address != null ? !address.equals(bleDevice.address) : bleDevice.address != null)
            return false;
        return !(name != null ? !name.equals(bleDevice.name) : bleDevice.name != null);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public boolean containsCharacteristic(ECharacteristic characteristic) {
        for (BluetoothGattCharacteristic c : getCharacteristics()) {
            if (c.getUuid().toString().equals(characteristic.getId())) {
                return true;
            }
        }

        return false;
    }

    public BluetoothGattCharacteristic getCharacteristic(ECharacteristic characteristic) {
        for (BluetoothGattCharacteristic c : getCharacteristics()) {
            if (c.getUuid().toString().equals(characteristic.getId())) {
                return c;
            }
        }

        return null;
    }

    public void setCharacteristicValue(String id, String value) {
        for (BluetoothGattCharacteristic c : getCharacteristics()) {
            if (c.getUuid().toString().contains(id)) {
                c.setValue(value);
                if (ocListener != null) ocListener.onChange(BleDevice.this);
            }
        }
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public List<BluetoothGattService> getServices() {
        return services;
    }

    public void setServices(List<BluetoothGattService> services) {
        this.services = services;
    }

    public List<BluetoothGattCharacteristic> getCharacteristics() {
        if (this.characteristics == null || this.characteristics.isEmpty())
            initCharacteristics();

        return characteristics;
    }

    private void initCharacteristics() {
        List<BluetoothGattCharacteristic> characteristics = new ArrayList<>();
        for (BluetoothGattService service : getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                characteristics.add(characteristic);
            }
        }

        this.characteristics = characteristics;
    }

    public void setCharacteristics(List<BluetoothGattCharacteristic> characteristics) {
        this.characteristics = characteristics;
    }

    public Map<String, Queue<Reading>> getReadings() {
        return readings;
    }

    public Map<String, Reading> getLatestReadings() {
        return latestReadings;
    }

    public Map<ECharacteristic, Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setValues(Map<String, Queue<Reading>> readings) {
        this.readings = readings;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isReading() {
        return reading;
    }

    public void setReading(boolean reading) {
        this.reading = reading;
    }

    public boolean isSubscribing() {
        return subscribing;
    }

    public void setSubscribing(boolean subscribing) {
        this.subscribing = subscribing;
    }

    public boolean isNotifyEnabled() {
        return notifyEnabled;
    }

    public void setNotifyEnabled(boolean notifyEnabled) {
        this.notifyEnabled = notifyEnabled;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public EDevice getType() {
        return type;
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnChangeListener {
        void onChange(BleDevice device);

        void onCacheCleared(boolean success);
    }

    public void registerOnChangeListener(OnChangeListener ocListener) {
        this.ocListener = ocListener;
    }
}
