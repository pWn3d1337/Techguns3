package techguns.entity.npc;

import javax.annotation.Nullable;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * What a Techguns 2 NPC class set up in code: its armor stat (Techguns 2 {@code setTGArmorStats}, used instead of
 * the worn armor), sounds, faction, whether it burns in daylight, and its loadout. Attributes go on the entity type.
 */
public record NpcDefinition(float armor, Sounds sounds, TGNpcFaction faction, boolean burnsInDay, NpcLoadout loadout) {

    /** The vanilla sound sets Techguns 2 NPCs used. */
    public enum Sounds {
        VILLAGER(SoundEvents.VILLAGER_AMBIENT, SoundEvents.VILLAGER_HURT, SoundEvents.VILLAGER_DEATH, null),
        ZOMBIE(SoundEvents.ZOMBIE_AMBIENT, SoundEvents.ZOMBIE_HURT, SoundEvents.ZOMBIE_DEATH, SoundEvents.ZOMBIE_STEP),
        SKELETON(SoundEvents.SKELETON_AMBIENT, SoundEvents.SKELETON_HURT, SoundEvents.SKELETON_DEATH, SoundEvents.SKELETON_STEP),
        ZOMBIFIED_PIGLIN(SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT, SoundEvents.ZOMBIFIED_PIGLIN_HURT, SoundEvents.ZOMBIFIED_PIGLIN_DEATH,
                SoundEvents.ZOMBIE_STEP);

        public final SoundEvent ambient;
        public final SoundEvent hurt;
        public final SoundEvent death;
        /** Played at 0.15 volume (Techguns 2 {@code playStepSound}); null for the default step sound. */
        @Nullable
        public final SoundEvent step;

        Sounds(SoundEvent ambient, SoundEvent hurt, SoundEvent death, @Nullable SoundEvent step) {
            this.ambient = ambient;
            this.hurt = hurt;
            this.death = death;
            this.step = step;
        }
    }
}
