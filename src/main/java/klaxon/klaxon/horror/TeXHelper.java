package klaxon.klaxon.horror;

import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.file.Path;

public class TeXHelper {
    private static final String PREAMBLE =
            "\\documentclass[17pt, letterpaper]{extarticle}\\usepackage{amsopn}\\begin{document}\\[\n\n";
    private static final String POSTSCRIPT = "\n\n\\]\\end{document}";

    /// Given a LaTeX string, adds another line of LaTeX below it
    public static String appendTex(String tex1, String tex2) {
        return tex1 + "\n\n" + tex2;
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
