package de.interoberlin.poisondartfrog.model;

/**
 * Enum containing reading type that can be retrieved by scanning
 */
public enum EReadingType {
    LUMINOSITY("luminosity", 0, 150), // min 0 max 4096
    PROXIMITY("proximity", 0, 150), // min 0 max 2047
    COLOR("color", 0, 4096), // min 0 max 4096

    TEMPERATURE("temperature", -10, 50), // min -100 max 100
    HUMIDITY("humidity", 0, 100); // min 0 max 100

    private String meaning;
    private int min;
    private int max;

    EReadingType(String meaning, int min, int max) {
        this.meaning = meaning;
        this.min = min;
        this.max = max;
    }

    // --------------------
    // Methods
    // --------------------

    public static EReadingType fromString(String meaning) {
        if (meaning != null) {
            for (EReadingType m : EReadingType.values()) {
                if (meaning.equalsIgnoreCase(m.getMeaning())) {
                    return m;
                }
            }
        }

        return null;
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public String getMeaning() {
        return meaning;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
