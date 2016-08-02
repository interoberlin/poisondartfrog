package de.interoberlin.poisondartfrog.model.mapping;

import de.interoberlin.poisondartfrog.model.IDisplayable;

public class Mapping implements IDisplayable {
    // <editor-fold defaultstate="expanded" desc="Members">

    private String name;
    private String source;
    private String sink;
    private transient boolean triggered;

    private OnChangeListener ocListener;

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Getters / Setters">

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSink() {
        return sink;
    }

    public void setSink(String sink) {
        this.sink = sink;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    // </editor-fold>

    // --------------------
    // Callback interfaces
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Callback interfaces">

    public interface OnChangeListener {
    }

    public void registerOnChangeListener(OnChangeListener ocListener) {
        this.ocListener = ocListener;
    }

    // </editor-fold>
}
