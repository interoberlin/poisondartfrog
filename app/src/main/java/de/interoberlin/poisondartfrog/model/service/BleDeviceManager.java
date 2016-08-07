package de.interoberlin.poisondartfrog.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.interoberlin.poisondartfrog.model.ble.BleDevice;
import rx.Subscriber;

public class BleDeviceManager {
    // <editor-fold defaultstate="collapsed" desc="Members">

    private final Map<String, BleDevice> discoveredDevices = new ConcurrentHashMap<>();
    private final Map<Long, Subscriber<? super List<BleDevice>>> devicesSubscriberMap = new ConcurrentHashMap<>();

    private static BleDeviceManager instance;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    private BleDeviceManager() {
    }

    public static BleDeviceManager getInstance() {
        if (instance == null) {
            instance = new BleDeviceManager();
        }

        return instance;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public void addSubscriber(Long key, Subscriber<? super List<BleDevice>> devicesSubscriber) {
        devicesSubscriberMap.put(key, devicesSubscriber);
        if (!discoveredDevices.isEmpty()) devicesSubscriber.onNext(getDiscoveredDevices());
    }

    // TODO EXTREMELY IMPORTANT METHOD
    void addDiscoveredDevice(BleDevice device) {
        discoveredDevices.remove(device.getAddress());
        discoveredDevices.put(device.getAddress(), device);

        for (Subscriber<? super List<BleDevice>> devicesSubscriber : devicesSubscriberMap.values())
            devicesSubscriber.onNext(getDiscoveredDevices());
    }

    void clear() {
        discoveredDevices.clear();
    }

    public void removeDevice(BleDevice device) {
        discoveredDevices.remove(device.getAddress());
    }

    public void removeSubscriber(Long key) {
        devicesSubscriberMap.remove(key);
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setter">

    boolean isDeviceDiscovered(String address) {
        return discoveredDevices.containsKey(address);
    }

    boolean isDeviceDiscovered(BleDevice device) {
        return isDeviceDiscovered(device.getAddress());
    }

    List<BleDevice> getDiscoveredDevices() {
        return new ArrayList<>(discoveredDevices.values());
    }

    public boolean isThereAnySubscriber() {
        return !devicesSubscriberMap.isEmpty();
    }

    // </editor-fold>
}
