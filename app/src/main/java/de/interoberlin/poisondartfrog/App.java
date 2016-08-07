package de.interoberlin.poisondartfrog;

import android.app.Application;
import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class App extends Application {
    // <editor-fold defaultstate="collapsed" desc="Members">

    // Context
    private static Context context;

    // </editor-fold>

    // --------------------
    // Methods - Callbacks
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Callbacks">

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public static Context getContext() {
        return context;
    }

    // </editor-fold>
}
