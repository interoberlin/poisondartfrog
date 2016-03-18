package de.interoberlin.poisondartfrog.model;

public enum EBluetoothDeviceType {
    WUNDERBAR_HTU("ecf6cf94-cb07-43ac-a85e-dccf26b48c86", "WunderbarHTU"),
    WUNDERBAR_GYRO("173c44b5-334e-493f-8eb8-82c8cc65d29f","WunderbarGYRO"),
    WUNDERBAR_LIGHT("a7ec1b21-8582-4304-b1cf-15a1fc66d1e8","WunderbarLIGHT"),
    WUNDERBAR_MIC("4f38b6c6-a8e9-4f93-91cd-2ac4064b7b5a","WunderbarMIC"),
    WUNDERBAR_BRIDG("ebd828dd-250c-4baf-807d-69d85bed065b","WunderbarBRIDG"),
    WUNDERBAR_IR("bab45b9c-1c44-4e71-8e98-a321c658df47","WunderbarIR");

    private final String id;
    private final String name;

    EBluetoothDeviceType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // --------------------
    // Methods
    // --------------------


    public static EBluetoothDeviceType fromString(String name) {
        if (name != null) {
            for (EBluetoothDeviceType b : EBluetoothDeviceType.values()) {
                if (name.equalsIgnoreCase(b.getName())) {
                    return b;
                }
            }
        }

        return null;
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}