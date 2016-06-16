package de.interoberlin.poisondartfrog.model.config;

import de.interoberlin.poisondartfrog.model.config.repository.RepositoryMapper;

public enum ECharacteristic {
    DEVICE_NAME("00002a00-0000-1000-8000-00805f9b34fb"),
    APPEARANCE("00002a01-0000-1000-8000-00805f9b34fb"),
    PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS("00002a04-0000-1000-8000-00805f9b34fb"),
    RECONNECTION_ADDRESS("00002a03-0000-1000-8000-00805f9b34fb"),

    SERVICE_CHANGED("00002a05-0000-1000-8000-00805f9b34fb"),

    MANUFACTURER_NAME("00002a29-0000-1000-8000-00805f9b34fb"),
    HARDWARE_REVISION("00002a27-0000-1000-8000-00805f9b34fb"),
    FIRMWARE_REVISION("00002a26-0000-1000-8000-00805f9b34fb"),

    BATTERY_LEVEL("00002a19-0000-1000-8000-00805f9b34fb"),

    SERVICE_ONBOARDING("00002001-0000-1000-8000-00805f9b34fb"),
    SENSOR_ID("00002010-0000-1000-8000-00805f9b34fb"),
    PASS_KEY("00002018-0000-1000-8000-00805f9b34fb"),
    ONBOARDING_FLAG("00002019-0000-1000-8000-00805f9b34fb"),

    BEACON_FREQUENCY("00002011-0000-1000-8000-00805f9b34fb"),
    FREQUENCY("00002012-0000-1000-8000-00805f9b34fb"),
    LED_STATE("00002013-0000-1000-8000-00805f9b34fb"),
    THRESHOLD("00002014-0000-1000-8000-00805f9b34fb"),
    CONFIGURATION("00002015-0000-1000-8000-00805f9b34fb"),
    DATA("00002016-0000-1000-8000-00805f9b34fb"),
    SEND_COMMAND("00002017-0000-1000-8000-00805f9b34fb"),

    LED_COLOR("00003001-0000-1000-8000-00805f9b34fb"),

    HEART_RATE_MEASUREMENT("00002a37-0000-1000-8000-00805f9b34fb"),
    ;

    private String id;

    // --------------------
    // Constructor
    // --------------------

    ECharacteristic(String id) {
        this.id = id;
    }

    // --------------------
    // Methods
    // --------------------

    public static ECharacteristic fromId(String id) {
        for (ECharacteristic c : ECharacteristic.values()) {
            if (c.getId().equals(id))
                return c;
        }

        return null;
    }

    public EFormat getFormat() {
        if (RepositoryMapper.getInstance().isKnownCharacteristic(id)) {
            return RepositoryMapper.getInstance().getCharacteristicById(id).getFormat();
        }

        return null;
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
