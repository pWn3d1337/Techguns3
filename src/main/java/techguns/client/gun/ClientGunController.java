package techguns.client.gun;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import techguns.client.TGKeyMappings;
import techguns.gun.GunClientEffects;
import techguns.gun.GunItem;
import techguns.gun.GunStats;
import techguns.gun.ShooterState;
import techguns.network.TGNetwork;

/**
 * Local player gun input (fire, zoom, force reload) and client gun effects. Uses NeoForge events only.
 */
public final class ClientGunController implements GunClientEffects {
    private static final ClientGunController INSTANCE = new ClientGunController();

    /** FOV multiplier while zoomed; 1 = not zoomed. */
    private float zoom = 1.0f;
    private boolean attackWasDown;
    private boolean useWasDown;
    private ItemStack lastMainHand = ItemStack.EMPTY;
    /** Ticks since the zoom state last changed, for the aim-down-sights animation. */
    private int zoomTicks;
    private boolean wasZooming;
    /** Dev testing: simulated held fire button (see TGDebugCommands). */
    private int forcedFireTicks;

    private ClientGunController() {
    }

    public static ClientGunController get() {
        return INSTANCE;
    }

    /** Dev testing: behave as if the attack key were held for {@code ticks} ticks. */
    public void holdFireFor(int ticks) {
        forcedFireTicks = ticks;
    }

    /** Dev testing: toggle zoom of the main hand gun as a right click would. */
    public void toggleZoomForHeldGun() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getMainHandItem().getItem() instanceof GunItem gun) {
            gun.stats().zoom().ifPresent(z -> toggleZoom(z.fovMultiplier()));
        }
    }

    /** Aim-down-sights progress: rises over 4 ticks when zooming, falls over 2.5 ticks when leaving zoom. */
    public float zoomProgress(float partialTick) {
        float time = zoomTicks + partialTick;
        return isZooming() ? Math.min(time, 4f) / 4f : 1f - Math.min(time, 2.5f) / 2.5f;
    }

    public static void init(IEventBus gameBus) {
        GunClientEffects.install(INSTANCE);
        gameBus.addListener(INSTANCE::onClientTick);
        gameBus.addListener(INSTANCE::onInteractionKey);
        gameBus.addListener(INSTANCE::onComputeFov);
        gameBus.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
            ShooterValues.clear();
            INSTANCE.zoom = 1.0f;
        });
    }

    // --- GunClientEffects ---

    @Override
    public void onFired(LivingEntity shooter, InteractionHand hand, int recoilTicks, int muzzleFlashTicks) {
        ShooterValues values = ShooterValues.of(shooter.getId());
        boolean continuous = shooter.getItemInHand(hand).getItem() instanceof GunItem gun && gun.stats().firing().continuousAnimation();
        if (!continuous || !values.recoil(hand).isActive()) {
            values.startRecoil(hand, recoilTicks);
        }
        if (!continuous || !values.muzzleFlash(hand).isActive()) {
            values.startMuzzleFlash(hand, muzzleFlashTicks);
        }
    }

    @Override
    public void onReloadStarted(LivingEntity shooter, InteractionHand hand, int reloadTicks) {
        ShooterValues.of(shooter.getId()).startReload(hand, reloadTicks);
    }

    @Override
    public void toggleZoom(float fovMultiplier) {
        zoom = zoom == 1.0f ? fovMultiplier : 1.0f;
    }

    @Override
    public void resetZoom() {
        zoom = 1.0f;
    }

    @Override
    public boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    public boolean isZooming() {
        return zoom != 1.0f;
    }

    // --- Events ---

    private void onClientTick(ClientTickEvent.Post event) {
        // animation timing runs even while input is ignored (menus, unfocused window)
        if (isZooming() != wasZooming) {
            zoomTicks = 0;
            wasZooming = isZooming();
        } else {
            zoomTicks++;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null || player.isSpectator()) {
            attackWasDown = false;
            useWasDown = false;
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (!ItemStack.isSameItem(mainHand, lastMainHand)) {
            resetZoom();
        }
        lastMainHand = mainHand;

        boolean attackDown = mc.options.keyAttack.isDown() || forcedFireTicks > 0;
        if (forcedFireTicks > 0) {
            forcedFireTicks--;
        }
        boolean useDown = mc.options.keyUse.isDown();

        if (mainHand.getItem() instanceof GunItem gun) {
            GunStats stats = gun.stats();
            stats.zoom().filter(z -> !z.toggle()).ifPresent(z -> zoom = useDown ? z.fovMultiplier() : 1.0f);
            if (gun.shootsWithLeftClick() && attackDown && (!stats.firing().semiAuto() || !attackWasDown)) {
                tryShoot(player, gun, mainHand, InteractionHand.MAIN_HAND);
            }
        }

        // off-hand guns fire with the use key, unless the main hand gun uses it for zooming
        ItemStack offHand = player.getOffhandItem();
        boolean mainUsesRightClick = mainHand.getItem() instanceof GunItem mainGun && mainGun.stats().zoom().isPresent();
        if (offHand.getItem() instanceof GunItem gun && gun.shootsWithLeftClick() && !mainUsesRightClick
                && useDown && (!gun.stats().firing().semiAuto() || !useWasDown)) {
            tryShoot(player, gun, offHand, InteractionHand.OFF_HAND);
        }

        while (TGKeyMappings.FORCE_RELOAD.consumeClick()) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof GunItem gun) {
                    TGNetwork.sendForceReload(hand);
                    gun.startReload(stack, player.level(), player, hand, true);
                }
            }
        }

        attackWasDown = attackDown;
        useWasDown = useDown;

    }

    private void tryShoot(LocalPlayer player, GunItem gun, ItemStack stack, InteractionHand hand) {
        if (ShooterState.of(player).fireDelay(hand) > 0) {
            return;
        }
        boolean zooming = isZooming();
        TGNetwork.sendShoot(hand, zooming);
        gun.shoot(stack, player.level(), player, hand, zooming);
    }

    /** Guns that shoot with left click must not attack or mine; off-hand guns block the use key. */
    private void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (event.isAttack() && player.getMainHandItem().getItem() instanceof GunItem gun && gun.shootsWithLeftClick()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    private void onComputeFov(ComputeFovModifierEvent event) {
        if (zoom != 1.0f) {
            event.setNewFovModifier(event.getNewFovModifier() * zoom);
        }
    }
}
