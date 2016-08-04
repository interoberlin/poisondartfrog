package de.interoberlin.poisondartfrog.controller;

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

import de.interoberlin.mate.lib.model.Log;
import de.interoberlin.poisondartfrog.App;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.mapping.Mapping;

public class MappingController {
    // <editor-fold defaultstate="expanded" desc="Members">

    public static final String TAG = MappingController.class.getSimpleName();

    // Model
    private Map<String, Mapping> existingMappings;
    private Map<String, Mapping> activeMappings;

    // Controller
    private DevicesController devicesController;

    private static MappingController instance;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Constructors">

    private MappingController() {
        this.existingMappings = loadMappingsFromAssets(App.getContext());
        this.activeMappings = new HashMap<>();

        this.devicesController = DevicesController.getInstance();
    }

    public static MappingController getInstance() {
        if (instance == null) {
            instance = new MappingController();
        }

        return instance;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Methods">

    /**
     * Loads all mapping files from assets and adds them to the list
     *
     * @param context context
     * @return list of mappings
     */
    private Map<String, Mapping> loadMappingsFromAssets(Context context) {
        Map<String, Mapping> mappings = new HashMap<>();

        try {
            for (String asset : Arrays.asList(context.getAssets().list(""))) {
                if (asset != null) {
                    if (asset.endsWith(context.getResources().getString(R.string.merlot_mapping_file_exception))) {
                        InputStream inputStream = context.getAssets().open(asset);
                        Mapping m = new Gson().fromJson(IOUtils.toString(inputStream, "UTF-8"), Mapping.class);
                        if (m != null) {
                            Log.i(TAG, "Loaded mapping " + m.getName());
                            mappings.put(m.getName(), m);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

        return mappings;
    }

    public boolean activateMapping(Mapping.OnChangeListener ocListener, Mapping mapping) {
        Log.d(TAG, "Activate " + mapping.getName());

        if (mapping != null) {
            if (existingMappings.containsKey(mapping.getName()))
                existingMappings.remove(mapping.getName());
            activeMappings.put(mapping.getName(), mapping);

            mapping.registerOnChangeListener(ocListener);

            checkSource(mapping);
            checkSink(mapping);

            return true;
        }

        return false;
    }

    public boolean deactivateMapping(Mapping mapping) {
        android.util.Log.d(TAG, "Deactivate" + mapping.getName());

        if (mapping != null) {

            if (activeMappings.containsKey(mapping.getName())) {
                activeMappings.remove(mapping.getName());
                existingMappings.put(mapping.getName(), mapping);
            }

            return true;
        }

        return false;
    }

    public void checkSourceAndSink() {
        Log.d(TAG, "checkSourceAndSink");

        for(Map.Entry<String, Mapping> e : activeMappings.entrySet()) {
            checkSource(e.getValue());
            checkSink(e.getValue());
        }
    }

    private void checkSource(Mapping mapping) {
        String source = mapping.getSource();

        if (source != null) {
            mapping.setSourceAttached(devicesController.getAttachedDevices().containsKey(source));
        }
    }

    private void checkSink(Mapping mapping) {
        String sink = mapping.getSink();

        if (sink != null) {
            mapping.setSinkAttached(devicesController.getAttachedDevices().containsKey(sink));
        }
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Getters / Setters">

    public Map<String, Mapping> getExistingMappings() {
        return existingMappings;
    }

    public Map<String, Mapping> getActiveMappings() {
        return activeMappings;
    }

    public List<String> getExistingMappingNames() {
        List<String> mappingNames = new ArrayList<>();

        for (Map.Entry<String, Mapping> m : getExistingMappings().entrySet()) {
            mappingNames.add(m.getKey());
        }

        return mappingNames;
    }

    public Mapping getExistingMappingByName(String name) {
        return getExistingMappings().get(name);
    }

    public Mapping getActiveMappingByName(String name) {
        return getActiveMappings().get(name);
    }

    public List<Mapping> getActiveMappingsAsList() {
        return new ArrayList<>(getActiveMappings().values());
    }

    // </editor-fold>
}
