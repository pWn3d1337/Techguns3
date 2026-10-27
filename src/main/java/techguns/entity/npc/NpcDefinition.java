package techguns.entity.npc;

import javax.annotation.Nullable;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import techguns.registry.TGSounds;

import java.util.function.Supplier;

/**
 * What a Techguns 2 NPC class set up in code: its armor stat (Techguns 2 {@code setTGArmorStats}, used instead of
 * the worn armor; {@link #GEAR_ARMOR} for {@code GenericNPCGearSpecificStats}, whose worn armor counts), sounds,
 * faction, whether it burns in daylight, its loadout and experience. Attributes go on the entity type.
 */
public record NpcDefinition(float armor, Sounds sounds, TGNpcFaction faction, boolean burnsInDay, NpcLoadout loadout, int experience) {
    /** The worn armor protects like a player's (Techguns 2 {@code GenericNPCGearSpecificStats}). */
    public static final float GEAR_ARMOR = -1.0f;
    /** Vanilla monster experience. */
    public static final int DEFAULT_EXPERIENCE = 5;

    public NpcDefinition(float armor, Sounds sounds, TGNpcFaction faction, boolean burnsInDay, NpcLoadout loadout) {
        this(armor, sounds, faction, burnsInDay, loadout, DEFAULT_EXPERIENCE);
    }

    /** The sound sets Techguns 2 NPCs used (vanilla mobs' and the cyber demon's). */
    public enum Sounds {
        VILLAGER(() -> SoundEvents.VILLAGER_AMBIENT, () -> SoundEvents.VILLAGER_HURT, () -> SoundEvents.VILLAGER_DEATH, null),
        /** Soldiers: villager voice, zombie villager steps. */
        SOLDIER(() -> SoundEvents.VILLAGER_AMBIENT, () -> SoundEvents.VILLAGER_HURT, () -> SoundEvents.VILLAGER_DEATH, () -> SoundEvents.ZOMBIE_VILLAGER_STEP),
        /** Villager voice, zombie steps. */
        HEAVY(() -> SoundEvents.VILLAGER_AMBIENT, () -> SoundEvents.VILLAGER_HURT, () -> SoundEvents.VILLAGER_DEATH, () -> SoundEvents.ZOMBIE_STEP),
        ZOMBIE(() -> SoundEvents.ZOMBIE_AMBIENT, () -> SoundEvents.ZOMBIE_HURT, () -> SoundEvents.ZOMBIE_DEATH, () -> SoundEvents.ZOMBIE_STEP),
        SKELETON(() -> SoundEvents.SKELETON_AMBIENT, () -> SoundEvents.SKELETON_HURT, () -> SoundEvents.SKELETON_DEATH, () -> SoundEvents.SKELETON_STEP),
        ZOMBIFIED_PIGLIN(() -> SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT, () -> SoundEvents.ZOMBIFIED_PIGLIN_HURT, () -> SoundEvents.ZOMBIFIED_PIGLIN_DEATH,
                () -> SoundEvents.ZOMBIE_STEP),
        GHAST(() -> SoundEvents.GHAST_AMBIENT, () -> SoundEvents.GHAST_HURT, () -> SoundEvents.GHAST_DEATH, null),
        /** Cyber demon and super mutants. */
        CYBER_DEMON(TGSounds.CYBERDEMON_IDLE, TGSounds.CYBERDEMON_HURT, TGSounds.CYBERDEMON_DEATH, TGSounds.CYBERDEMON_STEP),
        /** Attack helicopter: no ambient sound (the rotor plays on its own timer), the hit sound for hurt and death. */
        HELICOPTER(() -> null, TGSounds.HELICOPTER_HIT, TGSounds.HELICOPTER_HIT, null),
        ALIEN_BUG(TGSounds.ALIENBUG_IDLE, TGSounds.ALIENBUG_HURT, TGSounds.ALIENBUG_DEATH, TGSounds.ALIENBUG_STEP);

        public final Supplier<SoundEvent> ambient;
        public final Supplier<SoundEvent> hurt;
        public final Supplier<SoundEvent> death;
        /** Played at 0.15 volume (Techguns 2 {@code playStepSound}); null for the default step sound. */
        @Nullable
        public final Supplier<SoundEvent> step;

        Sounds(Supplier<SoundEvent> ambient, Supplier<SoundEvent> hurt, Supplier<SoundEvent> death, @Nullable Supplier<SoundEvent> step) {
            this.ambient = ambient;
            this.hurt = hurt;
            this.death = death;
            this.step = step;
        }
    }
}
