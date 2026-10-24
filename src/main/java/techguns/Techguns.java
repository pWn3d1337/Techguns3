package techguns;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
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

        modContainer.registerConfig(ModConfig.Type.COMMON, TGConfig.COMMON_SPEC);
    }
}
