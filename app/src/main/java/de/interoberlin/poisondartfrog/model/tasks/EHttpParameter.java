package de.interoberlin.poisondartfrog.model.tasks;

public enum EHttpParameter {
    // <editor-fold defaultstate="collapsed" desc="Entries">

    DBG("dbg"),
    TOKEN("token"),
    CITY("city"),
    ZIP("zip"),
    COUNTRY("country"),
    LAT("lat"),
    LONG("long"),
    TYPE("type"),
    TEMP("temp");

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Members">

    private String param;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    EHttpParameter(String param) {
        this.param = param;
    }

    public String getParam() {
        return param;
    }

    // </editor-fold>
}
