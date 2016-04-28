package de.interoberlin.poisondartfrog.model.tasks;

public enum EHttpParameter {
    DGG("dbg"),
    TOKEN("token"),
    CITY("city"),
    ZIP("zip"),
    COUNTRY("country"),
    LAT("lat"),
    LONG("long"),
    TYPE("type"),
    TEMP("temp");

    private String param;

    EHttpParameter(String param) {
        this.param = param;
    }

    public String getParam() {
        return param;
    }
}
