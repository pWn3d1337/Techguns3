package techguns.network;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
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
    }

    // --- Send helpers ---

    public static void sendShoot(InteractionHand hand, boolean zooming) {
        PacketDistributor.sendToServer(new GunPayloads.Shoot(hand, zooming));
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

    // --- Server handlers ---

    private static void handleShoot(GunPayloads.Shoot payload, IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getItemInHand(payload.hand());
        if (stack.getItem() instanceof GunItem gun && !player.isSpectator()) {
            gun.shoot(stack, player.level(), player, payload.hand(), payload.zooming());
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

    private static Entity clientEntity(IPayloadContext context, int id) {
        return context.player().level().getEntity(id);
    }
}
