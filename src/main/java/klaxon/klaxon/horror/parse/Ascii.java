package klaxon.klaxon.horror.parse;

import it.unimi.dsi.fastutil.bytes.BytePredicate;

public class Ascii {
    public static final byte ZERO = 48;
    public static final byte NINE = 57;
    public static final BytePredicate IS_ASCII_DIGIT = b -> b >= ZERO && b <= NINE;

    /// Converts a (presumably ASCII value) to a 0-9 digit.
    public static byte toDigit(byte val) {
        return (byte) (val - ZERO);
    }
}
