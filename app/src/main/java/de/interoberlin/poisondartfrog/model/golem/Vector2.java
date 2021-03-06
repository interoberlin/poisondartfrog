package de.interoberlin.poisondartfrog.model.golem;

public class Vector2 {
    // <editor-fold defaultstate="collapsed" desc="Members">

    private Float x;
    private Float y;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public Vector2(Float x, Float y) {
        this.setX(x);
        this.setY(y);
    }

    public Vector2() {
        this.setX(0.0f);
        this.setY(0.0f);
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public Vector2 add(Vector2 v) {
        this.x += v.getX();
        this.y += v.getY();
        return this;
    }

    public float getLength() {
        return (float) Math.sqrt(Math.pow(getX(), 2) + Math.pow(getY(), 2));
    }

    public void normalize() {
        this.x = this.x / this.getLength();
        this.y = this.y / this.getLength();
    }

    public void scale(Float factor) {
        this.x = this.x * factor;
        this.y = this.y * factor;
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    // </editor-fold>
}