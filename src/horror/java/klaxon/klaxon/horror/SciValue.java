package klaxon.klaxon.horror;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

public class SciValue {
    private static final Pattern NON_ZERO_POINT = Pattern.compile("[^0.]");
    private static final Pattern PROPER_DEC = Pattern.compile("^(\\d)\\.");
    private static final Pattern NORM_NUM = Pattern.compile("^[1-9]\\d*");
    public final String latexName;
    public final String exactValue;
    public final int power;
    public final double value;
    public final boolean isExact;
    public final int sigFigs;

    /// Create a variable with some error.
    /// @throws IllegalArgumentException if the error is greater than or equal to the value.
    public SciValue(@NotNull String latexName, @NotNull String exactValue, @NotNull String exactError) {
        value = Double.parseDouble(exactValue);
        this.latexName = latexName;
        if (Double.parseDouble(exactError) >= value)
            throw new IllegalArgumentException("Attempted to initialize variable with no significance!");

        // Load exact value
        final var halves = exactValue.split("E", 2);
        final var frist = unifyDecimals(halves[0]);
        final var valPow = getPowerOfTen(frist, halves.length > 1 ? halves[1] : null);
        this.exactValue = frist;
        this.power = valPow;

        // Determine significant figures
        var errPow = getPowerOfTen(exactError);

        // If the error is 1.x, it gets another digit. This is a physics thing, go away chem nerds!
        if (exactError.startsWith("1")) errPow--;

        // Ex: 1.00 +- 0.10 -> 0 - -2 + 1 -> 3.
        sigFigs = valPow - errPow + 1;
        isExact = false;
    }

    /// Create a variable with NO error.
    public SciValue(@NotNull String latexName, @NotNull String exactValue) {
        this.latexName = latexName;
        value = Double.parseDouble(exactValue);
        sigFigs = Integer.MAX_VALUE;
        isExact = true;

        // Load exact value
        final var halves = exactValue.split("E", 2);
        final var frist = unifyDecimals(halves[0]);
        final var valPow = getPowerOfTen(frist, halves.length > 1 ? halves[1] : null);
        this.exactValue = frist;
        this.power = valPow;
    }

    /// Create a variable with undetermined value.
    public SciValue(@NotNull String latexName) {
        this.latexName = latexName;
        value = Double.NaN;
        exactValue = "NaN";
        power = Integer.MIN_VALUE;
        sigFigs = 0;
        isExact = false;
    }

    /// SymJa doesn't support E-notation, so this converts it (without rounding)
    public IExpr getSymJaValue() {
        if (Double.isNaN(value)) throw new IllegalStateException("Cannot get NaN value!");

        return F.symjify(exactValue + "*10^" + power);
    }

    /// Pretty-prints the SymJa-compatible number (after round-to-even)
    public String getSymJaString() {
        if (Double.isNaN(value)) throw new IllegalStateException("Cannot get NaN value!");
        return switch (isExact) {
            case true -> Double.toString(value);
            case false -> {
                // Rounding isn't affected by power, we can split these steps up.
                final var df = DecimalFormat.getInstance();
                df.setRoundingMode(RoundingMode.HALF_EVEN);
                df.setMaximumIntegerDigits(1);
                df.setMaximumFractionDigits(sigFigs - 1);
                yield df.format(value);
            }
        } + "*10^" + power;
    }

    private static final Pattern NONZERO_DIGIT = Pattern.compile("[1-9]");
    /// Given a string like 1004, strips it to 1.004. It converts it into the first half of scientific notation,
    /// ignoring E-values.
    private String unifyDecimals(String s) {
        Matcher m = NONZERO_DIGIT.matcher(s);
        if (!m.find()) throw new IllegalArgumentException(s + " is not a nonzero number!");

        final int idx = m.start();
        return s.charAt(idx) + "." + s.substring(idx + 1);
    }

    /// See {@link #getPowerOfTen(String, String)}
    @Contract(pure = true)
    static int getPowerOfTen(@NotNull String s) {
        var split = s.split("E", 2);
        return getPowerOfTen(split[0], split.length > 1 ? split[1] : null);
    }

    /// Given some number, returns the power of ten of it's leading digit. Formats accepted:
    /// - 0.0003E-5
    /// - 4000.00000
    /// - 4000.00000E5
    /// - 4.0000E-778
    ///
    /// 0 is illegal!
    /// @param left The number part of the exponential
    /// @param right The value of the E-part.
    /// @throws IllegalArgumentException if the input isn't a normal decimal or number
    @Contract(pure = true)
    static int getPowerOfTen(@NotNull String left, @Nullable String right) {
        int place = 0;

        var decMatch = PROPER_DEC.matcher(left);
        if (decMatch.find()) {
            // This is a decimal!
            if ("0".equals(decMatch.group(1))) {
                // find the first non-zero and modify the place accordingly
                var match = NON_ZERO_POINT.matcher(left); match.find();
                place = -match.start() + 1;
            }
        } else {
            // This is a "normal number". Find the length of the first section of numbers, up to the decimal
            var numMatch = NORM_NUM.matcher(left);
            if (!numMatch.find()) throw new IllegalArgumentException("Value is neither a decimal nor a number!");
            // 1000.0E4 -> 3; 10.00 -> 1
            place = numMatch.end() - 1;
        }
        // Place now points to the power of ten of the first digit, sans E

        // Find the E-value if present, and shift the place
        if (right != null) place += Integer.parseInt(right);

        return place;
    }
}
