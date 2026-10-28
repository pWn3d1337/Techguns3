package techguns;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import techguns.armor.ArmorBonusEvents;
import techguns.armor.ArmorPowerEvents;
import techguns.armor.ShieldEvents;
import techguns.slot.ExtraSlotEvents;
import techguns.slot.ExtraSlots;
import techguns.gun.ShooterState;
import org.slf4j.Logger;
import techguns.damage.TGDamageEvents;
import techguns.gun.MeleeToolEvents;
import techguns.entity.spawn.TGSpawnManager;
import techguns.item.AmmoBagEvents;
import techguns.data.TGDataGenerators;
import techguns.network.TGNetwork;
import techguns.registry.TGRegistries;

/**
 * Techguns 3 common entrypoint. Only wires things together; feature logic lives in its own packages.
 */
@Mod(Techguns.MODID)
public class Techguns {
    public static final String MODID = "techguns";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Techguns(IEventBus modEventBus, ModContainer modContainer) {
        TGRegistries.register(modEventBus);
        // Techguns 2 Chem Lab: milk + raw rubber + green dye make slime balls
        net.neoforged.neoforge.common.NeoForgeMod.enableMilkFluid();
        modEventBus.addListener(TGNetwork::registerPayloads);
        modEventBus.addListener(TGDataGenerators::gatherData);
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) -> techguns.compat.team.TGTeams.registerProviders());
        if (net.neoforged.fml.ModList.get().isLoaded("mekanism")) {
            techguns.compat.mekanism.MekanismRadiation.register(modEventBus);
        }

        NeoForge.EVENT_BUS.addListener(TGDamageEvents::onLivingIncomingDamage);
        NeoForge.EVENT_BUS.addListener(TGDamageEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(TGDamageEvents::onKnockback);
        MeleeToolEvents.init(NeoForge.EVENT_BUS);
        ArmorBonusEvents.init(NeoForge.EVENT_BUS);
        ArmorPowerEvents.init(NeoForge.EVENT_BUS);
        ShieldEvents.init(NeoForge.EVENT_BUS);
        ExtraSlots.init();
        ExtraSlotEvents.init(NeoForge.EVENT_BUS);
        techguns.stats.StatOverrideEvents.init(NeoForge.EVENT_BUS);
        AmmoBagEvents.init(NeoForge.EVENT_BUS);
        TGSpawnManager.init(modEventBus, NeoForge.EVENT_BUS);
        techguns.radiation.RadiationEvents.init(modEventBus, NeoForge.EVENT_BUS);
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> ShooterState.of(event.getEntity()).tick());

        modContainer.registerConfig(ModConfig.Type.COMMON, TGConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, TGConfig.CLIENT_SPEC);
    }
}
