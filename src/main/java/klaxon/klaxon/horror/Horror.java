package klaxon.klaxon.horror;

import klaxon.klaxon.horror.ast.Tree;
import klaxon.klaxon.horror.parse.Stringerator;

public class Horror {
    static void main(String[] args) {
        if (args.length < 1) {
            IO.println("No input found!");
            return;
        }

        final var input = new Stringerator(args[0]);

        // Parse into an AST
        final var tree = Tree.consume(input);

        IO.println(tree);
    }
}
