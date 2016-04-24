package de.interoberlin.poisondartfrog.model.config;


public enum EDescriptor {
    DATA_NOTIFICATIONS("00002902-0000-1000-8000-00805f9b34fb");

    private String id;

    // --------------------
    // Constructor
    // --------------------

    EDescriptor(String id) {
        this.id = id;
    }

    // --------------------
    // Methods
    // --------------------

    public static EDescriptor fromId(String id) {
        for (EDescriptor s : EDescriptor.values()) {
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
