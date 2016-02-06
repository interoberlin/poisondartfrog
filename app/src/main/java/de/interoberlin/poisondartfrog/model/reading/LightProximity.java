package de.interoberlin.poisondartfrog.model.reading;

public class LightProximity {
    // --------------------
    // Inner classes
    // --------------------

    public static class Color {
        private int red;
        private int green;
        private int blue;

        // --------------------
        // Constructors
        // --------------------

        public Color() {
            this(255, 255, 255);
        }

        public Color(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        // --------------------
        // Getters / Setters
        // --------------------

        public int getRed() {
            return red;
        }

        public void setRed(int red) {
            this.red = red;
        }

        public int getGreen() {
            return green;
        }

        public void setGreen(int green) {
            this.green = green;
        }

        public int getBlue() {
            return blue;
        }

        public void setBlue(int blue) {
            this.blue = blue;
        }
    }
}
