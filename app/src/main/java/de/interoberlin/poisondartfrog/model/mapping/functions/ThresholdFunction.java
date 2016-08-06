package de.interoberlin.poisondartfrog.model.mapping.functions;

public class ThresholdFunction implements IFunction {
    public static final String TAG = ThresholdFunction.class.getSimpleName();

    private float minValue;
    private float maxValue;

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Methods">

    @Override
    public boolean isTriggered(float input) {
        return input >= minValue && input <= maxValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("minValue=").append(this.getMinValue()).append(", \n");
        sb.append("maxValue=").append(this.getMaxValue());

        return sb.toString();
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Getters / Setters">

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    // </editor-fold>
}
