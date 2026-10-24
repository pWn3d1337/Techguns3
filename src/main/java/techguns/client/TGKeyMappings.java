package techguns.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Techguns key mappings. Handlers are added together with the features that use them.
 */
public final class TGKeyMappings {
    public static final String CATEGORY = "key.categories.techguns";

    public static final KeyMapping FORCE_RELOAD = create("key.techguns.force_reload", GLFW.GLFW_KEY_R);
    public static final KeyMapping TOGGLE_SAFE_MODE = create("key.techguns.toggle_safe_mode", GLFW.GLFW_KEY_B);
    public static final KeyMapping TOGGLE_STEP_ASSIST = create("key.techguns.toggle_step_assist", GLFW.GLFW_KEY_V);

    private TGKeyMappings() {
    }

    private static KeyMapping create(String name, int defaultKey) {
        return new KeyMapping(name, InputConstants.Type.KEYSYM, defaultKey, CATEGORY);
    }

    static void register(RegisterKeyMappingsEvent event) {
        event.register(FORCE_RELOAD);
        event.register(TOGGLE_SAFE_MODE);
        event.register(TOGGLE_STEP_ASSIST);
    }
}
