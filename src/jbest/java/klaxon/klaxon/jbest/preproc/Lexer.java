package klaxon.klaxon.jbest.preproc;

import java.util.ArrayList;

public class Lexer {
    /// Each token is merely a space delimited thing
    public static ArrayList<PreToken> lex(char[] input) {
        int idx = 0;
        int len = input.length;
        var buf = new StringBuilder();
        var ret = new ArrayList<PreToken>();

        while (idx < len) {
            var next = input[idx++];
            if (!Character.isWhitespace(next)) buf.append(next);
            else if (!buf.isEmpty()) ret.add(new PreToken(buf));
        }

        return ret;
    }
}
