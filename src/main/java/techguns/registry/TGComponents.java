package techguns.registry;

import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;

/**
 * Techguns item data components (Codec + StreamCodec). Read and write them through typed accessors
 * such as {@code GunData.of(stack)}, never via scattered {@code stack.get(...)} calls.
 */
public final class TGComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Techguns.MODID);

    private TGComponents() {
    }
}
