package de.interoberlin.poisondartfrog.model.config;

public enum EService {
    GENERIC_ACCESS("00001800-0000-1000-8000-00805f9b34fb"),
    GENERIC_ATTRIBUTE("00001801-0000-1000-8000-00805f9b34fb"),
    DEVICE_INFORMATION("0000180a-0000-1000-8000-00805f9b34fb"),
    BATTERY_LEVEL("0000180f-0000-1000-8000-00805f9b34fb"),

    CONNECTED_TO_MASTER_MODULE("00002000-0000-1000-8000-00805f9b34fb"),
    DIRECT_CONNECTION("00002002-0000-1000-8000-00805f9b34fb"),

    HEART_RATE("0000180d-0000-1000-8000-00805f9b34fb"),

    NORDIC_UART("6E400001-B5A3-F393-E0A9-E50E24DCCA9E"),

    SENTIENT_LIGHT("00003000-0000-1000-8000-00805f9b34fb");

    private String id;

    // --------------------
    // Constructor
    // --------------------

    EService(String id) {
        this.id = id;
    }

    // --------------------
    // Methods
    // --------------------

    public static EService fromId(String id) {
        for (EService s : EService.values()) {
            if (s.getId().equals(id))
                return s;
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
