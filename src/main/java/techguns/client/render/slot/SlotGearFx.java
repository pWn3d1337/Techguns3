package techguns.client.render.slot;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import techguns.Techguns;
import techguns.client.particle.ClientFx;
import techguns.player.TGPlayerData;
import techguns.registry.TGSounds;
import techguns.slot.ExtraSlots;
import techguns.slot.TGSlotType;

/**
 * Flames of the jetpack and jump pack while the boost key is held (Techguns 2 spawned them from {@code ModelJetPack}
 * while rendering; here once per client tick per player), and the jetpack sounds (Techguns 2
 * {@code TGExtendedPlayerClient.setJumpkeyPressed}: start sound + loop while boosting, stop sound when it ends; 1.12 played the unmapped "jetpack.end").
 */
public final class SlotGearFx {
    /** Techguns 2 jetpack sound volume. */
    private static final float JETPACK_VOLUME = 0.75f;
    /** Players boosting in the previous tick, by entity id. */
    private static final Set<Integer> BOOSTING = new HashSet<>();

    private SlotGearFx() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener(SlotGearFx::onPlayerTick);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player) || !(player.level() instanceof ClientLevel level)) {
            return;
        }
        jetpackSounds(player, level);
        if (!TGPlayerData.of(player).jetpackBoost() || player.getDeltaMovement().y <= -0.1 || player.isSpectator()
                || !SlotGearLayer.isSlotVisible(player, TGSlotType.BACK)) {
            return;
        }
        ItemStack back = ExtraSlots.get(player, TGSlotType.BACK);
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(back.getItem());
        if (!key.getNamespace().equals(Techguns.MODID) || !(key.getPath().equals("jetpack") || key.getPath().equals("jump_pack"))) {
            return;
        }
        double rot = Math.toRadians(-player.yBodyRot);
        double dZ = player.isShiftKeyDown() ? -0.45 : -0.3;
        double sin = Math.sin(rot);
        double cos = Math.cos(rot);
        float motionY = (float) Math.min(-0.025, player.getDeltaMovement().y);
        // nozzle bottom: the rocket tubes end 8 px below the body top (1.5 blocks; the body drops 0.2 when sneaking).
        // Techguns 2 used 1.5 relative to its render position, which put the flames at the top of the pack here
        double nozzleY = player.isShiftKeyDown() ? 0.8 : 1.0;
        for (double dX : new double[] {0.2, -0.2}) {
            double offsetZ = dZ * cos - dX * sin;
            double offsetX = dZ * sin + dX * cos;
            ClientFx.createFX("FlamethrowerTrailFlames", level, player.getX() + offsetX, player.getY() + nozzleY, player.getZ() + offsetZ,
                    0.0, motionY, 0.0, 0.0f, 0.0f, 1.0f);
        }
    }

    private static boolean wearsJetpack(AbstractClientPlayer player) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(ExtraSlots.get(player, TGSlotType.BACK).getItem());
        return key.getNamespace().equals(Techguns.MODID) && key.getPath().equals("jetpack");
    }

    private static void jetpackSounds(AbstractClientPlayer player, ClientLevel level) {
        boolean boosting = TGPlayerData.of(player).jetpackBoost() && wearsJetpack(player) && !player.isSpectator();
        boolean wasBoosting = BOOSTING.contains(player.getId());
        if (boosting && !wasBoosting) {
            BOOSTING.add(player.getId());
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), TGSounds.JETPACK_START.get(), SoundSource.PLAYERS,
                    JETPACK_VOLUME, 1.0f, false);
            Minecraft.getInstance().getSoundManager().play(new JetpackLoop(player));
        } else if (!boosting && wasBoosting) {
            BOOSTING.remove(player.getId());
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), TGSounds.JETPACK_STOP.get(), SoundSource.PLAYERS,
                    JETPACK_VOLUME, 1.0f, false);
        }
    }

    /** Jetpack loop that follows the player and stops when the boost ends. */
    private static final class JetpackLoop extends AbstractTickableSoundInstance {
        private final AbstractClientPlayer player;

        JetpackLoop(AbstractClientPlayer player) {
            super(TGSounds.JETPACK_LOOP.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.player = player;
            this.looping = true;
            this.volume = JETPACK_VOLUME;
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
        }

        @Override
        public void tick() {
            if (player.isRemoved() || !BOOSTING.contains(player.getId())) {
                stop();
                return;
            }
            x = player.getX();
            y = player.getY();
            z = player.getZ();
        }
    }
}
