package techguns;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import techguns.gun.ShooterState;
import org.slf4j.Logger;
import techguns.damage.TGDamageEvents;
import techguns.gun.MeleeToolEvents;
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
        modEventBus.addListener(TGNetwork::registerPayloads);
        modEventBus.addListener(TGDataGenerators::gatherData);

        NeoForge.EVENT_BUS.addListener(TGDamageEvents::onLivingIncomingDamage);
        NeoForge.EVENT_BUS.addListener(TGDamageEvents::onLivingDeath);
        MeleeToolEvents.init(NeoForge.EVENT_BUS);
        AmmoBagEvents.init(NeoForge.EVENT_BUS);
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> ShooterState.of(event.getEntity()).tick());

        modContainer.registerConfig(ModConfig.Type.COMMON, TGConfig.COMMON_SPEC);
    }
}
