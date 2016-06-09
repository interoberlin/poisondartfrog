package de.interoberlin.poisondartfrog.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.interoberlin.poisondartfrog.model.BleDevice;
import rx.Subscriber;

public class BleDeviceManager {

    private final Map<String, BleDevice> mDiscoveredDevices = new ConcurrentHashMap<>();
    private final Map<Long, Subscriber<? super List<BleDevice>>> mDevicesSubscriberMap = new ConcurrentHashMap<>();

    private static BleDeviceManager instance;

    // --------------------
    // Constructors
    // --------------------

    private BleDeviceManager() {
    }

    public static BleDeviceManager getInstance() {
        if (instance == null) {
            instance = new BleDeviceManager();
        }

        return instance;
    }

    // --------------------
    // Methods
    // --------------------

    public void addSubscriber(Long key, Subscriber<? super List<BleDevice>> devicesSubscriber) {
        mDevicesSubscriberMap.put(key, devicesSubscriber);
        if (!mDiscoveredDevices.isEmpty()) devicesSubscriber.onNext(getDiscoveredDevices());
    }

    //TODO EXTREMELY IMPORTANT METHOD
    void addDiscoveredDevice(BleDevice device) {
        mDiscoveredDevices.remove(device.getAddress());
        mDiscoveredDevices.put(device.getAddress(), device);

        for (Subscriber<? super List<BleDevice>> mDevicesSubscriber : mDevicesSubscriberMap.values())
            mDevicesSubscriber.onNext(getDiscoveredDevices());
    }

    boolean isDeviceDiscovered(String address) {
        return mDiscoveredDevices.containsKey(address);
    }

    boolean isDeviceDiscovered(BleDevice device) {
        return isDeviceDiscovered(device.getAddress());
    }

    void clear() {
        mDiscoveredDevices.clear();
    }

    List<BleDevice> getDiscoveredDevices() {
        return new ArrayList<>(mDiscoveredDevices.values());
    }

    public void removeDevice(BleDevice device) {
        mDiscoveredDevices.remove(device.getAddress());
    }

    public void removeSubscriber(Long key) {
        mDevicesSubscriberMap.remove(key);
    }

    public boolean isThereAnySubscriber() {
        return !mDevicesSubscriberMap.isEmpty();
    }
}
