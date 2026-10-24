package techguns.gun;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import techguns.registry.TGAttachments;

/**
 * Per-player gun timers, kept separately on client and server (the client predicts, the server decides).
 * Not saved and not synced: both sides tick it down themselves.
 */
public final class ShooterState {
    private final int[] fireDelay = new int[2];
    private final int[] loopSoundDelay = new int[2];

    public static ShooterState of(Player player) {
        return player.getData(TGAttachments.SHOOTER_STATE);
    }

    public int fireDelay(InteractionHand hand) {
        return fireDelay[hand.ordinal()];
    }

    public void setFireDelay(InteractionHand hand, int ticks) {
        fireDelay[hand.ordinal()] = ticks;
    }

    public int loopSoundDelay(InteractionHand hand) {
        return loopSoundDelay[hand.ordinal()];
    }

    public void setLoopSoundDelay(InteractionHand hand, int ticks) {
        loopSoundDelay[hand.ordinal()] = ticks;
    }

    /** Called once per player tick on both sides. */
    public void tick() {
        for (int i = 0; i < 2; i++) {
            if (fireDelay[i] > 0) {
                fireDelay[i]--;
            }
            if (loopSoundDelay[i] > 0) {
                loopSoundDelay[i]--;
            }
        }
    }
}
