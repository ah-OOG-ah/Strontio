package klaxon.klaxon.jbest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import klaxon.klaxon.jbest.preproc.Lexer;

public class Main {
    static void main(String[] args) throws IOException {
        if (args.length == 0) {
            IO.println("No input!");
            return;
        }

        var data = Files.readString(Path.of(args[0]));
        for (var tok : Lexer.lex(data.toCharArray())) {
            IO.println("Token: " + tok.val());
        }
    }
}
