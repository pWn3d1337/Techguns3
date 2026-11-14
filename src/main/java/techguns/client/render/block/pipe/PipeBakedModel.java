package techguns.client.render.block.pipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import techguns.core.transport.SideMode;
import techguns.transport.PipeParts;
import techguns.transport.TGPipeBlockEntity;

/**
 * The pipe block's model: for each segment in the block entity's model data the core, the arms to connected sides and
 * the connector plates (coloured by side mode) from the standalone part models. The quads go into the chunk mesh like
 * any block model; there is no block entity renderer.
 */
// MIGRATION(26.x): IDynamicBakedModel / ModelData are replaced by BlockStateModel#collectParts in 1.21.5+.
public class PipeBakedModel implements IDynamicBakedModel {
    private static final ChunkRenderTypeSet CUTOUT = ChunkRenderTypeSet.of(RenderType.cutout());

    private final Map<String, net.minecraft.client.resources.model.BakedModel> parts;
    private final net.minecraft.client.resources.model.BakedModel base;

    public PipeBakedModel(Map<String, net.minecraft.client.resources.model.BakedModel> parts, net.minecraft.client.resources.model.BakedModel base) {
        this.parts = parts;
        this.base = base;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data,
                                    @Nullable RenderType renderType) {
        TGPipeBlockEntity.RenderState render = data.get(TGPipeBlockEntity.RENDER_STATE);
        if (render == null) {
            return List.of();
        }
        List<BakedQuad> quads = new ArrayList<>();
        for (TGPipeBlockEntity.SegmentView segment : render.segments()) {
            add(quads, PipeParts.coreModel(segment.kind(), segment.tier(), segment.lane()), state, side, rand, renderType);
            for (Direction dir : Direction.values()) {
                int bit = 1 << dir.get3DDataValue();
                if (((segment.links() | segment.ends()) & bit) != 0) {
                    add(quads, PipeParts.armModel(segment.kind(), segment.tier(), segment.lane(), dir), state, side, rand, renderType);
                }
                SideMode mode = segment.modes()[dir.get3DDataValue()];
                if ((segment.ends() & bit) != 0 && mode.connects()) {
                    add(quads, PipeParts.plateModel(segment.lane(), dir, mode), state, side, rand, renderType);
                }
                if ((segment.adapters() & bit) != 0 && segment.lane() != techguns.core.transport.Lane.B) {
                    add(quads, PipeParts.adapterModel(segment.kind(), segment.tier(), dir), state, side, rand, renderType);
                }
            }
        }
        return quads;
    }

    private void add(List<BakedQuad> quads, String part, @Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                     @Nullable RenderType renderType) {
        net.minecraft.client.resources.model.BakedModel model = parts.get(part);
        if (model != null) {
            quads.addAll(model.getQuads(state, side, rand, ModelData.EMPTY, renderType));
        }
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return CUTOUT;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return base.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
