package klaxon.klaxon.horror.ast;

import static klaxon.klaxon.horror.Stringerator.IS_ASCII_DIGIT;

import java.util.Spliterator;
import klaxon.klaxon.horror.Stringerator;

/// An AST node representing a number
public record Integer(int value) implements Node {

    public static Integer parse(Stringerator theRest, Tree.BBuf buf) {
        while (theRest.peekNext(IS_ASCII_DIGIT)) buf.push(theRest.next());
        return new Integer(buf.dumpAsInt());
    }
}
