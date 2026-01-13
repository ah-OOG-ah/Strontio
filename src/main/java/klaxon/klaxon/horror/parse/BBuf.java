package klaxon.klaxon.horror.parse;

public class BBuf {
    private static byte[] backing = new byte[16];
    private static int len = 0;

    private void resize() {
        final var newBuf = new byte[len * 2];
        System.arraycopy(backing, 0, newBuf, 0, len);
        backing = newBuf;
    }

    public BBuf push(byte val) {
        if (backing.length == len) resize();
        backing[len++] = val;
        return this;
    }

    /// Assumes all buffer elements are digits
    public int dumpAsInt() {
        int ret = 0;
        int mul = 1;
        for (int i = 1; i <= len; ++i) {
            final int digit = backing[len - i];
            ret += mul * digit;
            mul *= 10;
        }

        len = 0;
        return ret;
    }
}
