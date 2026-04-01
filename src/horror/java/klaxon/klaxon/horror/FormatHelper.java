package klaxon.klaxon.horror;

import static java.lang.Math.floor;
import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static java.util.regex.Matcher.quoteReplacement;

import com.google.common.collect.HashBiMap;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.matheclipse.core.interfaces.ISymbol;

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

    /// Substitute occurences of the symbols with their SymJa-safed equivalents
    public static String subSymbols(String original, HashBiMap<ISymbol, SciValue> symbols) {
        for (var pair : symbols.entrySet()) {
            assert pair.getValue() != null;
            var name = pair.getValue().latexName;

            assert pair.getKey() != null;
            var safeName = pair.getKey().getSymbolName();

            original = original.replaceAll(quoteReplacement(name), safeName);
        }

        return original;
    }

    /// The inverse of {@link #subSymbols(String, HashBiMap)}
    public static String unsubSymbols(String original, HashBiMap<ISymbol, SciValue> symbols) {
        for (var pair : symbols.entrySet()) {
            assert pair.getValue() != null;
            var name = pair.getValue().latexName;

            assert pair.getKey() != null;
            var safeName = pair.getKey().getSymbolName();

            // No need to quote, SymJa-safe names have no special characters.
            original = original.replaceAll("(?<![a-z])" + safeName + "(?![a-z])", quoteReplacement(name));
        }

        return original;
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

    private static final String[] SAFE_NAMES = {
            "a","b","c","d","e",
            "f","g","h","i","j",
            "k","l","m","n","o",
            "p","q","r","s","t",
            "u","v","w","x","y",
            "z"
    };
    private static int FREE_NAME_IDX = 0;

    /// Returns an arbitrary name SymJa won't choke on.
    /// @throws IllegalStateException if it has to repeat names
    public static String getNextSafeName() {
        if (FREE_NAME_IDX >= SAFE_NAMES.length) throw new IllegalStateException("Ran out of names to allocate!");
        return SAFE_NAMES[FREE_NAME_IDX++];
    }

    /// Returns an arbitrary name SymJa won't choke on.
    /// @throws IllegalStateException if it has to repeat names
    public static void resetSafeNames() {
        FREE_NAME_IDX = 0;
    }
}
