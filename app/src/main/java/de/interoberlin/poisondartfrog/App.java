package de.interoberlin.poisondartfrog;

import android.app.Application;
import android.content.Context;
import de.interoberlin.poisondartfrog.model.wunderbar.RelayrSdkInitializer;

public class App extends Application {
    // Context
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        RelayrSdkInitializer.initSdk(this);
    }

    // --------------------
    // Methods
    // --------------------

    public static Context getContext() {
        return context;
    }
}
