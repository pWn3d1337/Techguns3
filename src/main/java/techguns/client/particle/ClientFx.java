package techguns.client.particle;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.minecraft.world.entity.LivingEntity;
import techguns.client.deatheffects.DeathEffects;
import techguns.core.damage.DeathType;
import techguns.core.fx.EntityCondition;
import techguns.client.render.entity.RenderLateEntityRenderer;
import techguns.network.FxClientHandler;
import techguns.registry.TGSounds;

/**
 * Client side of the FX system: loads the FX definitions, ticks and renders the {@link TGParticleManager}, runs the
 * late entity render pass and handles the FX payloads (Fabric {@code ClientProxy.createFX*}).
 */
public final class ClientFx implements FxClientHandler {
    private static final ClientFx INSTANCE = new ClientFx();

    private ClientFx() {
    }

    /** Registers the reload listener and event handlers and installs the payload handler. */
    public static void init(IEventBus modBus, IEventBus gameBus) {
        FxClientHandler.install(INSTANCE);
        modBus.addListener((RegisterClientReloadListenersEvent event) ->
                event.registerReloadListener((ResourceManagerReloadListener) TGFX::load));

        gameBus.addListener((ClientTickEvent.Post event) -> {
            if (Minecraft.getInstance().level != null) {
                TGParticleManager.get().tickParticles();
            }
        });
        gameBus.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
            TGParticleManager.get().clear();
            RenderLateEntityRenderer.clearQueue();
        });
        gameBus.addListener(ClientFx::onRenderLevelStage);
        DeathEffects.init(gameBus);
    }

    /**
     * Fabric hooked the end of {@code WorldRenderer.render} with a mixin. The equivalent here is
     * {@link RenderLevelStageEvent.Stage#AFTER_WEATHER}, the last stage fired while the level's model-view matrix
     * (camera rotation) is still applied to the render system ({@code AFTER_LEVEL} comes after it was popped).
     * At this stage the event's pose stack is the level's identity pose stack, so everything is drawn with
     * camera-relative coordinates ({@code worldPos - camera.getPosition()}), exactly like the entity pass.
     */
    // MIGRATION(26.x): RenderLevelStageEvent is replaced by the new render pipeline / extraction hooks.
    private static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        var buffers = mc.renderBuffers().bufferSource();
        TGParticleManager.get().renderParticles(buffers, event.getCamera(), partialTick, event.getPoseStack());
        RenderLateEntityRenderer.renderQueued(buffers, event.getCamera(), partialTick, event.getPoseStack());
    }

    // --- FxClientHandler ---

    @Override
    public void spawnFx(Level level, String name, double x, double y, double z, double motionX, double motionY, double motionZ,
                        float pitch, float yaw, float scale) {
        if (level instanceof ClientLevel clientLevel) {
            createFX(name, clientLevel, x, y, z, motionX, motionY, motionZ, pitch, yaw, scale);
        }
    }

    @Override
    public void spawnFxOnEntity(Level level, String name, Entity entity, float offsetX, float offsetY, float offsetZ,
                                boolean attachToHead, EntityCondition condition) {
        createFXOnEntity(name, entity, offsetX, offsetY, offsetZ, attachToHead, condition);
    }

    @Override
    public void entityDeath(LivingEntity entity, DeathType deathType, float motionX, float motionY, float motionZ) {
        DeathEffects.play(entity, deathType, motionX, motionY, motionZ);
    }

    @Override
    public void gunImpact(Level level, short soundType, double x, double y, double z, float pitch, float yaw, boolean incendiary,
                          boolean blue) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }
        if (incendiary) {
            createFX("Impact_IncendiaryBullet", clientLevel, x, y, z, 0, 0, 0, pitch, yaw, 1.0f);
        }
        String fx;
        SoundEvent sound;
        switch (soundType) {
            case 0 -> {
                sound = TGSounds.BULLET_IMPACT_STONE.get();
                fx = "Impact_BulletRock";
            }
            case 1 -> {
                sound = TGSounds.BULLET_IMPACT_WOOD.get();
                fx = "Impact_BulletWood";
            }
            case 2 -> {
                sound = TGSounds.BULLET_IMPACT_GLASS.get();
                fx = "Impact_BulletGlass";
            }
            case 3 -> {
                sound = TGSounds.BULLET_IMPACT_METAL.get();
                fx = "Impact_BulletMetal";
            }
            case 4 -> {
                sound = TGSounds.BULLET_IMPACT_DIRT.get();
                fx = "Impact_BulletDirt";
            }
            case 5 -> {
                // dirt sound only
                sound = TGSounds.BULLET_IMPACT_DIRT.get();
                fx = null;
            }
            default -> {
                sound = TGSounds.BULLET_IMPACT_DIRT.get();
                fx = "Impact_BulletDefault";
            }
        }
        clientLevel.playLocalSound(x, y, z, sound, SoundSource.AMBIENT, 1.0f, 1.0f, true);
        if (fx != null) {
            // advanced and gauss rounds use the blue variants (Fabric handleBulletImpactBlue)
            createFX(blue ? fx + "_Blue" : fx, clientLevel, x, y, z, 0, 0, 0, pitch, yaw, 1.0f);
        }
    }

    // --- FX creation ---

    /** Spawns a particle system at a position. */
    public static void createFX(String name, ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ,
                                float pitch, float yaw, float scale) {
        List<TGParticleSystem> systems = TGFX.createFX(level, name, x, y, z, motionX, motionY, motionZ);
        if (systems != null) {
            for (TGParticleSystem system : systems) {
                system.rotationPitch = pitch;
                system.rotationYaw = yaw;
                system.scale = scale;
                TGParticleManager.get().addEffect(system);
            }
        }
    }

    /** Spawns a particle system attached to an entity. */
    public static void createFXOnEntity(String name, @Nullable Entity entity, float offsetX, float offsetY, float offsetZ,
                                        boolean attachToHead, EntityCondition condition) {
        if (entity == null) {
            return;
        }
        List<TGParticleSystem> systems = TGFX.createFXOnEntity(entity, name);
        if (systems != null) {
            for (TGParticleSystem system : systems) {
                system.entityOffset = new Vec3(offsetX, offsetY, offsetZ);
                system.attachToHead = attachToHead;
                system.condition = condition;
                TGParticleManager.get().addEffect(system);
            }
        }
    }
}
