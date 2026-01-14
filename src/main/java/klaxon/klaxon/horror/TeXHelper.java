package klaxon.klaxon.horror;

import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import org.matheclipse.core.form.tex.TeXFormFactory;
import org.matheclipse.core.interfaces.IExpr;

public class TeXHelper {
    private static final String PREAMBLE =
            "\\documentclass[17pt, letterpaper]{extarticle}\\usepackage{amsopn}\\begin{document}\\[\n\n";
    private static final String POSTSCRIPT = "\n\n\\]\\end{document}";
    private static final TeXFormFactory FACTORY = new TeXFormFactory();

    /// Given a LaTeX string, adds another line of LaTeX below it
    public static String appendTexs(String... texs) {
        return Arrays.stream(texs).reduce((t1, t2) -> t1 + "\n\n" + t2).orElse("");
    }

    public static String makeTex(IExpr expression) {
        final var buf = new StringBuilder();
        FACTORY.convert(buf, expression);
        return buf.toString();
    }

    public static void writeTex(String tex, String filename, boolean verbose) {
        final var path = Path.of(filename);
        try {
            writeString(path, PREAMBLE, CREATE, TRUNCATE_EXISTING, WRITE);
            writeString(path, tex, APPEND);
            writeString(path, POSTSCRIPT, APPEND);
        } catch (IOException e) {
            if (verbose) {
                IO.println("Failed to write TeX to " + path + "!");
                IO.println(e);
            }
        }
    }
}
