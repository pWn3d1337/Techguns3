package techguns.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.LevelReader;
import techguns.core.damage.DeathType;

/**
 * Techguns 2 {@code TGCrowbar}: a steel pickaxe that is also the Techguns wrench (item tag {@code c:tools/wrench}, see
 * {@link WrenchEvents}). Sneaking does not skip the block's own use, so sneak + use reaches doors and machines.
 * <p>
 * Simplified vs 1.12: hits deal the vanilla player attack (Techguns 2 tools dealt its physical damage type); the gore
 * death effect is kept.
 */
public class CrowbarItem extends PickaxeItem implements TGMeleeWeapon {
    public CrowbarItem(Properties properties) {
        super(TGToolTiers.STEEL, properties.attributes(PickaxeItem.createAttributes(TGToolTiers.STEEL, 1.0f, -2.8f)));
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public DeathType meleeDeathType(ItemStack stack) {
        return TGToolTiers.DEATH_TYPE;
    }

    @Override
    public float meleeGoreChance() {
        return TGToolTiers.GORE_CHANCE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("techguns.tooltip.wrench").withStyle(ChatFormatting.GRAY));
    }
}
