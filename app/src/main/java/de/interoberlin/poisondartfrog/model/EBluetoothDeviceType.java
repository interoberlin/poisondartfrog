package de.interoberlin.poisondartfrog.model;

public enum EBluetoothDeviceType {
    WUNDERBAR_HTU("WunderbarHTU"),
    WUNDERBAR_GYRO("WunderbarGYRO"),
    WUNDERBAR_LIGHT("WunderbarLIGHT"),
    WUNDERBAR_MIC("WunderbarMIC"),
    WUNDERBAR_BRIDG("WunderbarBRIDG"),
    WUNDERBAR_IR("WunderbarIR");

    private final String name;

    EBluetoothDeviceType(String name) {
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

    public String getName() {
        return name;
    }
}
