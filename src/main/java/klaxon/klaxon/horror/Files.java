package klaxon.klaxon.horror;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/// A wrapper for [java.nio.file.Files], with Option-style interfaces
public class Files {

    public static Optional<String> readString(Path path, boolean verbose) {
        try {
            return Optional.of(java.nio.file.Files.readString(path));
        } catch (IOException e) {

            if (verbose) {
                IO.println("Failed to read " + path + "!");
                IO.println(e);
            }
            return Optional.empty();
        }
    }
}
