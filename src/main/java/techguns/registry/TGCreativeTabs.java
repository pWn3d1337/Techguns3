package techguns.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;

public final class TGCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Techguns.MODID);

    public static final String MAIN_TITLE = "itemGroup.techguns";

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable(MAIN_TITLE))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> TGItems.STEEL_INGOT.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                TGBlocks.blocksWithItem().forEach(block -> output.accept(block.get()));
                TGItems.simpleItems().forEach(item -> output.accept(item.get()));
            })
            .build());

    private TGCreativeTabs() {
    }
}
