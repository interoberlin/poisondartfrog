package de.interoberlin.poisondartfrog.model.mapping.functions;

public enum EFunctionType {
    THRESHOLD(ThresholdFunction.class);

    private final Class c;

    // --------------------
    // Constructors
    // --------------------

    EFunctionType(Class c) {
        this.c = c;
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public Class getC() {
        return c;
    }
}
