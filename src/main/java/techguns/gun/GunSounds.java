package techguns.gun;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.sounds.SoundEvent;

/**
 * Sounds of a gun. Suppliers because sound events are registry objects.
 *
 * @param fire          played on every shot
 * @param reload        played when a reload starts
 * @param fireStart     played instead of {@code fire} when a looping weapon starts firing (may be null)
 * @param rechamber     played after each shot, e.g. pump action (may be null)
 * @param maxLoopDelay  ticks until a looping weapon plays {@code fireStart} again; 0 = not looping
 */
public record GunSounds(Supplier<SoundEvent> fire, Supplier<SoundEvent> reload, @Nullable Supplier<SoundEvent> fireStart,
                        @Nullable Supplier<SoundEvent> rechamber, int maxLoopDelay) {
    public static GunSounds of(Supplier<SoundEvent> fire, Supplier<SoundEvent> reload) {
        return new GunSounds(fire, reload, null, null, 0);
    }

    public GunSounds withRechamber(Supplier<SoundEvent> rechamber) {
        return new GunSounds(fire, reload, fireStart, rechamber, maxLoopDelay);
    }

    public GunSounds withLoop(Supplier<SoundEvent> fireStart, int maxLoopDelay) {
        return new GunSounds(fire, reload, fireStart, rechamber, maxLoopDelay);
    }
}
