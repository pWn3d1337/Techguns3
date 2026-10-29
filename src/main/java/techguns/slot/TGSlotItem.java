package techguns.slot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import techguns.gun.AmmoInventory;

/**
 * Item worn in an extra slot (Techguns 2 {@code ItemTGSpecialSlot} / {@code ItemTGSpecialSlotAmmo}). Its durability
 * is its charge: worn-out gear (damage = max) does nothing until {@link #tryRecharge} swaps in a refill item from the
 * inventory. Bonuses are attribute modifiers applied while worn and charged (see {@link ExtraSlotEvents}); behaviour
 * goes into {@link #slotTick}. Right click puts it into its empty slot.
 */
public class TGSlotItem extends Item {
    private final TGSlotType slot;
    private final Map<Holder<Attribute>, Double> bonuses = new LinkedHashMap<>();
    @Nullable
    private Supplier<? extends ItemLike> refill;
    @Nullable
    private Supplier<? extends ItemLike> refillEmpty;

    /** @param durability charge; 0 = not damageable (always active) */
    public TGSlotItem(Properties properties, TGSlotType slot, int durability) {
        super(durability > 0 ? properties.durability(durability) : properties.stacksTo(1));
        this.slot = slot;
    }

    public TGSlotItem bonus(Holder<Attribute> attribute, double value) {
        bonuses.put(attribute, value);
        return this;
    }

    /** Item used up to recharge, and the item given back (Techguns 2 {@code ItemTGSpecialSlotAmmo}). */
    public TGSlotItem refill(Supplier<? extends ItemLike> full, Supplier<? extends ItemLike> empty) {
        this.refill = full;
        this.refillEmpty = empty;
        return this;
    }

    public TGSlotType slot() {
        return slot;
    }

    /** Item that recharges this gear, or null. */
    @Nullable
    public ItemLike refillItem() {
        return refill == null ? null : refill.get();
    }

    /** Bonuses this stack gives while worn now (empty when worn out). */
    public Map<Holder<Attribute>, Double> activeBonuses(Player player, ItemStack stack) {
        return isCharged(stack) ? bonuses : Map.of();
    }

    public static boolean isCharged(ItemStack stack) {
        return !stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage();
    }

    /** Uses {@code amount} charge (never below empty; creative players use none). */
    public static void useCharge(Player player, ItemStack stack, int amount) {
        if (stack.isDamageableItem() && !player.getAbilities().instabuild) {
            stack.setDamageValue(Math.min(stack.getMaxDamage(), stack.getDamageValue() + amount));
        }
    }

    /** Empty gear uses up a refill item from the inventory and is fully charged again. */
    public void tryRecharge(Player player, ItemStack stack) {
        if (refill == null || player.level().isClientSide || !stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage()) {
            return;
        }
        if (AmmoInventory.consume(player, List.of(new ItemStack(refill.get())))) {
            stack.setDamageValue(0);
            if (refillEmpty != null) {
                AmmoInventory.give(player, new ItemStack(refillEmpty.get()));
            }
        }
    }

    /** Called every tick on both sides while worn in its slot. */
    public void slotTick(Player player, ItemStack stack) {
        tryRecharge(player, stack);
    }

    /** A bonus of this item was used for an action (jump, fall); see {@code ArmorPowerEvents.useActionPower}. */
    public void onBonusAction(Player player, ItemStack stack, Holder<Attribute> bonus) {
    }

    /** Never breaks: empty gear stays and waits for a refill. */
    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && ExtraSlots.tryEquip(player, stack, slot)) {
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.techguns.slot", Component.translatable("tooltip.techguns.slot." + slot.id()))
                .withStyle(ChatFormatting.GRAY));
        if (stack.isDamageableItem()) {
            tooltip.add(Component.translatable("tooltip.techguns.slot.charge", stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage())
                    .withStyle(isCharged(stack) ? ChatFormatting.GRAY : ChatFormatting.RED));
        }
        if (refill != null) {
            tooltip.add(Component.translatable("tooltip.techguns.slot.refill", refill.get().asItem().getDescription()).withStyle(ChatFormatting.AQUA));
        }
        bonuses.forEach((attribute, value) -> tooltip.add(Component.literal("+" + value + " ")
                .append(Component.translatable(attribute.value().getDescriptionId())).withStyle(ChatFormatting.DARK_GREEN)));
    }
}
