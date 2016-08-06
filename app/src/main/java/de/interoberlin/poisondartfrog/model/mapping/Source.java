package de.interoberlin.poisondartfrog.model.mapping;

public class Source {
    private String address;
    private String characteristic;

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Methods">

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("address=").append(this.getAddress()).append(", \n");
        sb.append("characteristic=").append(this.getCharacteristic().toString());

        return sb.toString();
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Getters / Setters">


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    // </editor-fold>
}
