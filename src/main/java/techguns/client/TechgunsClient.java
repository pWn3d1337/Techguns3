package techguns.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import techguns.Techguns;
import techguns.client.gui.LockOnOverlay;
import techguns.client.gun.ChargeEffects;
import techguns.client.gun.ClientGunController;
import techguns.client.gun.GunAmmoDecorator;
import techguns.client.model.TGGeoBridge;
import techguns.client.render.geo.GeckoLibGunRenderer;
import techguns.client.particle.ClientFx;
import techguns.client.render.entity.BeamProjectileRenderer;
import techguns.client.render.entity.BioGunProjectileRenderer;
import techguns.client.render.entity.GenericProjectileRenderer;
import techguns.client.render.entity.GrapplingHookProjectileRenderer;
import techguns.client.render.entity.GrenadeProjectileRenderer;
import techguns.client.render.entity.RocketProjectileRenderer;
import techguns.client.render.MiningPlaneOutline;
import techguns.client.render.TGRenderTypes;
import techguns.client.render.item.RenderingEntityTracker;
import techguns.client.render.item.TGClientItemExtensions;
import techguns.registry.TGEntities;
import techguns.registry.TGParticleTypes;

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
        modEventBus.addListener(LockOnOverlay::register);
        TGGeoBridge.install(new GeckoLibGunRenderer());
        modEventBus.addListener((RegisterParticleProvidersEvent event) ->
                event.registerSpecial(TGParticleTypes.NONE.get(), (options, level, x, y, z, dx, dy, dz) -> null));

        ClientGunController.init(NeoForge.EVENT_BUS);
        ClientFx.init(modEventBus, NeoForge.EVENT_BUS);
        ChargeEffects.init(NeoForge.EVENT_BUS);
        RenderingEntityTracker.init(NeoForge.EVENT_BUS);
        MiningPlaneOutline.init(NeoForge.EVENT_BUS);
        NeoForge.EVENT_BUS.addListener(TGDebugCommands::register);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TGEntities.GENERIC_PROJECTILE.get(), GenericProjectileRenderer::new);
        event.registerEntityRenderer(TGEntities.GENERIC_BEAM_PROJECTILE.get(), BeamProjectileRenderer::new);
        event.registerEntityRenderer(TGEntities.ROCKET_PROJECTILE.get(), RocketProjectileRenderer::new);
        event.registerEntityRenderer(TGEntities.GUIDED_MISSILE.get(), RocketProjectileRenderer::new);
        event.registerEntityRenderer(TGEntities.GRENADE_PROJECTILE.get(), GrenadeProjectileRenderer::new);
        // invisible: only the main segment's particle trail is drawn
        event.registerEntityRenderer(TGEntities.SONIC_SHOTGUN_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(TGEntities.TFG_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(TGEntities.CHAINSAW_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(TGEntities.GRAPPLING_HOOK_PROJECTILE.get(), GrapplingHookProjectileRenderer::new);
        event.registerEntityRenderer(TGEntities.BIOGUN_PROJECTILE.get(), BioGunProjectileRenderer::new);
        // bullet tracer; the type's particle trail comes on top
        event.registerEntityRenderer(TGEntities.MAGIC_RIFLE_PROJECTILE.get(), GenericProjectileRenderer::new);
    }
}
