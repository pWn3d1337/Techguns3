package techguns.armor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import techguns.registry.TGAttributes;
import techguns.registry.TGComponents;

/**
 * Powered armor piece (Techguns 2 {@code PoweredArmor}). Each piece stores power; the chestplate refills itself from a
 * battery item in the inventory and tops up the other pieces of the same power type (see {@link ArmorPowerEvents}).
 * Bonuses depend on power: powered pieces give their full bonuses, empty ones only the "unpowered" values. Armor
 * values do not depend on power.
 */
public class TGPoweredArmorItem extends TGArmorItem {
    /** Techguns 2 {@code ArmorPowerType}: pieces only share power with a chestplate of the same type. */
    public enum PowerType {
        STEAM, RF;

        public String id() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private final PowerType powerType;
    private final int maxPower;
    private final Map<Holder<Attribute>, Double> unpoweredBonuses = new LinkedHashMap<>();
    @Nullable
    private Supplier<? extends ItemLike> battery;
    @Nullable
    private Supplier<? extends ItemLike> emptyBattery;

    public TGPoweredArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, TGArmorStats stats, PowerType powerType,
                              int maxPower) {
        super(material, type, properties.component(TGComponents.ARMOR_POWER.get(), 0), stats);
        this.powerType = powerType;
        this.maxPower = maxPower;
    }

    // --- Builder (Techguns 2 two-value setters: powered, unpowered) ---

    public TGPoweredArmorItem bonus(Holder<Attribute> attribute, double powered, double unpowered) {
        bonus(attribute, powered);
        if (unpowered != 0.0) {
            unpoweredBonuses.put(attribute, unpowered);
        }
        return this;
    }

    public TGPoweredArmorItem speed(float speed, float jump, float speedUnpowered, float jumpUnpowered) {
        return bonus(TGAttributes.SPEED_BONUS, speed, speedUnpowered).bonus(TGAttributes.JUMP_BONUS, jump, jumpUnpowered);
    }

    public TGPoweredArmorItem fallProtection(float reduction, float freeHeight, float reductionUnpowered, float freeHeightUnpowered) {
        return bonus(TGAttributes.FALL_DAMAGE_REDUCTION, reduction, reductionUnpowered)
                .bonus(TGAttributes.FALL_FREE_HEIGHT, freeHeight, freeHeightUnpowered);
    }

    public TGPoweredArmorItem mining(float bonus, float unpowered) {
        return bonus(TGAttributes.MINING_SPEED_BONUS, bonus, unpowered);
    }

    public TGPoweredArmorItem waterMining(float bonus, float unpowered) {
        return bonus(TGAttributes.WATER_MINING_SPEED_BONUS, bonus, unpowered);
    }

    public TGPoweredArmorItem waterSpeed(float bonus, float unpowered) {
        return bonus(TGAttributes.WATER_SPEED_BONUS, bonus, unpowered);
    }

    public TGPoweredArmorItem gunAccuracy(float bonus, float unpowered) {
        return bonus(TGAttributes.GUN_ACCURACY, bonus, unpowered);
    }

    public TGPoweredArmorItem health(int bonus, int unpowered) {
        return bonus(Attributes.MAX_HEALTH, bonus, unpowered);
    }

    public TGPoweredArmorItem knockbackResistance(float resistance, float unpowered) {
        return bonus(Attributes.KNOCKBACK_RESISTANCE, resistance, unpowered);
    }

    public TGPoweredArmorItem stepAssist(float value, float unpowered) {
        return bonus(TGAttributes.STEP_ASSIST, value, unpowered);
    }

    public TGPoweredArmorItem oxygenGear(float value, float unpowered) {
        return bonus(TGAttributes.OXYGEN_GEAR, value, unpowered);
    }

    public TGPoweredArmorItem cooling(float value, float unpowered) {
        return bonus(TGAttributes.COOLING_SYSTEM, value, unpowered);
    }

    /** Item the chestplate uses up when it runs empty, and the item it gives back. */
    public TGPoweredArmorItem battery(Supplier<? extends ItemLike> full, Supplier<? extends ItemLike> empty) {
        this.battery = full;
        this.emptyBattery = empty;
        return this;
    }

    // --- Power ---

    public PowerType powerType() {
        return powerType;
    }

    public int maxPower() {
        return maxPower;
    }

    @Nullable
    public ItemLike battery() {
        return battery == null ? null : battery.get();
    }

    @Nullable
    public ItemLike emptyBattery() {
        return emptyBattery == null ? null : emptyBattery.get();
    }

    public static int power(ItemStack stack) {
        return stack.getOrDefault(TGComponents.ARMOR_POWER.get(), 0);
    }

    public static boolean hasPower(ItemStack stack) {
        return power(stack) > 0;
    }

    public static void consumePower(ItemStack stack, int amount) {
        if (amount > 0) {
            stack.set(TGComponents.ARMOR_POWER.get(), Math.max(0, power(stack) - amount));
        }
    }

    /** Adds up to {@code available} power; returns the amount taken. */
    public int addPower(ItemStack stack, int available) {
        int missing = maxPower - power(stack);
        int taken = Math.max(0, Math.min(missing, available));
        if (taken > 0) {
            stack.set(TGComponents.ARMOR_POWER.get(), power(stack) + taken);
        }
        return taken;
    }

    public void setFullyPowered(ItemStack stack) {
        stack.set(TGComponents.ARMOR_POWER.get(), maxPower);
    }

    /**
     * Power a bonus action uses from this piece (Techguns 2 {@code applyPowerConsumptionOnAction}): jumping uses 5 from
     * the leggings, a fall over 3 blocks 5 from the boots, cooling 1 from the chestplate.
     */
    public int actionCost(Holder<Attribute> bonus) {
        EquipmentSlot slot = getEquipmentSlot();
        if (bonus.value() == TGAttributes.JUMP_BONUS.value()) {
            return slot == EquipmentSlot.LEGS ? 5 : 0;
        } else if (bonus.value() == TGAttributes.FALL_DAMAGE_REDUCTION.value()) {
            return slot == EquipmentSlot.FEET ? 5 : 0;
        } else if (bonus.value() == TGAttributes.COOLING_SYSTEM.value()) {
            return slot == EquipmentSlot.CHEST ? 1 : 0;
        }
        return 0;
    }

    /** Radiation resistance does not depend on power (Techguns 2 applies it as a plain attribute modifier). */
    @Override
    protected Map<Holder<Attribute>, Double> activeBonuses(ItemStack stack) {
        if (hasPower(stack)) {
            return super.activeBonuses(stack);
        }
        double radiation = bonus(TGAttributes.RADIATION_RESISTANCE);
        if (radiation == 0.0) {
            return unpoweredBonuses;
        }
        Map<Holder<Attribute>, Double> bonuses = new LinkedHashMap<>(unpoweredBonuses);
        bonuses.put(TGAttributes.RADIATION_RESISTANCE, radiation);
        return bonuses;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.techguns.armor.power", power(stack), maxPower).withStyle(ChatFormatting.AQUA));
        Component type = Component.translatable("tooltip.techguns.armor.power_type." + powerType.id());
        if (battery != null) {
            tooltip.add(Component.translatable("tooltip.techguns.armor.power_type_battery", type,
                    battery.get().asItem().getDescription()).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.techguns.armor.power_type", type).withStyle(ChatFormatting.AQUA));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
