package de.interoberlin.poisondartfrog.model.mapping.actions;

import de.interoberlin.poisondartfrog.model.mapping.Sink;

public class WriteCharacteristicAction implements IAction {
    private Object value;

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Methods">

    public void perform(Sink sink) {
        // TODO
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("value=").append(this.getValue().toString());

        return sb.toString();
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Getters / Setters">

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    // </editor-fold>
}
