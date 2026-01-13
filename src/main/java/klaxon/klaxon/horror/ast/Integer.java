package klaxon.klaxon.horror.ast;

import static klaxon.klaxon.horror.parse.Ascii.IS_ASCII_DIGIT;
import static klaxon.klaxon.horror.parse.Ascii.toDigit;

import klaxon.klaxon.horror.parse.BBuf;
import klaxon.klaxon.horror.parse.Stringerator;

/// An AST node representing a number
public record Integer(int value) implements Node {

    public static Integer parse(Stringerator theRest, BBuf buf) {
        while (theRest.peekNext(IS_ASCII_DIGIT)) buf.push(toDigit(theRest.next()));
        return new Integer(buf.dumpAsInt());
    }
}
