package techguns.armor;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import techguns.camo.CamoData;
import techguns.core.repair.RepairFormula;

/**
 * Techguns shield (Techguns 2 {@code GenericShield}): a vanilla shield whose block against Techguns damage follows its
 * {@link ShieldStats} (see {@link ShieldEvents}). Repaired with its material in an anvil or on the Repair Bench
 * ({@code repairCount} for a full repair, Techguns 2 {@code setRepairMat}).
 */
public class TGShieldItem extends ShieldItem implements RepairMaterial.Repairable {
    private final ShieldStats stats;
    private final TagKey<Item> repairMaterial;
    private final int repairCount;

    public TGShieldItem(Properties properties, int durability, ShieldStats stats, TagKey<Item> repairMaterial, int repairCount) {
        super(properties.durability(durability));
        this.stats = stats;
        this.repairMaterial = repairMaterial;
        this.repairCount = repairCount;
    }

    public ShieldStats stats() {
        return stats;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(repairMaterial);
    }

    @Override
    public boolean canRepairOnBench() {
        return repairCount > 0;
    }

    @Override
    public List<RepairMaterial> repairCost(int damage, int maxDamage) {
        int count = RepairFormula.count(damage, maxDamage, repairCount);
        return count == 0 ? List.of() : List.of(new RepairMaterial(Ingredient.of(repairMaterial), count));
    }

    /** Block percentages per category as the armor icon row. */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new TGArmorTooltip(TGArmorTooltip.CATEGORIES.stream().map(category -> (float) (stats.rate(category) * 100.0)).toList()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (context.registries() != null) {
            CamoData.current(context.registries(), stack).ifPresent(camo ->
                    tooltip.add(Component.translatable("tooltip.techguns.camo", camo.display()).withStyle(ChatFormatting.GRAY)));
        }
        tooltip.add(Component.translatable("tooltip.techguns.armor.durability", stack.getMaxDamage() - stack.getDamageValue(),
                stack.getMaxDamage()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.techguns.shield.blocks").withStyle(ChatFormatting.BLUE));
    }
}
