package techguns.data.legacy;

import java.nio.file.Path;
import java.util.Optional;

/**
 * The Techguns 2 conversion inputs in {@code src/legacy}: the data generator run passes the folder as the system
 * property {@code techguns.legacyDir}. Without it (other runs) the legacy providers generate nothing.
 */
final class LegacyFiles {
    private LegacyFiles() {
    }

    static Optional<Path> dir() {
        String dir = System.getProperty("techguns.legacyDir");
        return dir == null ? Optional.empty() : Optional.of(Path.of(dir));
    }
}
