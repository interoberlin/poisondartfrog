package de.interoberlin.poisondartfrog.model.mapping.actions;

import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.ble.BleDevice;
import de.interoberlin.poisondartfrog.model.config.ECharacteristic;
import de.interoberlin.poisondartfrog.model.mapping.Sink;

public class WriteCharacteristicAction implements IAction {
    // <editor-fold defaultstate="collapsed" desc="Members">

    private Object value;

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    /**
     * Performs action
     *
     * @param sink sink
     */
    public void perform(Sink sink) {
        BleDevice device = DevicesController.getInstance().getAttachedDeviceByAddress(sink.getAddress());
        ECharacteristic characteristic = ECharacteristic.fromId(sink.getCharacteristic());
        device.write(characteristic.getService(), characteristic, value);
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

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    // </editor-fold>
}
