package klaxon.klaxon.horror.parse;

import static java.nio.charset.StandardCharsets.US_ASCII;

import it.unimi.dsi.fastutil.bytes.BytePredicate;
import java.util.Iterator;

public class Stringerator implements Iterator<Byte> {
    private final byte[] src;
    private int index = 0;

    public Stringerator(String src) {
        this.src = src.getBytes(US_ASCII);
    }

    public byte nextChar() {
        return src[index++];
    }

    /// @return True if the next byte exists and matches the predicate
    public boolean peekNext(BytePredicate condition) {
        return hasNext() && condition.test(src[index]);
    }

    @Override
    public boolean hasNext() {
        return index < src.length;
    }

    @Override
    public Byte next() {
        return nextChar();
    }
}
