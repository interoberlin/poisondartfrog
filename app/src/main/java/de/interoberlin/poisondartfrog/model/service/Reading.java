package de.interoberlin.poisondartfrog.model.service;

public class Reading {

    /** Timestamp - when reading is received on the platform */
    public final long received;

    /** Timestamp - when reading is recorded on the device */
    public final long recorded;

    /** Timestamp - when reading is saved to device state */
    public final long ts;
    public final String meaning;

    /**
     * If device contains multiple levels of readings (more than one component)
     * they will be identified with the path.
     */
    public final String path;
    public final Object value;

    public Reading(long received, long recorded, String meaning, String path, Object value) {
        this.received = received;
        this.recorded = recorded;
        this.meaning = meaning;
        this.path = path;
        this.value = value;
        this.ts = 0;
    }

    @Override public String toString() {
        return "Reading{" +
                "received=" + received +
                ", recorded=" + recorded +
                ", meaning='" + meaning + '\'' +
                ", path='" + path + '\'' +
                ", value=" + value +
                '}';
    }
}
