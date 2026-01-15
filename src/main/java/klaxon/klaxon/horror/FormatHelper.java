package klaxon.klaxon.horror;

import static java.lang.Math.floor;
import static java.lang.Math.log10;
import static java.lang.Math.pow;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class FormatHelper {

    /// symja doesn't properly handle several characters I want to use. This converts them into characters I *don't*
    /// want to use, but symja is fine with.
    /// - `_` -> `uuu`
    /// - `capital letters` -> `zzlowercase letterszz`
    private static final ArrayList<Function<String, String>> ESCAPES_FWD = new ArrayList<>();
    private static final ArrayList<Function<String, String>> ESCAPES_BCKWD = new ArrayList<>();
    static {
        ESCAPES_FWD.add(s -> s.replaceAll("([A-Z]+)", "zz$1zz").toLowerCase());
        final var pattern = Pattern.compile("zz([a-z]+)zz");
        ESCAPES_BCKWD.add(s -> {
            var m = pattern.matcher(s);
            var sb = new StringBuilder(s.length());
            while (m.find()) {
                m.appendReplacement(sb, m.group(1).toUpperCase());
            }
            m.appendTail(sb);
            return sb.toString();
        });
        // needs to be second, to avoid ruining the first one on reverse
        ESCAPES_FWD.add(s -> s.replaceAll("_", "uuu"));
        ESCAPES_BCKWD.add(s -> s.replaceAll("uuu", "_"));
    }

    /// See [#ESCAPES_FWD] for the list of escaped symbols
    public static String escapeSymbol(String sym) {
        var ret = sym;
        for (var escaper : ESCAPES_FWD) {
            ret = escaper.apply(ret);
        }
        return ret;
    }

    /// See [#ESCAPES_FWD] for the list of escaped symbols
    public static String unescapeSymbol(String sym) {
        var ret = sym;
        for (var unescaper : ESCAPES_BCKWD) {
            ret = unescaper.apply(ret);
        }
        return ret;
    }

    /// Generates a [NumberFormat] with the correct number of decimals, assuming that errVal is a measured error.
    public static @NotNull NumberFormat makeDFormatter(double errVal) {
        // Find the "E-value" - the number is less than 1*10^(E+1)
        final var E = (int) floor(log10(errVal));
        // If the number is 1E-x, we get another digit. As a treat.
        final var precision = (errVal < pow(10, E) * 2) ? E - 1 : E;

        // Now set the rounding for both numbers
        var df = DecimalFormat.getInstance();
        df.setMaximumFractionDigits(-1 * precision);
        df.setMinimumFractionDigits(-1 * precision);
        return df;
    }

    public static String formatError(double error) {
        return makeDFormatter(error).format(error);
    }
}
