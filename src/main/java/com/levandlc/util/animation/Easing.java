package com.levandlc.util.animation;

/**
 * Common easing curves for explicit, duration-based animations.
 *
 * <p>Each constant maps a normalized progress {@code t} in {@code [0, 1]} to an
 * eased value in {@code [0, 1]}. Pure math - completely version-independent.
 */
public enum Easing {

    LINEAR {
        @Override
        public double apply(double t) {
            return t;
        }
    },
    EASE_IN_QUAD {
        @Override
        public double apply(double t) {
            return t * t;
        }
    },
    EASE_OUT_QUAD {
        @Override
        public double apply(double t) {
            return t * (2.0 - t);
        }
    },
    EASE_IN_OUT_QUAD {
        @Override
        public double apply(double t) {
            return t < 0.5 ? 2.0 * t * t : -1.0 + (4.0 - 2.0 * t) * t;
        }
    },
    EASE_OUT_CUBIC {
        @Override
        public double apply(double t) {
            double f = t - 1.0;
            return f * f * f + 1.0;
        }
    },
    EASE_IN_OUT_CUBIC {
        @Override
        public double apply(double t) {
            return t < 0.5 ? 4.0 * t * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 3.0) / 2.0;
        }
    },
    EASE_OUT_EXPO {
        @Override
        public double apply(double t) {
            return t >= 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * t);
        }
    },
    EASE_OUT_BACK {
        @Override
        public double apply(double t) {
            double c1 = 1.70158;
            double c3 = c1 + 1.0;
            double f = t - 1.0;
            return 1.0 + c3 * f * f * f + c1 * f * f;
        }
    };

    /** Maps normalized progress {@code t} (clamped to [0,1]) to its eased value. */
    public abstract double apply(double t);

    /** Eased interpolation between {@code start} and {@code end}. */
    public double interpolate(double start, double end, double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return start + (end - start) * apply(t);
    }
}
