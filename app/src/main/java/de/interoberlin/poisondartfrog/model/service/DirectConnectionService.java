package de.interoberlin.poisondartfrog.model.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.google.gson.Gson;

import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.config.ECharacteristic;
import de.interoberlin.poisondartfrog.model.config.EDescriptor;
import de.interoberlin.poisondartfrog.model.config.EService;
import de.interoberlin.poisondartfrog.model.parser.BleDataParser;
import de.interoberlin.poisondartfrog.model.parser.DataPackage;
import de.interoberlin.poisondartfrog.model.service.error.CharacteristicNotFoundException;
import de.interoberlin.poisondartfrog.model.util.BleUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import static rx.Observable.error;

/**
 * A class representing the Direct Connection BLE Service.
 * The functionality and characteristics available when a device is in DIRECT_CONNECTION mode.
 */
public class DirectConnectionService extends BaseService {
    public static final String TAG = DirectConnectionService.class.getSimpleName();

    // --------------------
    // Constructors
    // --------------------

    public DirectConnectionService(BleDevice device, BluetoothGatt gatt, BluetoothGattReceiver receiver) {
        super(device, gatt, receiver);
    }

    // --------------------
    // Methods
    // --------------------

    public static Observable<DirectConnectionService> connect(final BleDevice bleDevice,
                                                              final BluetoothDevice device) {
        final BluetoothGattReceiver receiver = new BluetoothGattReceiver();
        return doConnect(device, receiver, false)
                .flatMap(new BondingReceiver.BondingFunc1())
                .map(new Func1<BluetoothGatt, DirectConnectionService>() {
                    @Override
                    public DirectConnectionService call(BluetoothGatt gatt) {
                        return new DirectConnectionService(bleDevice, gatt, receiver);
                    }
                });
    }

    /**
     * Reads a single value from a characteristic
     *
     * @param id uuid of the characteristic
     * @return observable containing reading
     */
    public Observable<Reading> read(String id) {
        BluetoothGattCharacteristic characteristic = BleUtils.getCharacteristicInServices(mBluetoothGatt.getServices(), EService.DIRECT_CONNECTION.getId(), id);

        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(id));
        }

        return mBluetoothGattReceiver
                .readCharacteristic(mBluetoothGatt, characteristic)
                .map(new Func1<BluetoothGattCharacteristic, String>() {
                    @Override
                    public String call(BluetoothGattCharacteristic characteristic) {
                        return BleDataParser.getFormattedValue(device.getType(), characteristic.getValue());
                    }
                })
                .flatMap(new Func1<String, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(final String s) {
                        return Observable.create(new Observable.OnSubscribe<Reading>() {
                            @Override
                            public void call(Subscriber<? super Reading> subscriber) {
                                DataPackage data = new Gson().fromJson(s, DataPackage.class);
                                for (DataPackage.Data dataPoint : data.readings) {
                                    subscriber.onNext(new Reading(data.received, dataPoint.recorded,
                                            dataPoint.meaning, dataPoint.path, dataPoint.value));
                                }
                            }
                        });
                    }
                });
    }

    public Observable<Reading> subscribe(String id) {
        BluetoothGattCharacteristic characteristic = BleUtils.getCharacteristicInServices(mBluetoothGatt.getServices(), EService.DIRECT_CONNECTION.getId(), id);

        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(id));
        }

        BluetoothGattDescriptor descriptor = BleUtils.getDescriptorInCharacteristic(
                characteristic, EDescriptor.DATA_NOTIFICATIONS.getId());
        return mBluetoothGattReceiver
                .subscribeToCharacteristicChanges(mBluetoothGatt, characteristic, descriptor)
                .map(new Func1<BluetoothGattCharacteristic, String>() {
                    @Override
                    public String call(BluetoothGattCharacteristic characteristic) {
                        return BleDataParser.getFormattedValue(device.getType(), characteristic.getValue());
                    }
                })
                .flatMap(new Func1<String, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(final String s) {
                        return Observable.create(new Observable.OnSubscribe<Reading>() {
                            @Override
                            public void call(Subscriber<? super Reading> subscriber) {
                                DataPackage data = new Gson().fromJson(s, DataPackage.class);
                                for (DataPackage.Data dataPoint : data.readings) {
                                    subscriber.onNext(new Reading(data.received, dataPoint.recorded,
                                            dataPoint.meaning, dataPoint.path, dataPoint.value));
                                }
                            }
                        });
                    }
                });
    }

    public Observable<BluetoothGattCharacteristic> stopSubscribing() {
        ECharacteristic c = ECharacteristic.DATA;
        EService s = EService.DIRECT_CONNECTION;

        BluetoothGattCharacteristic characteristic = BleUtils.getCharacteristicInServices(
                mBluetoothGatt.getServices(), s.getId(), c.getId());
        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(c.getId()));
        }
        BluetoothGattDescriptor descriptor = BleUtils.getDescriptorInCharacteristic(
                characteristic, EDescriptor.DATA_NOTIFICATIONS.getId());
        return mBluetoothGattReceiver
                .unsubscribeToCharacteristicChanges(mBluetoothGatt, characteristic, descriptor);
    }
}
