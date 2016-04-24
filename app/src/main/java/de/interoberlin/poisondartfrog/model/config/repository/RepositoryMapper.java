package de.interoberlin.poisondartfrog.model.config.repository;


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

import de.interoberlin.poisondartfrog.App;
import de.interoberlin.poisondartfrog.view.components.ServicesComponent;

public class RepositoryMapper {
    private static final String TAG = ServicesComponent.class.getSimpleName();

    private List<Namespace> namespaces;
    private Map<String, Object> idMap;

    private static RepositoryMapper instance;

    // --------------------
    // Constructors
    // --------------------

    private RepositoryMapper() {
        namespaces = getNamespaces();
        idMap = createIdMap(namespaces);
    }

    public static RepositoryMapper getInstance() {
        if (instance == null) {
            instance = new RepositoryMapper();
        }

        return instance;
    }

    // --------------------
    // Methods
    // --------------------

    public Object getObjectById(String id) {
        return idMap.get(id);
    }

    public boolean isKnownService(String id) {
        return idMap.containsKey(id) && idMap.get(id) instanceof Service;
    }

    public boolean isKnownCharacteristic(String id) {
        return idMap.containsKey(id) && idMap.get(id) instanceof Characteristic;
    }

    public Service getServiceById(String id) {
        return (isKnownService(id)) ? (Service) getObjectById(id) : null;
    }

    public Characteristic getCharacteristicById(String id) {
        return (isKnownCharacteristic(id)) ? (Characteristic) getObjectById(id) : null;
    }

    /**
     * Reads all device specifications from json files
     *
     * @return list of devices
     */
    private List<Namespace> getNamespaces() {
        Context context = App.getContext();
        List<Namespace> namespaces = new ArrayList<>();

        try {
            for (String assetName : Arrays.asList(context.getAssets().list(""))) {
                if (assetName != null && assetName.endsWith("json")) {
                    InputStream inputStream = context.getAssets().open(assetName);
                    String content = IOUtils.toString(inputStream, "UTF-8");

                    Namespace d = new Gson().fromJson(content, Namespace.class);

                    if (d != null) namespaces.add(d);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return namespaces;
    }

    /**
     * Iterates through all known devices and their services and characteristics and stores all
     *
     * @param namespaces list of devies
     * @return map of objects
     */
    private Map<String, Object> createIdMap(List<Namespace> namespaces) {
        Map<String, Object> idMap = new HashMap<>();

        for (Namespace d : namespaces) {
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
