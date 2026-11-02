package techguns.client.particle;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FxDefinitionsTest {
    /**
     * A 3D particle system only spawns particles when its creator gives it models (gore gibs); an FX file that marks a
     * system 3D spawns nothing, as the guided missile explosion's fireball did.
     */
    @ParameterizedTest
    @ValueSource(strings = {"fxlist.txt", "deathfx.txt", "impactfx.txt", "nuke.txt", "testfx.txt"})
    void fxFilesDefineNo3dSystems(String file) throws IOException {
        List<String> systems3d = new ArrayList<>();
        try (InputStream in = FxDefinitionsTest.class.getResourceAsStream("/assets/techguns/particles/" + file)) {
            assertNotNull(in, file + " is on the classpath");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String system = null;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String trimmed = line.trim();
                if (trimmed.endsWith("{")) {
                    system = trimmed.substring(0, trimmed.length() - 1).trim();
                } else if (trimmed.toLowerCase(Locale.ROOT).replace(" ", "").equals("is3d=true")) {
                    systems3d.add(system);
                }
            }
        }
        assertTrue(systems3d.isEmpty(), file + " marks particle systems 3D: " + systems3d);
    }
}
