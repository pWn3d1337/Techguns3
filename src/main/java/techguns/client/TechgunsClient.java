package techguns.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import techguns.Techguns;
import techguns.client.gun.ClientGunController;
import techguns.client.gun.GunAmmoDecorator;
import techguns.client.particle.ClientFx;
import techguns.client.render.entity.BeamProjectileRenderer;
import techguns.client.render.entity.GenericProjectileRenderer;
import techguns.client.render.entity.RocketProjectileRenderer;
import techguns.client.render.TGRenderTypes;
import techguns.client.render.item.RenderingEntityTracker;
import techguns.client.render.item.TGClientItemExtensions;
import techguns.registry.TGEntities;

/**
 * Client-only entrypoint. Never loaded on dedicated servers.
 */
@Mod(value = Techguns.MODID, dist = Dist.CLIENT)
public class TechgunsClient {
    public TechgunsClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(TGKeyMappings::register);
        modEventBus.addListener(TechgunsClient::registerEntityRenderers);
        modEventBus.addListener(TGClientItemExtensions::register);
        modEventBus.addListener(TGRenderTypes::registerShaders);
        modEventBus.addListener(GunAmmoDecorator::register);

        ClientGunController.init(NeoForge.EVENT_BUS);
        ClientFx.init(modEventBus, NeoForge.EVENT_BUS);
        RenderingEntityTracker.init(NeoForge.EVENT_BUS);
        NeoForge.EVENT_BUS.addListener(TGDebugCommands::register);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TGEntities.GENERIC_PROJECTILE.get(), GenericProjectileRenderer::new);
        event.registerEntityRenderer(TGEntities.GENERIC_BEAM_PROJECTILE.get(), BeamProjectileRenderer::new);
        event.registerEntityRenderer(TGEntities.ROCKET_PROJECTILE.get(), RocketProjectileRenderer::new);
    }
}
