package klaxon.klaxon.horror;

import java.io.IOException;
import java.nio.file.Path;
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
