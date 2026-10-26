package techguns.armor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ItemLike;
import techguns.TGIds;
import techguns.core.damage.TGDamageCategory;
import techguns.registry.TGAttributes;

/**
 * Techguns armor piece (Techguns 2 {@code GenericArmor}). Armor against each damage category, toughness and the
 * piece's bonuses are attribute modifiers: regular armor is the physical value, the {@code techguns:armor.<category>}
 * attributes add the difference to the category value (see {@code TGArmorFormula}). A worn-out piece (one point of
 * durability left) never breaks but gives no armor and no bonuses until it is repaired.
 */
public class TGArmorItem extends ArmorItem {
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    /** Bonuses shown as percentages in the tooltip. */
    private static final List<Holder<Attribute>> PERCENT_BONUSES = List.of(TGAttributes.SPEED_BONUS, TGAttributes.WATER_SPEED_BONUS,
            TGAttributes.MINING_SPEED_BONUS, TGAttributes.WATER_MINING_SPEED_BONUS, TGAttributes.FALL_DAMAGE_REDUCTION,
            TGAttributes.GUN_ACCURACY, Attributes.KNOCKBACK_RESISTANCE);

    private final TGArmorStats stats;
    /** Bonus attribute -> value of this piece (Techguns 2 armor bonuses), in insertion order for the tooltip. */
    private final Map<Holder<Attribute>, Double> bonuses = new LinkedHashMap<>();
    @Nullable
    private Supplier<? extends ItemLike> repairItem;
    /** Custom armor model id (Techguns 2 {@code setArmorModel}), resolved by the client; null = vanilla armor model. */
    @Nullable
    private ResourceLocation armorModel;
    /** Single texture for all layers (Techguns 2 armor models without double textures); null = material layers. */
    @Nullable
    private ResourceLocation armorTexture;

    public TGArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, TGArmorStats stats) {
        super(material, type, properties.durability(stats.durability(type)));
        this.stats = stats;
    }

    // --- Builder (Techguns 2 setters) ---

    public TGArmorItem bonus(Holder<Attribute> attribute, double value) {
        if (value != 0.0) {
            bonuses.put(attribute, value);
        }
        return this;
    }

    /** Movement speed (a fraction) and jump velocity. */
    public TGArmorItem speed(float speed, float jump) {
        return bonus(TGAttributes.SPEED_BONUS, speed).bonus(TGAttributes.JUMP_BONUS, jump);
    }

    /** Fraction of fall damage avoided and fall distance without damage. */
    public TGArmorItem fallProtection(float reduction, float freeHeight) {
        return bonus(TGAttributes.FALL_DAMAGE_REDUCTION, reduction).bonus(TGAttributes.FALL_FREE_HEIGHT, freeHeight);
    }

    public TGArmorItem mining(float bonus) {
        return bonus(TGAttributes.MINING_SPEED_BONUS, bonus);
    }

    public TGArmorItem waterMining(float bonus) {
        return bonus(TGAttributes.WATER_MINING_SPEED_BONUS, bonus);
    }

    public TGArmorItem waterSpeed(float bonus) {
        return bonus(TGAttributes.WATER_SPEED_BONUS, bonus);
    }

    public TGArmorItem gunAccuracy(float bonus) {
        return bonus(TGAttributes.GUN_ACCURACY, bonus);
    }

    public TGArmorItem radiationResistance(float resistance) {
        return bonus(TGAttributes.RADIATION_RESISTANCE, resistance);
    }

    public TGArmorItem oxygenGear(float value) {
        return bonus(TGAttributes.OXYGEN_GEAR, value);
    }

    public TGArmorItem cooling(float value) {
        return bonus(TGAttributes.COOLING_SYSTEM, value);
    }

    public TGArmorItem knockbackResistance(float resistance) {
        return bonus(Attributes.KNOCKBACK_RESISTANCE, resistance);
    }

    /** Extra max health (Techguns 2 "hearts" were health points). */
    public TGArmorItem health(int bonus) {
        return bonus(Attributes.MAX_HEALTH, bonus);
    }

    /** Item that repairs this piece in an anvil (Techguns 2 {@code setRepairMats}: the metal, else the cloth). */
    public TGArmorItem repairItem(Supplier<? extends ItemLike> item) {
        this.repairItem = item;
        return this;
    }

    /** Renders with a custom armor model, see {@code TGArmorModels}. */
    public TGArmorItem armorModel(ResourceLocation model) {
        this.armorModel = model;
        return this;
    }

    /** One texture for every layer instead of the material's {@code _layer_1/_layer_2} textures. */
    public TGArmorItem armorTexture(ResourceLocation texture) {
        this.armorTexture = texture;
        return this;
    }

    // --- State ---

    @Nullable
    public ResourceLocation armorModel() {
        return armorModel;
    }

    @Override
    @Nullable
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return armorTexture;
    }

    public TGArmorStats stats() {
        return stats;
    }

    /** Value of one of this piece's bonuses, 0 if it has none. */
    public double bonus(Holder<Attribute> attribute) {
        return bonuses.getOrDefault(attribute, 0.0);
    }

    /** Worn out: one point of durability left, no armor and no bonuses. */
    public static boolean isWornOut(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    // --- Attributes ---

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (isWornOut(stack)) {
            return ItemAttributeModifiers.EMPTY;
        }
        EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(getEquipmentSlot());
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        float physical = stats.armor(getType(), TGDamageCategory.PHYSICAL);
        builder.add(Attributes.ARMOR, modifier("armor", physical), slot);
        for (TGDamageCategory category : TGDamageCategory.values()) {
            Holder<Attribute> attribute = TGAttributes.bonusArmor(category);
            if (attribute == null || category == TGDamageCategory.PHYSICAL) {
                continue;
            }
            // regular armor already counts with the category's factor; the attribute adds the rest
            double bonus = stats.armor(getType(), category) - physical * category.armorFactor(false);
            if (bonus != 0.0) {
                builder.add(attribute, modifier("armor." + category.id(), bonus), slot);
            }
        }
        if (stats.toughness() > 0.0f) {
            builder.add(Attributes.ARMOR_TOUGHNESS, modifier("toughness", stats.toughness()), slot);
        }
        bonuses.forEach((attribute, value) -> builder.add(attribute,
                modifier(attribute.unwrapKey().map(key -> key.location().getPath()).orElse("bonus"), value), slot));
        // the vanilla lines would show the category differences; the tooltip lists the real values instead
        return builder.build().withTooltip(false);
    }

    private AttributeModifier modifier(String name, double value) {
        return new AttributeModifier(TGIds.id("armor." + getType().getName() + "." + name), value, AttributeModifier.Operation.ADD_VALUE);
    }

    // --- Durability and repair ---

    /** Worn-out armor stays at one point of durability instead of breaking (Techguns 2 {@code damageArmor}). */
    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return 0;
        }
        return Math.max(0, Math.min(amount, stack.getMaxDamage() - 1 - stack.getDamageValue()));
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairItem != null && repairCandidate.is(repairItem.get().asItem());
    }

    // --- Tooltip (Techguns 2 lines, Fabric armor icon row) ---

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.techguns.armor.durability", stack.getMaxDamage() - stack.getDamageValue(),
                stack.getMaxDamage()).withStyle(isWornOut(stack) ? ChatFormatting.RED : ChatFormatting.GRAY));
        if (stats.toughness() > 0.0f) {
            tooltip.add(Component.translatable("tooltip.techguns.armor.toughness", FORMAT.format(stats.toughness())).withStyle(ChatFormatting.GRAY));
        }
        bonuses.forEach((attribute, value) -> tooltip.add(bonusLine(attribute, value)));
    }

    /** Armor values as a row of category icons (see {@code ClientArmorTooltip}). */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new TGArmorTooltip(TGArmorTooltip.CATEGORIES.stream().map(category -> stats.armor(getType(), category)).toList()));
    }

    private static Component bonusLine(Holder<Attribute> attribute, double value) {
        Component name = Component.translatable(attribute.value().getDescriptionId());
        boolean percent = PERCENT_BONUSES.stream().anyMatch(holder -> holder.value() == attribute.value());
        String amount = percent ? FORMAT.format(value * 100.0) + "%" : FORMAT.format(value);
        return Component.literal((value >= 0 ? "+" : "") + amount + " ").append(name).withStyle(ChatFormatting.DARK_GREEN);
    }

}
