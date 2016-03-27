package de.interoberlin.poisondartfrog.model.service;

public class Reading {

    /** Timestamp - when reading is received on the platform */
    public final long received;

    /** Timestamp - when reading is recorded on the device */
    public final long recorded;

    /** Timestamp - when reading is saved to device state */
    public final long ts;

    /**
     * Every device has a {@link DeviceModel} which defines all device readings.
     * Every {@link DeviceReading} has a {@link DeviceReading#meaning}.
     * Details for every reading can be found in {@link DeviceReading#getValueSchema()}
     * Details about device readings can be obtained using {@link DeviceModelCache#getModelById(String)}
     * where modelId is {@link Device#getModelId()}.
     */
    public final String meaning;

    /**
     * If device contains multiple levels of readings (more than one component)
     * they will be identified with the path.
     */
    public final String path;

    /** Reading value is determined by {@link DeviceReading#getValueSchema()} */
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
