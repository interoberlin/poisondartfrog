package de.interoberlin.poisondartfrog.model;

public enum EMeaning {
    LUMNINOSITY("luminosity"),
    PROXIMITY("proximity"),
    COLOR("color");

    private String value;

    EMeaning(String value) {
        this.value = value;
    }

    // --------------------
    // Methods
    // --------------------

    public static EMeaning fromString(String text) {
        if (text != null) {
            for (EMeaning m : EMeaning.values()) {
                if (text.equalsIgnoreCase(m.getValue())) {
                    return m;
                }
            }
        }

        return null;
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public String getValue() {
        return value;
    }
}
