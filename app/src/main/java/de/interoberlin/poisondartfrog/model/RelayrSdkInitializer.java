package de.interoberlin.poisondartfrog.model;

import android.content.Context;

import io.relayr.android.RelayrSdk;

public abstract class RelayrSdkInitializer {
    /**
     * Initializes the relayr SDK
     *
     * @param context context
     */
    public static void initSdk(Context context) {
        new RelayrSdk.Builder(context).build();
    }
}
