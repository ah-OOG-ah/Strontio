package klaxon.klaxon.horror.ast;

import static klaxon.klaxon.horror.Stringerator.IS_ASCII_DIGIT;

import klaxon.klaxon.horror.Stringerator;

public class Tree {
    private Node root = null;

    public static class BBuf {
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
            for (int i = 0; i < len; ++i) {
                final int digit = backing[len - i];
                ret += mul * digit;
                mul *= 10;
            }

            len = 0;
            return ret;
        }
    }

    public static Tree consume(Stringerator input) {
        final var ret = new Tree();
        final var buf = new BBuf();

        if (!input.hasNext()) return ret;
        final var c = input.nextChar();

        if (IS_ASCII_DIGIT.test(c)) {
            ret.root = Integer.parse(input, buf.push((byte) (c - 0x30)));
        }

        return ret;
    }

    @Override
    public String toString() {
        return "Tree{" + root;
    }
}
