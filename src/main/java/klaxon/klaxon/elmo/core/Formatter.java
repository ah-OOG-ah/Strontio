package klaxon.klaxon.elmo.core;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.floorDiv;
import static java.lang.Math.log10;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

public class Formatter {
    /// SI prefixes from femto (10e-15) to peta (10e15)
    public static String[] siPrefixes = { "f", "p", "n", "μ", "m", "", "k", "M", "G", "T", "P" };
    /// Inverted SI multipliers. For example, 1_000_000 * 1e-6 = 1, which you can then suffix with `M<unit>`
    public static float[] siMults = { 1e15f, 1e12f, 1e9f, 1e6f, 1e3f, 1e0f, 1e-3f, 1e-6f, 1e-9f, 1e-12f, 1e-15f };

    public static String fmtUnit(float val, String unit) {
        final var negative = val < 0;
        val = abs(val);
        final var multIdx = min(max(floorDiv((int) floor((log10(val))), 3) + 5, 0), siPrefixes.length - 1);
        if (negative) val *= -1;

        final var mult = siMults[multIdx];
        final var prefix = siPrefixes[multIdx];
        return String.format("%.3f%s%s", val * mult, prefix, unit);
    }
}
