package techguns.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import techguns.core.damage.DeathType;
import techguns.core.fx.EntityCondition;
import techguns.gun.GunClientEffects;
import techguns.gun.GunItem;

/**
 * Registers all Techguns payloads and offers typed send helpers. All payload types live in this package.
 */
public final class TGNetwork {
    /** Bump when payloads change incompatibly. */
    public static final String PROTOCOL_VERSION = "1";

    private TGNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(GunPayloads.Shoot.TYPE, GunPayloads.Shoot.STREAM_CODEC, TGNetwork::handleShoot);
        registrar.playToServer(GunPayloads.ForceReload.TYPE, GunPayloads.ForceReload.STREAM_CODEC, TGNetwork::handleForceReload);
        registrar.playToClient(GunPayloads.GunFired.TYPE, GunPayloads.GunFired.STREAM_CODEC, TGNetwork::handleGunFired);
        registrar.playToClient(GunPayloads.ReloadStarted.TYPE, GunPayloads.ReloadStarted.STREAM_CODEC, TGNetwork::handleReloadStarted);
        registrar.playToClient(FxPayloads.SpawnFx.TYPE, FxPayloads.SpawnFx.STREAM_CODEC, TGNetwork::handleSpawnFx);
        registrar.playToClient(FxPayloads.SpawnFxOnEntity.TYPE, FxPayloads.SpawnFxOnEntity.STREAM_CODEC, TGNetwork::handleSpawnFxOnEntity);
        registrar.playToClient(FxPayloads.GunImpactFx.TYPE, FxPayloads.GunImpactFx.STREAM_CODEC, TGNetwork::handleGunImpactFx);
        registrar.playToClient(FxPayloads.EntityDeathFx.TYPE, FxPayloads.EntityDeathFx.STREAM_CODEC, TGNetwork::handleEntityDeathFx);
    }

    // --- Send helpers ---

    public static void sendShoot(InteractionHand hand, boolean zooming, int targetId) {
        PacketDistributor.sendToServer(new GunPayloads.Shoot(hand, zooming, targetId));
    }

    public static void sendForceReload(InteractionHand hand) {
        PacketDistributor.sendToServer(new GunPayloads.ForceReload(hand));
    }

    /** To everybody tracking the shooter except the shooter (who predicted it). */
    public static void sendGunFired(LivingEntity shooter, InteractionHand hand, int recoilTicks, int muzzleFlashTicks) {
        PacketDistributor.sendToPlayersTrackingEntity(shooter, new GunPayloads.GunFired(shooter.getId(), hand, recoilTicks, muzzleFlashTicks));
    }

    public static void sendReloadStarted(LivingEntity shooter, InteractionHand hand, int reloadTicks) {
        PacketDistributor.sendToPlayersTrackingEntity(shooter, new GunPayloads.ReloadStarted(shooter.getId(), hand, reloadTicks));
    }

    // --- FX send helpers ---

    /** Spawn an FX at a position for all players within {@code radius} blocks. */
    public static void sendFxNear(ServerLevel level, double x, double y, double z, double radius, FxPayloads.SpawnFx payload) {
        PacketDistributor.sendToPlayersNear(level, null, x, y, z, radius, payload);
    }

    /** Spawn an FX at a position for all players within {@code radius} blocks of that position. */
    public static void sendFxNear(ServerLevel level, Vec3 pos, double radius, String name) {
        sendFxNear(level, pos.x, pos.y, pos.z, radius, new FxPayloads.SpawnFx(name, pos.x, pos.y, pos.z));
    }

    /** Spawn an FX at a position for all players tracking {@code entity} (not for the entity itself). */
    public static void sendFxTracking(Entity entity, FxPayloads.SpawnFx payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    /** Attach an FX to an entity for everybody tracking it. */
    public static void sendFxOnEntity(Entity entity, String name, float offsetX, float offsetY, float offsetZ, boolean attachToHead,
                                      EntityCondition condition) {
        PacketDistributor.sendToPlayersTrackingEntity(entity,
                new FxPayloads.SpawnFxOnEntity(name, entity.getId(), offsetX, offsetY, offsetZ, attachToHead, condition));
    }

    /** Same as {@link #sendFxOnEntity} but also for the entity itself if it is a player. */
    public static void sendFxOnEntityAndSelf(Entity entity, String name, float offsetX, float offsetY, float offsetZ,
                                             boolean attachToHead, EntityCondition condition) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity,
                new FxPayloads.SpawnFxOnEntity(name, entity.getId(), offsetX, offsetY, offsetZ, attachToHead, condition));
    }

    /** Bullet impact sound and particles for all players within {@code radius} blocks. */
    public static void sendGunImpactNear(ServerLevel level, FxPayloads.GunImpactFx payload, double radius) {
        PacketDistributor.sendToPlayersNear(level, null, payload.x(), payload.y(), payload.z(), radius, payload);
    }

    /** Death effect of {@code entity} for everybody tracking it, including itself if it is a player. */
    public static void sendEntityDeath(LivingEntity entity, DeathType deathType) {
        Vec3 motion = entity.getDeltaMovement();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity,
                new FxPayloads.EntityDeathFx(entity.getId(), deathType, (float) motion.x, (float) motion.y, (float) motion.z));
    }

    // --- Server handlers ---

    private static void handleShoot(GunPayloads.Shoot payload, IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getItemInHand(payload.hand());
        if (stack.getItem() instanceof GunItem gun && !player.isSpectator()) {
            Entity target = payload.targetId() >= 0 ? player.level().getEntity(payload.targetId()) : null;
            gun.shoot(stack, player.level(), player, payload.hand(), payload.zooming(), gun.acceptTarget(player, target));
        }
    }

    private static void handleForceReload(GunPayloads.ForceReload payload, IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getItemInHand(payload.hand());
        if (stack.getItem() instanceof GunItem gun && !player.isSpectator()) {
            gun.startReload(stack, player.level(), player, payload.hand(), true);
        }
    }

    // --- Client handlers (only invoked on the client) ---

    private static void handleGunFired(GunPayloads.GunFired payload, IPayloadContext context) {
        if (clientEntity(context, payload.entityId()) instanceof LivingEntity shooter) {
            GunClientEffects.get().onFired(shooter, payload.hand(), payload.recoilTicks(), payload.muzzleFlashTicks());
        }
    }

    private static void handleReloadStarted(GunPayloads.ReloadStarted payload, IPayloadContext context) {
        if (clientEntity(context, payload.entityId()) instanceof LivingEntity shooter) {
            GunClientEffects.get().onReloadStarted(shooter, payload.hand(), payload.reloadTicks());
        }
    }

    private static void handleSpawnFx(FxPayloads.SpawnFx payload, IPayloadContext context) {
        FxClientHandler.get().spawnFx(context.player().level(), payload.name(), payload.x(), payload.y(), payload.z(),
                payload.motionX(), payload.motionY(), payload.motionZ(), payload.pitch(), payload.yaw(), payload.scale());
    }

    private static void handleSpawnFxOnEntity(FxPayloads.SpawnFxOnEntity payload, IPayloadContext context) {
        Entity entity = clientEntity(context, payload.entityId());
        if (entity != null) {
            FxClientHandler.get().spawnFxOnEntity(context.player().level(), payload.name(), entity, payload.offsetX(), payload.offsetY(),
                    payload.offsetZ(), payload.attachToHead(), payload.entityCondition());
        }
    }

    private static void handleGunImpactFx(FxPayloads.GunImpactFx payload, IPayloadContext context) {
        FxClientHandler.get().gunImpact(context.player().level(), payload.soundType(), payload.x(), payload.y(), payload.z(),
                payload.pitch(), payload.yaw(), payload.incendiary(), payload.blue());
    }

    private static void handleEntityDeathFx(FxPayloads.EntityDeathFx payload, IPayloadContext context) {
        if (clientEntity(context, payload.entityId()) instanceof LivingEntity entity) {
            FxClientHandler.get().entityDeath(entity, payload.deathType(), payload.motionX(), payload.motionY(), payload.motionZ());
        }
    }

    private static Entity clientEntity(IPayloadContext context, int id) {
        return context.player().level().getEntity(id);
    }
}
