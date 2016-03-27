package de.interoberlin.poisondartfrog.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.interoberlin.poisondartfrog.model.config.Characteristic;
import de.interoberlin.poisondartfrog.model.config.PropertyMapper;
import de.interoberlin.poisondartfrog.model.parser.RelayrDataParser;
import de.interoberlin.poisondartfrog.model.service.BaseService;
import de.interoberlin.poisondartfrog.model.service.BleDeviceManager;
import de.interoberlin.poisondartfrog.model.service.DirectConnectionService;
import de.interoberlin.poisondartfrog.model.service.Reading;
import de.interoberlin.poisondartfrog.model.tasks.ReadCharacteristicTask;
import de.interoberlin.poisondartfrog.model.tasks.SubscribeCharacteristicTask;
import de.interoberlin.poisondartfrog.model.util.DeviceCompatibilityUtils;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * A class representing a relayr BLE Device
 */
public class BleDevice {
    public static final String TAG = BleDevice.class.getSimpleName();

    private final BluetoothDevice device;
    private final String name;
    private final String address;
    private final EBluetoothDeviceType type;

    private int rssi;
    private final BleDeviceManager deviceManager;
    private final Observable<? extends BaseService> serviceObservable;

    private List<BluetoothGattService> services;
    private List<BluetoothGattCharacteristic> characteristics;

    private boolean connected;
    private boolean reading;
    private boolean subscribing;
    private boolean scanning;

    private ReadCharacteristicTask readCharacteristicTask;
    private SubscribeCharacteristicTask subscribeCharacteristicTask;

    private int lastReadCharacteristic;

    // --------------------
    // Constructors
    // --------------------

    public BleDevice(BluetoothDevice device, BleDeviceManager manager) {
        this(device, manager, 0);
    }

    public BleDevice(BluetoothDevice device, BleDeviceManager manager, int rssi) {
        this.device = device;
        this.name = device.getName();
        this.address = device.getAddress();
        this.type = EBluetoothDeviceType.fromString(device.getName());

        this.rssi = rssi;
        this.deviceManager = manager;
        this.serviceObservable = DirectConnectionService.connect(this, device).cache();

        this.services = new ArrayList<>();
        this.reading = false;
        this.connected = false;
    }

    // --------------------
    // Methods
    // --------------------

    public Observable<? extends BaseService> connect() {
        return serviceObservable;
    }

    public Observable<BleDevice> disconnect() {
        deviceManager.removeDevice(this);
        return serviceObservable
                .flatMap(new Func1<BaseService, Observable<BleDevice>>() {
                    @Override
                    public Observable<BleDevice> call(BaseService service) {
                        return service.disconnect();
                    }
                });
    }

    public void refreshGatt() {
        serviceObservable
                .flatMap(new Func1<BaseService, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(final BaseService service) {
                        return Observable.create(new Observable.OnSubscribe<Boolean>() {
                            @Override
                            public void call(Subscriber<? super Boolean> subscriber) {
                                service.getGatt().disconnect();
                                service.getGatt().close();
                                DeviceCompatibilityUtils.refresh(service.getGatt());
                            }
                        });
                    }
                })
                .subscribe();
    }

    public Subscription subscribe() {
        return connect()
                .flatMap(new Func1<BaseService, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(BaseService baseService) {
                        return ((DirectConnectionService) baseService).getReadings();
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
                        Log.i(TAG, reading.toString());
                    }
                });
    }

    public void readNextCharacteristic(BluetoothLeService service) {
        setReading(true);

        if (getCharacteristics() != null && !getCharacteristics().isEmpty()) {
            BluetoothGattCharacteristic characteristic = getCharacteristics().get(lastReadCharacteristic);
            Characteristic c = PropertyMapper.getInstance().getCharacteristicById(characteristic.getUuid());

            Log.d(TAG, c.getId() + " " + c.getName());

            if (c != null && c.getRead() != null) {
                switch (c.getRead()) {
                    case NEVER:
                    case SUBSCRIBE: {
                        int index = getLastReadCharacteristic() + 1;
                        int total = getCharacteristics().size();
                        Log.v(TAG, "Skip [" + ((index < 10) ? " " : "") + index + "/" + total + "]");

                        incrementLastReadCharacteristic();
                        readNextCharacteristic(service);
                        break;
                    }
                    case ONCE:
                    default: {
                        if (characteristic.getValue() == null || characteristic.getValue().length == 0) {
                            readCharacteristicTask = new ReadCharacteristicTask(service);
                            readCharacteristicTask.execute(getCharacteristics().get(lastReadCharacteristic));
                        } else {
                            int index = getLastReadCharacteristic() + 1;
                            int total = getCharacteristics().size();
                            Log.v(TAG, "Skip [" + ((index < 10) ? " " : "") + index + "/" + total + "]");

                            incrementLastReadCharacteristic();
                            readNextCharacteristic(service);
                        }
                        break;
                    }
                }
            } else {
                if (characteristic.getValue() == null || characteristic.getValue().length == 0) {
                    readCharacteristicTask = new ReadCharacteristicTask(service);
                    readCharacteristicTask.execute(getCharacteristics().get(lastReadCharacteristic));
                } else {
                    int index = getLastReadCharacteristic() + 1;
                    int total = getCharacteristics().size();
                    Log.v(TAG, "Skip [" + ((index < 10) ? " " : "") + index + "/" + total + "]");

                    incrementLastReadCharacteristic();
                    readNextCharacteristic(service);
                }
            }
        }
    }

    public void stopReading() {
        setReading(false);

        if (readCharacteristicTask != null)
            readCharacteristicTask.cancel(true);
    }

    /**
     * Clears values of all characteristics
     */
    public void clearValues() {
        for (BluetoothGattService s : services) {
            for (BluetoothGattCharacteristic c : s.getCharacteristics()) {
                c.setValue("");
            }
        }
    }

    /**
     * Updates the {@code value} of a characteristic defined by a given {@code id}
     *
     * @param id    characteristic id
     * @param value new value
     */
    public void updateCharacteristicValue(String id, String value) {
        for (BluetoothGattService service : getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().equals(id)) {
                    characteristic.setValue(value);
                }
            }
        }
    }

    public void incrementLastReadCharacteristic() {
        lastReadCharacteristic++;
        if (lastReadCharacteristic >= getCharacteristics().size())
            lastReadCharacteristic = 0;
    }

    /**
     * Retrieves a value of a given {@code characteristic}
     *
     * @param characteristic characteristic
     * @param device         device
     * @return value string
     */
    public static String parseValue(BluetoothDevice device, BluetoothGattCharacteristic characteristic) {
        UUID id = characteristic.getUuid();

        if (PropertyMapper.getInstance().isKnownCharacteristic(id) && PropertyMapper.getInstance().getCharacteristicById(id).getFormat() != null) {
            Characteristic.EFormat format = PropertyMapper.getInstance().getCharacteristicById(id).getFormat();
            String value = "";

            switch (format) {
                case STRING:
                    value = getStringValue(characteristic.getValue());
                    break;
                case UINT8:
                    value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                    break;
                case UINT16:
                    value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
                    break;
                case UINT32:
                    value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0));
                    break;
                case SINT8:
                    value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0));
                    break;
                case SINT16:
                    value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                    break;
                case SINT32:
                    value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0));
                    break;
                case SFLOAT:
                    value = String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, 0));
                    break;
                case FLOAT:
                    value = String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 0));
                    break;
                case RELAYR:
                    value = RelayrDataParser.getFormattedValue(EBluetoothDeviceType.fromString(device.getName()), characteristic.getValue());
                    value = value.replaceAll(",", ",\n");
                    break;
            }

            return value;
        } else {
            return new String(characteristic.getValue()).replaceAll(" ", "");
        }
    }

    /**
     * Turns a byte array into a corresponding ASCII string
     *
     * @param value byte array
     * @return ASCII string
     */
    private static String getStringValue(byte[] value) {
        StringBuilder sb = new StringBuilder();
        String v = new String(value).replaceAll(" ", "");

        try {
            for (int i = 0; i < v.length(); i += 2) {
                String s = v.substring(i, i + 2);
                sb.append((char) Integer.parseInt(s, 16));
            }
            return sb.toString();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return v;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(this.getName()).append(", \n");
        sb.append("type=").append(this.getType()).append("), \n");
        sb.append("address=").append(this.getAddress()).append(", \n");
        sb.append("services=\n");

        for (BluetoothGattService service : getServices()) {
            sb.append("  service ").append(service.getUuid()).append("\n");
            for (BluetoothGattCharacteristic chara : service.getCharacteristics()) {
                sb.append("  characteristic ").append(chara.getUuid()).append((chara.getValue() != null) ? " / " + parseValue(device, chara) : "").append("\n");
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

    // --------------------
    // Getters / Setters
    // --------------------

    public List<BluetoothGattService> getServices() {
        return services;
    }

    public void setServices(List<BluetoothGattService> services) {
        this.services = services;
        this.characteristics = new ArrayList<>();

        for (BluetoothGattService s : services) {
            for (BluetoothGattCharacteristic c : s.getCharacteristics()) {
                characteristics.add(c);
            }
        }
    }

    public List<BluetoothGattCharacteristic> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<BluetoothGattCharacteristic> characteristics) {
        this.characteristics = characteristics;
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

    public boolean isScanning() {
        return scanning;
    }

    public void setScanning(boolean scanning) {
        this.scanning = scanning;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public EBluetoothDeviceType getType() {
        return type;
    }

    public int getLastReadCharacteristic() {
        return lastReadCharacteristic;
    }

    public void setLastReadCharacteristic(int lastReadCharacteristic) {
        this.lastReadCharacteristic = lastReadCharacteristic;
    }
}
