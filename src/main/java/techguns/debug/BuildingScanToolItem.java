package techguns.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.TGIds;
import techguns.world.structure.StructureScans;

/**
 * Techguns 2 {@code BuildingScanTool}: scans the selected area. Techguns 2 wrote its own block list format to
 * {@code structure_scan.txt}; here the area becomes a structure template {@code techguns:scans/<name>} (the item's
 * name from an anvil, else {@code scan}), saved like the vanilla structure block's and without its size limit, to
 * place with {@code /place template} or use in a structure.
 */
public class BuildingScanToolItem extends AreaToolItem {
    public BuildingScanToolItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void apply(ServerLevel level, Player player, ItemStack stack, BlockPos min, BlockPos max) {
        String name = stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME) ? StructureScans.cleanName(stack.getHoverName().getString()) : "scan";
        ResourceLocation id = TGIds.id("scans/" + name);
        Vec3i size = new Vec3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);
        boolean saved = StructureScans.save(level, min, size, id);
        player.displayClientMessage(Component.translatable(saved ? "techguns.debug.scan.saved" : "techguns.debug.scan.failed", id.toString()), false);
    }
}
