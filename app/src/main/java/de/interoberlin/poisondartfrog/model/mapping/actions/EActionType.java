package de.interoberlin.poisondartfrog.model.mapping.actions;

public enum EActionType {
    WRITE_CHARACTERISTIC(WriteCharacteristicAction.class);

    private final Class c;

    // --------------------
    // Constructors
    // --------------------

    EActionType(Class c) {
        this.c = c;
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public Class getC() {
        return c;
    }
}
