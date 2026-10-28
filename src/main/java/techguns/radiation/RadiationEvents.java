package techguns.radiation;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import techguns.TGConfig;
import techguns.core.damage.DeathType;
import techguns.core.radiation.RadiationRules;
import techguns.damage.TGDamage;
import techguns.registry.TGItems;
import techguns.registry.TGMobEffects;

/**
 * Techguns 2 {@code TGTickHandler} / {@code TGEventHandler} radiation parts: radioactive items in the inventory,
 * poisoning by radiation level, radiation lost on death, and the radiation potions' brewing recipes. The effects
 * given here show no particles but keep their icon, like the Techguns 2 ones.
 */
public final class RadiationEvents {
    private RadiationEvents() {
    }

    public static void init(IEventBus modBus, IEventBus gameBus) {
        modBus.addListener((RegisterDataMapTypesEvent event) -> event.register(ItemRadiation.DATA_MAP));
        gameBus.addListener(RadiationEvents::onPlayerTick);
        gameBus.addListener(RadiationEvents::onDeath);
        gameBus.addListener(RadiationEvents::registerBrewing);
        gameBus.addListener((PlayerEvent.PlayerLoggedInEvent event) -> PlayerRadiation.forgetSent(event.getEntity()));
        gameBus.addListener((PlayerEvent.PlayerRespawnEvent event) -> PlayerRadiation.forgetSent(event.getEntity()));
        gameBus.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> PlayerRadiation.forgetSent(event.getEntity()));
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        long time = player.level().getGameTime();
        if (time % RadiationRules.INVENTORY_INTERVAL == 0) {
            inventoryRadiation(player);
        }
        if (time % 20 == 0) {
            poisoning(player);
            PlayerRadiation.syncIfChanged(player);
        }
    }

    /** The radiation effect of the strongest radioactive item in the main inventory, until the next check. */
    public static void inventoryRadiation(Player player) {
        if (!TGConfig.RADIATION_ENABLED.get()) {
            return;
        }
        int level = 0;
        for (ItemStack stack : player.getInventory().items) {
            level = Math.max(level, ItemRadiation.of(stack));
        }
        if (level > 0) {
            player.addEffect(new MobEffectInstance(TGMobEffects.RADIATION, RadiationRules.INVENTORY_INTERVAL, level - 1, false, false, true));
        }
    }

    /** Techguns 2 poisoning stages, every second. */
    public static void poisoning(Player player) {
        RadiationRules.Stage stage = RadiationRules.stage(PlayerRadiation.get(player));
        switch (stage) {
            case MINOR -> player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 30, 0, false, false, true));
            case SEVERE, LETHAL -> {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 30, 1, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 30, 1, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 1, false, false, true));
            }
            default -> {
            }
        }
        if (stage == RadiationRules.Stage.LETHAL) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 30, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false, true));
            player.hurt(TGDamage.radiationPoisoning(player.level(), null, null, DeathType.LASER), RadiationRules.LETHAL_DAMAGE);
        }
    }

    /** The level carries over to the respawned player (the attachment is copied on death), minus the death loss. */
    private static void onDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Player player) {
            PlayerRadiation.add(player, -RadiationRules.LOST_ON_DEATH);
        }
    }

    /**
     * Techguns 2 brewing: awkward potion + uranium (Techguns 2: uranium dust) = radiation, + enriched uranium = severe
     * radiation, + lead = rad resistance; radiation + golden carrot or glistering melon = rad regeneration.
     */
    private static void registerBrewing(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, TGItems.RAW_URANIUM.get(), TGMobEffects.RADIATION_POTION);
        builder.addMix(Potions.AWKWARD, TGItems.ENRICHED_URANIUM.get(), TGMobEffects.SEVERE_RADIATION_POTION);
        builder.addMix(Potions.AWKWARD, TGItems.LEAD_INGOT.get(), TGMobEffects.RAD_RESISTANCE_POTION);
        builder.addMix(TGMobEffects.RADIATION_POTION, Items.GOLDEN_CARROT, TGMobEffects.RAD_REGENERATION_POTION);
        builder.addMix(TGMobEffects.RADIATION_POTION, Items.GLISTERING_MELON_SLICE, TGMobEffects.RAD_REGENERATION_POTION);
    }
}
