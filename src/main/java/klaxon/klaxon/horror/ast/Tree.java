package klaxon.klaxon.horror.ast;

import static klaxon.klaxon.horror.parse.Ascii.IS_ASCII_DIGIT;
import static klaxon.klaxon.horror.parse.Ascii.toDigit;

import klaxon.klaxon.horror.parse.BBuf;
import klaxon.klaxon.horror.parse.Stringerator;

public class Tree {
    private Node root = null;

    public static Tree consume(Stringerator input) {
        final var ret = new Tree();
        final var buf = new BBuf();

        if (!input.hasNext()) return ret;
        final var c = input.nextChar();

        if (IS_ASCII_DIGIT.test(c)) {
            ret.root = Integer.parse(input, buf.push(toDigit(c)));
        }

        return ret;
    }

    @Override
    public String toString() {
        return "Tree{" + root;
    }
}
