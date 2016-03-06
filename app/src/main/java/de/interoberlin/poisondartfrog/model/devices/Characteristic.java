package de.interoberlin.poisondartfrog.model.devices;

public class Characteristic {
    private String id;
    private String name;
    private EFormat format;
    private ERead read;

    public enum EFormat {
        STRING,
        UINT8,
        UINT16,
        UINT32,
        SINT8,
        SINT16,
        SINT32,
        SFLOAT,
        FLOAT;
    }

    public enum ERead {
        NEVER,
        ONCE,
        CYCLIC;
    }

    // --------------------
    // Methods
    // --------------------

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id=").append(this.getId()).append(", \n");
        sb.append("name=").append(this.getName()).append(", \n");
        sb.append("format=").append(this.getFormat()).append(", \n");
        sb.append("read=").append(this.getRead()).append(", \n");
        return sb.toString();
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

    public ERead getRead() {
        return read;
    }

    public void setRead(ERead read) {
        this.read = read;
    }
}
