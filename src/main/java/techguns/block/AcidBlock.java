package techguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import techguns.core.damage.DeathType;
import techguns.damage.TGDamage;

/** Creeper acid (Techguns 2 {@code BlockFluidAcid}): 2 poison damage to living things in it, always with gore. */
public class AcidBlock extends LiquidBlock {
    private static final float DAMAGE = 2.0f;

    public AcidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            living.hurt(TGDamage.poison(level, null, null, DeathType.BIO).setGoreChance(1.0f), DAMAGE);
        }
    }
}
