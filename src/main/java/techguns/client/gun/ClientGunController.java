package techguns.client.gun;

import net.minecraft.core.registries.BuiltInRegistries;
import techguns.TGIds;
import techguns.player.TGPlayerData;
import techguns.slot.ExtraSlots;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import techguns.TGConfig;
import techguns.core.view.FovMath;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import techguns.client.TGKeyMappings;
import techguns.gun.GunClientEffects;
import techguns.gun.GunHands;
import techguns.gun.GunItem;
import techguns.gun.LockOnGunItem;
import techguns.gun.GunStats;
import techguns.gun.ShooterState;
import techguns.network.TGNetwork;

/**
 * Local player gun input (fire, zoom, force reload) and client gun effects. Uses NeoForge events only.
 */
public final class ClientGunController implements GunClientEffects {
    private static final ClientGunController INSTANCE = new ClientGunController();
    private boolean forcedShift;

    /** FOV multiplier while zoomed; 1 = not zoomed. */
    private float zoom = 1.0f;
    private boolean attackWasDown;
    private boolean useWasDown;
    private boolean jetpackBoosting;
    private ItemStack lastMainHand = ItemStack.EMPTY;
    /** Ticks since the zoom state last changed, for the aim-down-sights animation. */
    private int zoomTicks;
    private boolean wasZooming;
    /** Dev testing: simulated held fire button (see TGDebugCommands). */
    private int forcedFireTicks;
    /** Dev testing: ticks the use key counts as held (/tgdebug aim). */
    private int forcedUseTicks;

    private ClientGunController() {
    }

    public static ClientGunController get() {
        return INSTANCE;
    }

    /** Dev testing: behave as if the attack key were held for {@code ticks} ticks. */
    public void holdFireFor(int ticks) {
        forcedFireTicks = ticks;
    }

    /** Dev testing: hold the use key for {@code ticks} ticks (MCP cannot press keys). */
    public void holdUseFor(int ticks) {
        forcedUseTicks = ticks;
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
            LockOnTracker.reset();
            INSTANCE.zoom = 1.0f;
            INSTANCE.zoomTicks = 0;
            INSTANCE.jetpackBoosting = false;
            INSTANCE.lastMainHand = ItemStack.EMPTY;
            INSTANCE.attackWasDown = false;
            INSTANCE.useWasDown = false;
            INSTANCE.forcedFireTicks = 0;
            INSTANCE.forcedUseTicks = 0;
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
    public void onChargeReleased(LivingEntity shooter, InteractionHand hand, float charge) {
        ShooterValues.of(shooter.getId()).setRecoilCharge(hand, charge);
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
        return Screen.hasShiftDown() || forcedShift;
    }

    /** Dev testing: tooltips as with shift held ({@code /tgdebug tooltip detailed}). */
    public void forceShift(boolean shift) {
        forcedShift = shift;
    }

    public boolean isZooming() {
        return zoom != 1.0f;
    }

    /** Flips a player toggle locally, tells the server and shows the new state. */
    private static void toggle(LocalPlayer player, TGPlayerData.Toggle toggle) {
        TGPlayerData data = TGPlayerData.of(player);
        data.set(toggle, !data.get(toggle));
        TGNetwork.sendSetToggle(toggle, data.get(toggle));
        player.displayClientMessage(Component.translatable("message.techguns.toggle." + toggle.id(),
                Component.translatable(data.get(toggle) ? "message.techguns.on" : "message.techguns.off")), true);
    }

    // --- Events ---

    /** Jetpack boost: while the jetpack is worn, charged and switched on, the jump key is the boost key (Techguns 2 ItemJetpack). */
    private void tickJetpack(Minecraft mc, LocalPlayer player) {
        boolean down = false;
        if (player != null && mc.screen == null && !player.isSpectator() && TGPlayerData.of(player).get(TGPlayerData.Toggle.JETPACK)) {
            ItemStack worn = ExtraSlots.get(player, TGSlotType.BACK);
            down = !worn.isEmpty() && worn.is(BuiltInRegistries.ITEM.get(TGIds.id("jetpack"))) && TGSlotItem.isCharged(worn)
                    && mc.options.keyJump.isDown();
        }
        if (down != jetpackBoosting) {
            jetpackBoosting = down;
            if (player != null) {
                TGPlayerData.of(player).setJetpackBoost(down);
                TGNetwork.sendJetpackBoost(down);
            }
        }
    }

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
        tickJetpack(mc, player);
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
        // dev testing: start and release the item use directly (vanilla key handling ignores a background window),
        // so charging and grenade wind-up run as with a held use key
        boolean useDown = mc.options.keyUse.isDown() || forcedUseTicks > 0;
        if (forcedUseTicks > 0 && mc.gameMode != null) {
            // the key state keeps vanilla from releasing the use again
            mc.options.keyUse.setDown(true);
            if (!player.isUsingItem()) {
                mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
            }
            if (--forcedUseTicks == 0) {
                mc.options.keyUse.setDown(false);
                if (player.isUsingItem()) {
                    mc.gameMode.releaseUsingItem(player);
                }
            }
        }

        if (mainHand.getItem() instanceof LockOnGunItem lockOnGun && useDown) {
            LockOnTracker.tick(player, lockOnGun);
        } else {
            // use key released (or no lock-on gun): the lock ends at once, so each press starts a new lock (Techguns 2
            // MouseMixin); missiles already fired keep their target
            LockOnTracker.reset();
        }

        if (mainHand.getItem() instanceof GunItem gun) {
            GunStats stats = gun.stats();
            stats.zoom().filter(z -> !z.toggle()).ifPresent(z -> zoom = useDown ? z.fovMultiplier() : 1.0f);
            if (gun.shootsWithLeftClick() && attackDown && (!stats.firing().semiAuto() || !attackWasDown)) {
                tryShoot(player, gun, mainHand, InteractionHand.MAIN_HAND);
            }
        }

        // off-hand guns fire with the use key, unless the main hand gun uses it for zooming
        ItemStack offHand = player.getOffhandItem();
        if (offHandGunUsesUseKey(player) && offHand.getItem() instanceof GunItem gun
                && useDown && (!gun.stats().firing().semiAuto() || !useWasDown)) {
            tryShoot(player, gun, offHand, InteractionHand.OFF_HAND);
        }

        while (TGKeyMappings.TOGGLE_STEP_ASSIST.consumeClick()) {
            toggle(player, TGPlayerData.Toggle.STEP_ASSIST);
        }
        while (TGKeyMappings.TOGGLE_NIGHT_VISION.consumeClick()) {
            toggle(player, TGPlayerData.Toggle.NIGHT_VISION);
        }
        while (TGKeyMappings.TOGGLE_JETPACK.consumeClick()) {
            toggle(player, TGPlayerData.Toggle.JETPACK);
        }
        while (TGKeyMappings.TOGGLE_SAFE_MODE.consumeClick()) {
            toggle(player, TGPlayerData.Toggle.SAFE_MODE);
        }
        while (TGKeyMappings.FORCE_RELOAD.consumeClick()) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof GunItem gun && GunHands.canUse(player, hand)) {
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
        int targetId = gun instanceof LockOnGunItem lockOnGun ? LockOnTracker.lockedTargetId(lockOnGun) : -1;
        TGNetwork.sendShoot(hand, zooming, targetId);
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
        // a main hand gun keeps its own use (charging, lock-on); anything else would place, eat or use as well
        if (event.isUseItem() && !(player.getMainHandItem().getItem() instanceof GunItem) && offHandGunUsesUseKey(player)) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    /**
     * The off-hand gun fires with the use key, unless the main hand gun uses that key for zooming or one of the guns is
     * two-handed.
     */
    private static boolean offHandGunUsesUseKey(LocalPlayer player) {
        boolean mainUsesRightClick = player.getMainHandItem().getItem() instanceof GunItem mainGun && mainGun.stats().zoom().isPresent();
        return player.getOffhandItem().getItem() instanceof GunItem gun && gun.shootsWithLeftClick() && !mainUsesRightClick
                && GunHands.canUseOffhand(player);
    }

    private void onComputeFov(ComputeFovModifierEvent event) {
        float modifier = event.getNewFovModifier();
        if (TGConfig.LOCK_SPEED_FOV.get()) {
            Player player = event.getPlayer();
            modifier = FovMath.lockSpeedFov(modifier, (float) player.getAttributeValue(Attributes.MOVEMENT_SPEED),
                    player.getAbilities().getWalkingSpeed(), player.isSprinting(), TGConfig.FIXED_SPRINT_FOV.get().floatValue());
        }
        if (zoom != 1.0f) {
            modifier *= zoom;
        }
        event.setNewFovModifier(modifier);
    }
}
