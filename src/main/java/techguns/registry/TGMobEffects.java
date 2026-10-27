package techguns.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;
import techguns.radiation.RadiationEffects;

/** Techguns mob effects and potions (Techguns 2 {@code TGRadiationSystem} effects, {@code CommonProxy} potion types). */
public final class TGMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Techguns.MODID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, Techguns.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> RADIATION = MOB_EFFECTS.register("radiation", RadiationEffects.Radiation::new);
    public static final DeferredHolder<MobEffect, MobEffect> RAD_REGENERATION = MOB_EFFECTS.register("rad_regeneration", RadiationEffects.RadRegeneration::new);
    public static final DeferredHolder<MobEffect, MobEffect> RAD_RESISTANCE = MOB_EFFECTS.register("rad_resistance", RadiationEffects.RadResistance::new);

    /** Techguns 2 potion types: radiation V (15 s), severe radiation XXV (15 s), rad regeneration X (20 s), rad resistance II (45 s). */
    public static final DeferredHolder<Potion, Potion> RADIATION_POTION = POTIONS.register("radiation",
            () -> new Potion("techguns.radiation", new MobEffectInstance(RADIATION, 300, 4)));
    public static final DeferredHolder<Potion, Potion> SEVERE_RADIATION_POTION = POTIONS.register("severe_radiation",
            () -> new Potion("techguns.radiation", new MobEffectInstance(RADIATION, 300, 24)));
    public static final DeferredHolder<Potion, Potion> RAD_REGENERATION_POTION = POTIONS.register("rad_regeneration",
            () -> new Potion("techguns.rad_regeneration", new MobEffectInstance(RAD_REGENERATION, 400, 9)));
    public static final DeferredHolder<Potion, Potion> RAD_RESISTANCE_POTION = POTIONS.register("rad_resistance",
            () -> new Potion("techguns.rad_resistance", new MobEffectInstance(RAD_RESISTANCE, 900, 1)));

    private TGMobEffects() {
    }
}
