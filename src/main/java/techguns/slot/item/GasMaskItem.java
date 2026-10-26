package techguns.slot.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGAttributes;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/** Gas mask (Techguns 2 {@code ItemGasMask}): removes poison and wither for 1 charge each, radiation resistance. */
public class GasMaskItem extends TGSlotItem {
    public GasMaskItem(Properties properties, int durability, double radiationResistance) {
        super(properties, TGSlotType.FACE, durability);
        bonus(TGAttributes.RADIATION_RESISTANCE, radiationResistance);
    }

    @Override
    public void slotTick(Player player, ItemStack stack) {
        SlotItemSupport.removeGasEffects(player, stack);
    }
}
