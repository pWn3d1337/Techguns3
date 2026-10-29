package techguns.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import techguns.core.damage.DeathType;
import techguns.registry.TGTags;

/** Tool materials of the Techguns tools (Techguns 2 {@code TGItems} tool materials). */
public final class TGToolTiers {
    /** Techguns 2 {@code TG_STEEL}: iron level, 1000 uses, speed 7.5, damage 2.5, enchantability 12, repaired with steel. */
    public static final Tier STEEL = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 1000, 7.5f, 2.5f, 12,
            () -> Ingredient.of(TGTags.Items.ingot("steel")));

    /** Techguns 2 {@code TGSword} / {@code TGPickaxe}: kills with gore a quarter of the time. */
    public static final DeathType DEATH_TYPE = DeathType.GORE;
    public static final float GORE_CHANCE = 0.25f;

    private TGToolTiers() {
    }
}
