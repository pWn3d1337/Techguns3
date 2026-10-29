package techguns.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import techguns.client.render.armor.TGArmorClientExtensions;
import techguns.client.render.slot.SlotGearFx;
import techguns.client.render.slot.SlotGearLayer;
import techguns.Techguns;
import techguns.client.gui.ClientAmmoBagTooltip;
import techguns.client.gui.ClientArmorTooltip;
import techguns.client.gui.ExtraSlotsScreen;
import techguns.client.gui.LockOnOverlay;
import techguns.client.gun.ChargeEffects;
import techguns.client.gun.ClientGunController;
import techguns.client.gun.GunAmmoDecorator;
import techguns.client.model.TGGeoBridge;
import techguns.client.render.geo.GeckoLibGunRenderer;
import techguns.client.particle.ClientFx;
import techguns.client.render.CamoTextures;
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
import techguns.client.render.item.TGShieldRenderer;
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
        techguns.compat.fusion.FusionTextures.init(modEventBus);
        modEventBus.addListener(TechgunsClient::registerEntityRenderers);
        modEventBus.addListener(TGClientItemExtensions::register);
        modEventBus.addListener(TGArmorClientExtensions::register);
        modEventBus.addListener(TGShieldRenderer::register);
        modEventBus.addListener(techguns.client.render.item.GuideBookModel::registerAdditional);
        modEventBus.addListener(techguns.client.render.item.GuideBookModel::modifyBakingResult);
        modEventBus.addListener(SlotGearLayer::register);
        modEventBus.addListener(TGShieldBlocking::register);
        modEventBus.addListener(TGRenderTypes::registerShaders);
        modEventBus.addListener(GunAmmoDecorator::register);
        modEventBus.addListener(LockOnOverlay::register);
        modEventBus.addListener(techguns.client.gui.TGHud::register);
        modEventBus.addListener(techguns.client.gui.RadiationOverlay::registerEffectIcon);
        NeoForge.EVENT_BUS.addListener(techguns.client.gui.RadiationOverlay::onTooltip);
        modEventBus.addListener(ClientAmmoBagTooltip::register);
        modEventBus.addListener(ClientArmorTooltip::register);
        modEventBus.addListener(ExtraSlotsScreen::register);
        modEventBus.addListener(techguns.client.gui.machine.TGMachineScreens::register);
        techguns.client.render.block.TGMachineRenderers.init(modEventBus);
        techguns.client.render.block.TGFluidClient.init(modEventBus);
        TGGeoBridge.install(new GeckoLibGunRenderer());
        modEventBus.addListener((RegisterParticleProvidersEvent event) ->
                event.registerSpecial(TGParticleTypes.NONE.get(), (options, level, x, y, z, dx, dy, dz) -> null));

        ClientGunController.init(NeoForge.EVENT_BUS);
        ClientFx.init(modEventBus, NeoForge.EVENT_BUS);
        ChargeEffects.init(NeoForge.EVENT_BUS);
        RenderingEntityTracker.init(NeoForge.EVENT_BUS);
        techguns.client.render.item.OffhandHiding.init(NeoForge.EVENT_BUS);
        CamoTextures.init(NeoForge.EVENT_BUS);
        SlotGearFx.init(NeoForge.EVENT_BUS);
        MiningPlaneOutline.init(NeoForge.EVENT_BUS);
        NeoForge.EVENT_BUS.addListener(ExtraSlotsScreen::onInventoryScreenInit);
        NeoForge.EVENT_BUS.addListener(TGDebugCommands::register);
        // a server's stat overrides must not stay active in the next world
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) -> techguns.stats.StatOverrides.clear());
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TGEntities.GENERIC_PROJECTILE.get(), GenericProjectileRenderer::new);
        event.registerEntityRenderer(TGEntities.TURRET.get(), techguns.client.render.entity.TurretRenderer::new);
        registerNpcRenderers(event);
        event.registerEntityRenderer(TGEntities.GENERIC_BEAM_PROJECTILE.get(), BeamProjectileRenderer::new);
        event.registerEntityRenderer(TGEntities.ROCKET_PROJECTILE.get(), RocketProjectileRenderer::new);
        event.registerEntityRenderer(TGEntities.GUIDED_MISSILE.get(), RocketProjectileRenderer::new);
        event.registerEntityRenderer(TGEntities.GRENADE_PROJECTILE.get(), GrenadeProjectileRenderer::new);
        // invisible: only the main segment's particle trail is drawn
        event.registerEntityRenderer(TGEntities.SONIC_SHOTGUN_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(TGEntities.TFG_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(TGEntities.RADIATION_AREA.get(), NoopRenderer::new);
        event.registerEntityRenderer(TGEntities.CHAINSAW_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(TGEntities.GRAPPLING_HOOK_PROJECTILE.get(), GrapplingHookProjectileRenderer::new);
        event.registerEntityRenderer(TGEntities.BIOGUN_PROJECTILE.get(), BioGunProjectileRenderer::new);
        // bullet tracer; the type's particle trail comes on top
        event.registerEntityRenderer(TGEntities.MAGIC_RIFLE_PROJECTILE.get(), GenericProjectileRenderer::new);
    }

    /** Techguns 2 NPC renderers: texture and the vanilla model layers of the mob they look like (humans use the zombie biped). */
    private static void registerNpcRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ResourceLocation zombieSoldier = techguns.TGIds.id("textures/entity/zombie_soldier.png");
        zombieLike(event, techguns.registry.TGNpcs.ZOMBIE_SOLDIER, zombieSoldier);
        zombieLike(event, techguns.registry.TGNpcs.ZOMBIE_FARMER, zombieSoldier);
        zombieLike(event, techguns.registry.TGNpcs.ZOMBIE_MINER, zombieSoldier);
        zombieLike(event, techguns.registry.TGNpcs.ZOMBIE_POLICEMAN, techguns.TGIds.id("textures/entity/zombie_policeman.png"));
        ResourceLocation armySoldier = techguns.TGIds.id("textures/entity/army_soldier.png");
        zombieLike(event, techguns.registry.TGNpcs.ARMY_SOLDIER, armySoldier);
        zombieLike(event, techguns.registry.TGNpcs.BANDIT, techguns.TGIds.id("textures/entity/bandit.png"));
        zombieLike(event, techguns.registry.TGNpcs.COMMANDO, armySoldier);
        zombieLike(event, techguns.registry.TGNpcs.OUTCAST, armySoldier);
        zombieLike(event, techguns.registry.TGNpcs.PSYCHO_STEVE, armySoldier);
        zombieLike(event, techguns.registry.TGNpcs.STORMTROOPER, armySoldier);
        zombieLike(event, techguns.registry.TGNpcs.DICTATOR_DAVE, techguns.TGIds.id("textures/entity/dictator.png"));
        event.registerEntityRenderer(techguns.registry.TGNpcs.CYBER_DEMON.type().get(), context -> new techguns.client.render.entity.npc.CustomModelNpcRenderer(
                context, techguns.client.render.entity.npc.CyberDemonModel.createLayer(), false, techguns.TGIds.id("textures/entity/cyberdemon.png"), 0.5f));
        event.registerEntityRenderer(techguns.registry.TGNpcs.GHASTLING.type().get(), techguns.client.render.entity.npc.GhastlingRenderer::new);
        event.registerEntityRenderer(techguns.registry.TGNpcs.ATTACK_HELICOPTER.type().get(), techguns.client.render.entity.npc.AttackHelicopterRenderer::new);
        event.registerEntityRenderer(techguns.registry.TGNpcs.ALIEN_BUG.type().get(), techguns.client.render.entity.npc.AlienBugRenderer::new);
        for (techguns.registry.TGNpcs.Npc mutant : java.util.List.of(techguns.registry.TGNpcs.SUPER_MUTANT_BASIC, techguns.registry.TGNpcs.SUPER_MUTANT_HEAVY,
                techguns.registry.TGNpcs.SUPER_MUTANT_ELITE)) {
            event.registerEntityRenderer(mutant.type().get(), techguns.client.render.entity.npc.SuperMutantRenderer::new);
        }
        event.registerEntityRenderer(techguns.registry.TGNpcs.ZOMBIE_PIGMAN_SOLDIER.type().get(), context -> new techguns.client.render.entity.npc.TGNpcRenderer(
                context, techguns.TGIds.of("minecraft", "textures/entity/piglin/zombified_piglin.png"),
                net.minecraft.client.model.geom.ModelLayers.ZOMBIFIED_PIGLIN, net.minecraft.client.model.geom.ModelLayers.ZOMBIFIED_PIGLIN_INNER_ARMOR,
                net.minecraft.client.model.geom.ModelLayers.ZOMBIFIED_PIGLIN_OUTER_ARMOR));
        event.registerEntityRenderer(techguns.registry.TGNpcs.SKELETON_SOLDIER.type().get(), context -> new techguns.client.render.entity.npc.TGNpcRenderer(
                context, techguns.TGIds.of("minecraft", "textures/entity/skeleton/skeleton.png"),
                net.minecraft.client.model.geom.ModelLayers.SKELETON, net.minecraft.client.model.geom.ModelLayers.SKELETON_INNER_ARMOR,
                net.minecraft.client.model.geom.ModelLayers.SKELETON_OUTER_ARMOR));
    }

    private static void zombieLike(EntityRenderersEvent.RegisterRenderers event, techguns.registry.TGNpcs.Npc npc, ResourceLocation texture) {
        event.registerEntityRenderer(npc.type().get(), context -> new techguns.client.render.entity.npc.TGNpcRenderer(context, texture,
                net.minecraft.client.model.geom.ModelLayers.ZOMBIE, net.minecraft.client.model.geom.ModelLayers.ZOMBIE_INNER_ARMOR,
                net.minecraft.client.model.geom.ModelLayers.ZOMBIE_OUTER_ARMOR));
    }
}
