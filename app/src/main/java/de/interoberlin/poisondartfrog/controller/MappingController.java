package de.interoberlin.poisondartfrog.controller;

import android.content.Context;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.interoberlin.mate.lib.model.Log;
import de.interoberlin.poisondartfrog.App;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.mapping.Mapping;

public class MappingController {
    // <editor-fold defaultstate="expanded" desc="Members">

    public static final String TAG = MappingController.class.getSimpleName();

    private List<Mapping> mappings;

    private static MappingController instance;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Constructors">

    private MappingController() {
        this.mappings = loadMappingsFromAssets(App.getContext());
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
    private List<Mapping> loadMappingsFromAssets(Context context) {
        List<Mapping> mappings = new ArrayList<>();

        try {
            for (String asset : Arrays.asList(context.getAssets().list(""))) {
                if (asset != null) {
                    if (asset.endsWith(context.getResources().getString(R.string.merlot_mapping_file_exception))) {
                        InputStream inputStream = context.getAssets().open(asset);
                        Mapping m = new Gson().fromJson(IOUtils.toString(inputStream, "UTF-8"), Mapping.class);
                        if (m != null) {
                            Log.i(TAG, "Loaded mapping " + m.getName());
                            mappings.add(m);
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

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Getters / Setters">

    public List<Mapping> getMappings() {
        return mappings;
    }

    /*
    public void setMappings(List<Mapping> mappings) {
        this.mappings = mappings;
    }
    */

    public List<String> getMappingNames() {
        List<String> mappingNames = new ArrayList<>();

        for (Mapping m : getMappings()) {
            mappingNames.add(m.getName());
        }

        return mappingNames;
    }

    // </editor-fold>
}
