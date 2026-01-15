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
    private static final String PREAMBLE = """
            \\documentclass[12pt]{extarticle}
            
            \\usepackage{amsopn}
            \\usepackage{amsmath}
            \\usepackage[letterpaper, landscape, margin=0.1in]{geometry}
            
            \\begin{document}
            \\begin{landscape}
            """;
    private static final String POSTSCRIPT = """
            \n
            \\end{landscape}
            \\end{document}
            """;
    private static final TeXFormFactory FACTORY = new TeXFormFactory();

    /// Given a LaTeX string, adds another line of LaTeX below it
    public static String appendTexs(String... texs) {
        return Arrays.stream(texs).reduce((t1, t2) -> t1 + "\n\n" + t2).orElse("");
    }

    /// Convert the given expression to LaTeX. Replaces uuu with _, for pretty printing
    public static String makeTex(IExpr expression) {
        final var buf = new StringBuilder();
        FACTORY.convert(buf, expression);
        return buf.toString().replaceAll("uuu", "_");
    }

    private static final String SPLEQ_TOKEN = "__REPLACEME__";
    private static final String SPLEQ_PRE = """
            \\begin{equation*} \\label{__REPLACEME__}
            \\begin{split}
            """;
    private static final String SPLEQ_POST = """
            
            \\end{split}
            \\end{equation*}
            """;

    /// Merges the given blocks into a split equation, where ans = texs...
    public static String makeSplitEq(String ans, String label, String... texs) {
        StringBuilder ret = new StringBuilder(SPLEQ_PRE.replaceFirst(SPLEQ_TOKEN, label));
        ret.append(ans);
        for (var tex : texs) {
            ret.append(" & = ").append(tex).append(" \\\\\n");
        }

        // Trim the last 3 chars (an extra ' \\')
        var len = ret.length();
        ret.delete(len - 3, len);
        ret.append(SPLEQ_POST);

        return ret.toString();
    }

    public static void writeTex(String tex, Path file, boolean verbose) {
        try {
            writeString(file, PREAMBLE, CREATE, TRUNCATE_EXISTING, WRITE);
            writeString(file, tex, APPEND);
            writeString(file, POSTSCRIPT, APPEND);
        } catch (IOException e) {
            if (verbose) {
                IO.println("Failed to write TeX to " + file + "!");
                IO.println(e);
            }
        }
    }
}
