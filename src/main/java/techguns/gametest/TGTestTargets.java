package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.block.Blocks;

/**
 * Test targets: adult husks without AI. Husks don't burn in daylight (a burning zombie loses health on its own) and
 * adults are always tall enough to be hit by eye-level shots (a random baby is not); they stand on a block.
 */
final class TGTestTargets {
    private TGTestTargets() {
    }

    static Husk husk(GameTestHelper helper, BlockPos pos) {
        // the empty arena has no floor everywhere: a falling target drops out of the line of fire of slow shots
        if (helper.getBlockState(pos.below()).isAir()) {
            helper.setBlock(pos.below(), Blocks.STONE);
        }
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, pos);
        husk.setBaby(false);
        return husk;
    }
}
