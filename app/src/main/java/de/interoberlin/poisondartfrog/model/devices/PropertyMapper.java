package de.interoberlin.poisondartfrog.model.devices;


import android.content.Context;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.interoberlin.poisondartfrog.App;
import de.interoberlin.poisondartfrog.view.components.ServicesComponent;

public class PropertyMapper {
    private static final String TAG = ServicesComponent.class.getSimpleName();

    private List<Device> devices;
    private Map<String, Object> idMap;

    private static PropertyMapper instance;

    // --------------------
    // Constructors
    // --------------------

    private PropertyMapper() {
        devices = getKnownDevices();
        idMap = createIdMap(devices);
    }

    public static PropertyMapper getInstance() {
        if (instance == null) {
            instance = new PropertyMapper();
        }

        return instance;
    }

    // --------------------
    // Methods
    // --------------------

    public Object getObjectById(String id) {
        return idMap.get(id);
    }

    public boolean isKnownService(UUID id) {
        return isKnownService(id.toString());
    }

    private boolean isKnownService(String id) {
        return idMap.containsKey(id) && idMap.get(id) instanceof Service;
    }

    public boolean isKnownCharacteristic(UUID id) {
        return isKnownCharacteristic(id.toString());
    }

    private boolean isKnownCharacteristic(String id) {
        return idMap.containsKey(id) && idMap.get(id) instanceof Characteristic;
    }

    public Service getServiceById(UUID id) {
        return getServiceById(id.toString());
    }

    private Service getServiceById(String id) {
        return (isKnownService(id)) ? (Service) getObjectById(id) : null;
    }

    public Characteristic getCharacteristicById(UUID id) {
        return getCharacteristicById(id.toString());
    }

    private Characteristic getCharacteristicById(String id) {
        return (isKnownCharacteristic(id)) ? (Characteristic) getObjectById(id) : null;
    }

    /**
     * Reads all device specifications from json files
     *
     * @return list of devices
     */
    private List<Device> getKnownDevices() {
        Context context = App.getContext();
        List<Device> devices = new ArrayList<>();

        try {
            for (String assetName : Arrays.asList(context.getAssets().list(""))) {
                if (assetName != null && assetName.endsWith("json")) {
                    InputStream inputStream = context.getAssets().open(assetName);
                    String content = IOUtils.toString(inputStream, "UTF-8");

                    Device d = new Gson().fromJson(content, Device.class);

                    if (d != null) devices.add(d);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return devices;
    }

    /**
     * Iterates through all known devices and their services and characteristics and stores all
     *
     * @param devices list of devies
     * @return map of objects
     */
    private Map<String, Object> createIdMap(List<Device> devices) {
        Map<String, Object> idMap = new HashMap<>();

        for (Device d : devices) {
            if (d != null) {
                for (Service s : d.getServices()) {
                    if (s != null) {
                        idMap.put(s.getId(), s);
                        for (Characteristic c : s.getCharacteristics()) {
                            if (c != null) {
                                idMap.put(c.getId(), c);
                            }
                        }
                    }
                }
            }
        }

        return idMap;
    }
}
