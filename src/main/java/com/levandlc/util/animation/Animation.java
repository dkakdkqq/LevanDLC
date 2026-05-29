package com.levandlc.util.animation;

/**
 * A frame-rate-independent smoothed value, perfect for GUI hover/toggle/expand
 * effects (the bread and butter of an animated ClickGUI).
 *
 * <p>Every call to {@link #animateTo(double)} eases the current value toward the
 * target using exponential smoothing based on real elapsed time, so the motion
 * looks identical at 30 or 240 FPS. Pure math - version-independent.
 *
 * <pre>{@code
 * private final Animation hover = new Animation(0.0, 16.0);
 * // in render():
 * float h = (float) hover.animateTo(isHovered ? 1.0 : 0.0);
 * }</pre>
 */
public class Animation {

    /** Largest time step honoured, to avoid huge jumps after a lag spike or pause. */
    private static final double MAX_DELTA_SECONDS = 0.1;

    private double value;
    private double speed;
    private long lastNanos;

    /**
     * @param initialValue starting value.
     * @param speed        higher = snappier (typical GUI range: 10-20).
     */
    public Animation(double initialValue, double speed) {
        this.value = initialValue;
        this.speed = speed;
        this.lastNanos = System.nanoTime();
    }

    public Animation(double speed) {
        this(0.0, speed);
    }

    /**
     * Advances the value toward {@code target} for the time elapsed since the last
     * call and returns the new value.
     */
    public double animateTo(double target) {
        long now = System.nanoTime();
        double dt = (now - lastNanos) / 1_000_000_000.0;
        lastNanos = now;

        if (dt > MAX_DELTA_SECONDS) {
            dt = MAX_DELTA_SECONDS;
        }

        double factor = 1.0 - Math.exp(-speed * dt);
        value += (target - value) * factor;
        return value;
    }

    public double getValue() {
        return value;
    }

    public float getValuef() {
        return (float) value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /** Returns true once the value is within {@code epsilon} of {@code target}. */
    public boolean isNear(double target, double epsilon) {
        return Math.abs(target - value) <= epsilon;
    }
}
