package klaxon.klaxon.elmo.core.math;

import static java.lang.Math.abs;

public class FloatUtils {
    static final float EPSILON = 1e-8f;

    public static boolean equals(float a, float b) {
        return abs(a - b) < EPSILON;
    }
}
