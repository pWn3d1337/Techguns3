package techguns.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;
import techguns.slot.menu.ExtraSlotsMenu;

/** Container menu types. */
public final class TGMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Techguns.MODID);

    // MIGRATION(26.x): MenuType constructor / registration may change in 1.21.2+.
    public static final DeferredHolder<MenuType<?>, MenuType<ExtraSlotsMenu>> EXTRA_SLOTS = MENUS.register("extra_slots",
            () -> new MenuType<>(ExtraSlotsMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private TGMenus() {
    }
}
