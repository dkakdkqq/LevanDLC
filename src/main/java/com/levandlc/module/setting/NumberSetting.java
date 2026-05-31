package com.levandlc.module.setting;

/** A numeric setting (double) with min/max/step, rendered as a slider. */
public class NumberSetting extends Setting {

    private double value;
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = clamp(defaultValue);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double v) {
        // Snap to the nearest step, then clamp.
        if (step > 0) {
            v = min + Math.round((v - min) / step) * step;
        }
        this.value = clamp(v);
    }

    /** Sets the value from a 0..1 slider fraction. */
    public void setFromFraction(double fraction) {
        fraction = Math.max(0, Math.min(1, fraction));
        setValue(min + fraction * (max - min));
    }

    /** @return the value as a 0..1 fraction for slider rendering. */
    public double getFraction() {
        if (max - min == 0) {
            return 0;
        }
        return (value - min) / (max - min);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    /** True when the step is whole-numbered, so we can format without decimals. */
    public boolean isInteger() {
        return step >= 1.0 && step == Math.floor(step);
    }

    private double clamp(double v) {
        return Math.max(min, Math.min(max, v));
    }
}
