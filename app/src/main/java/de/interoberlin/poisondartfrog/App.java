package de.interoberlin.poisondartfrog;

import android.app.Application;
import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class App extends Application {
    // Context
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);
    }

    // --------------------
    // Methods
    // --------------------

    public static Context getContext() {
        return context;
    }
}
