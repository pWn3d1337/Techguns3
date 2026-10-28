package techguns.compat.mekanism;

import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.api.radiation.capability.IRadiationShielding;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import techguns.TGIds;
import techguns.Techguns;
import techguns.registry.TGArmors;
import techguns.registry.TGAttributes;
import techguns.registry.TGMobEffects;

/**
 * Mekanism radiation bridge (design doc §5.4): the two radiation systems stay separate, but protection and cures work
 * for both. Techguns armor shields against Mekanism radiation (its radiation resistance, 1 for a hazmat piece, as a
 * share of Mekanism's full-suit shielding: helmet 0.25, chestplate 0.4, leggings 0.2, boots 0.15); armor with
 * Mekanism shielding (its hazmat suit) gets Techguns radiation resistance the other way round; rad regeneration (RadAway,
 * Anti-RAD Pills) also lets Mekanism radiation decay. Only loaded with Mekanism installed.
 */
// MIGRATION(26.x): Mekanism capabilities are looked up by name (mekanism:radiation_shielding, mekanism:radiation); check them.
public final class MekanismRadiation {
    private static final ItemCapability<IRadiationShielding, Void> SHIELDING = ItemCapability.createVoid(
            TGIds.of("mekanism", "radiation_shielding"), IRadiationShielding.class);
    private static final EntityCapability<IRadiationEntity, Void> RADIATION = EntityCapability.createVoid(
            TGIds.of("mekanism", "radiation"), IRadiationEntity.class);

    private MekanismRadiation() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(MekanismRadiation::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(MekanismRadiation::onAttributeModifiers);
        NeoForge.EVENT_BUS.addListener(MekanismRadiation::onPlayerTick);
    }

    /** Techguns radiation resistance of an armor stack in its slot (the Techguns 2 hazmat piece has 1). */
    public static double resistance(ItemStack stack, EquipmentSlot slot) {
        double[] total = {0.0};
        stack.forEachModifier(slot, (attribute, modifier) -> {
            if (attribute.equals(TGAttributes.RADIATION_RESISTANCE) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                total[0] += modifier.amount();
            }
        });
        return total[0];
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var armor : TGArmors.armors()) {
            event.registerItem(SHIELDING, (stack, context) -> () -> {
                EquipmentSlot slot = ((ArmorItem) stack.getItem()).getEquipmentSlot();
                return MekanismRadiationRules.slotShare(slot) * Math.min(1.0, resistance(stack, slot));
            }, armor.get());
        }
    }

    /** Other mods' armor with Mekanism shielding: the same share as Techguns radiation resistance (1 for full shielding). */
    private static void onAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ArmorItem armor) || BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(Techguns.MODID)) {
            return;
        }
        IRadiationShielding shielding = stack.getCapability(SHIELDING);
        double share = MekanismRadiationRules.slotShare(armor.getEquipmentSlot());
        if (shielding == null || share <= 0.0 || shielding.getRadiationShielding() <= 0.0) {
            return;
        }
        event.addModifier(TGAttributes.RADIATION_RESISTANCE, new AttributeModifier(TGIds.id("mekanism_shielding." + armor.getEquipmentSlot().getName()),
                Math.min(1.0, shielding.getRadiationShielding() / share), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(armor.getEquipmentSlot()));
    }

    /** Every second rad regeneration level n keeps {@code 1 - 0.02 n} of the Mekanism radiation (RadAway: gone in 20 s). */
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide || event.getEntity().tickCount % 20 != 0) {
            return;
        }
        MobEffectInstance regeneration = event.getEntity().getEffect(TGMobEffects.RAD_REGENERATION);
        IRadiationEntity radiation = regeneration == null ? null : event.getEntity().getCapability(RADIATION);
        if (radiation != null && radiation.getRadiation() > 0.0) {
            radiation.set(radiation.getRadiation() * MekanismRadiationRules.decay(regeneration.getAmplifier()));
        }
    }
}
