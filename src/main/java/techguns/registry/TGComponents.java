package techguns.registry;

import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;

/**
 * Techguns item data components (Codec + StreamCodec). Read and write them through typed accessors
 * such as {@code GunData.of(stack)}, never via scattered {@code stack.get(...)} calls.
 */
public final class TGComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Techguns.MODID);

    private TGComponents() {
    }
}
