package de.interoberlin.poisondartfrog.model.devices;

public class Characteristic {
    private String id;
    private String name;
    private EFormat format;

    public enum EFormat {
        ASCII,
        FORMAT_UINT8,
        FORMAT_UINT16,
        FORMAT_UINT32,
        FORMAT_SINT8,
        FORMAT_SINT16,
        FORMAT_SINT32,
        FORMAT_SFLOAT,
        FORMAT_FLOAT;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EFormat getFormat() {
        return format;
    }

    public void setFormat(EFormat format) {
        this.format = format;
    }
}
