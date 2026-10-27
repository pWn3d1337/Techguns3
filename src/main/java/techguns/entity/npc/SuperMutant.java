package techguns.entity.npc;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGArmorProvider;
import techguns.registry.TGSounds;

/**
 * Techguns 2 {@code SuperMutantBasic} / {@code Heavy} / {@code Elite}: big mutants with the cyber demon's sounds and
 * their own armor per damage category; the kind picks size, armor and texture.
 */
public class SuperMutant extends TGNpc implements TGArmorProvider {
    /** Techguns 2 values: model scale, armor value, armor against elemental, physical and poison/radiation damage. */
    public enum Kind {
        BASIC(1.35f, 7, 10.0f, 7.0f, 15.0f),
        HEAVY(1.5f, 12, 16.0f, 12.0f, 18.0f),
        ELITE(1.65f, 10, 14.0f, 10.0f, 16.0f);

        public final float scale;
        private final int armorValue;
        private final float elemental;
        private final float physical;
        private final float poison;

        Kind(float scale, int armorValue, float elemental, float physical, float poison) {
            this.scale = scale;
            this.armorValue = armorValue;
            this.elemental = elemental;
            this.physical = physical;
            this.poison = poison;
        }
    }

    private final Kind kind;

    public SuperMutant(EntityType<? extends TGNpc> type, Level level, NpcDefinition definition, Kind kind) {
        super(type, level, definition);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public float tgArmorAgainst(TGDamageCategory category) {
        return switch (category) {
            case EXPLOSION, LIGHTNING, ENERGY, FIRE, ICE -> kind.elemental;
            case PHYSICAL, PROJECTILE -> kind.physical;
            case POISON, RADIATION -> kind.poison;
            default -> 0.0f;
        };
    }

    @Override
    public int getArmorValue() {
        return kind.armorValue;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TGSounds.CYBERDEMON_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TGSounds.CYBERDEMON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TGSounds.CYBERDEMON_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(TGSounds.CYBERDEMON_STEP.get(), 0.15f, 1.0f);
    }
}
