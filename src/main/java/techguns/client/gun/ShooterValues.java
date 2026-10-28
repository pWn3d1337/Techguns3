package techguns.client.gun;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.InteractionHand;

/**
 * Client-only animation timers per shooting entity (recoil, muzzle flash, reload), in milliseconds.
 * Renderers read progress values from here.
 */
public final class ShooterValues {
    private static final Map<Integer, ShooterValues> VALUES = new HashMap<>();

    private final Timer[] recoil = {new Timer(), new Timer()};
    private final Timer[] muzzleFlash = {new Timer(), new Timer()};
    private final Timer[] reload = {new Timer(), new Timer()};
    private final float[] recoilCharge = new float[2];

    /** A running animation: ends at {@code end} (ms), lasts {@code duration} ms. */
    public static final class Timer {
        private long end;
        private int duration;

        void start(int ticks) {
            this.duration = ticks * 50;
            this.end = System.currentTimeMillis() + duration;
        }

        public boolean isActive() {
            return System.currentTimeMillis() < end;
        }

        /** Changes with every start: identifies one flash, recoil or reload. */
        public long startId() {
            return end;
        }

        /** 0 at start, 1 when finished. */
        public float progress() {
            if (duration <= 0) {
                return 1.0f;
            }
            long left = end - System.currentTimeMillis();
            return left <= 0 ? 1.0f : 1.0f - (float) left / duration;
        }
    }

    public static ShooterValues of(int entityId) {
        return VALUES.computeIfAbsent(entityId, id -> new ShooterValues());
    }

    public static void clear() {
        VALUES.clear();
    }

    public Timer recoil(InteractionHand hand) {
        return recoil[hand.ordinal()];
    }

    public Timer muzzleFlash(InteractionHand hand) {
        return muzzleFlash[hand.ordinal()];
    }

    public Timer reload(InteractionHand hand) {
        return reload[hand.ordinal()];
    }

    /** Charge a charge gun was released with; its charge animation fades out during the recoil. */
    public float recoilCharge(InteractionHand hand) {
        return recoilCharge[hand.ordinal()];
    }

    void setRecoilCharge(InteractionHand hand, float charge) {
        recoilCharge[hand.ordinal()] = charge;
    }

    void startRecoil(InteractionHand hand, int ticks) {
        recoil[hand.ordinal()].start(ticks);
    }

    void startMuzzleFlash(InteractionHand hand, int ticks) {
        muzzleFlash[hand.ordinal()].start(ticks);
    }

    void startReload(InteractionHand hand, int ticks) {
        reload[hand.ordinal()].start(ticks);
    }
}
