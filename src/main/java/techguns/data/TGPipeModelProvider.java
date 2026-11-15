package techguns.data;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import techguns.Techguns;
import techguns.core.transport.Lane;
import techguns.core.transport.SideMode;
import techguns.registry.TGBlocks;
import techguns.transport.PipeKind;
import techguns.transport.PipeParts;
import techguns.transport.TGPipeKinds;

/**
 * Pipe models (Techguns 3): the pipe block state (a particle-only model, replaced by the pipe model at bake time), the
 * standalone part models the pipe model assembles ({@code block/pipe/part/*}, geometry from {@link PipeParts}) and the
 * pipe item models. Texture layout: see {@code tools/tex_transport.py}.
 */
public class TGPipeModelProvider extends BlockStateProvider {
    /** Texture regions in 1/16 of the texture (tools/tex_transport.py). */
    private static final float[] TUBE_UV = {0, 0, 3, 16};
    private static final float[] CORE_UV = {4, 0, 8, 4};
    private static final float[] PLATE_FACE_UV = {0, 0, 5, 5};
    private static final float[] PLATE_EDGE_UV = {0, 6, 5, 7};

    public TGPipeModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Techguns.MODID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Pipe Models: " + Techguns.MODID;
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile block = models().getBuilder("block/pipe").texture("particle", modLoc("block/pipe/cable_basic"));
        getVariantBuilder(TGBlocks.PIPE.get()).partialState().setModels(new ConfiguredModel(block));
        for (PipeKind kind : TGPipeKinds.ALL) {
            for (int tier = 0; tier < kind.tierCount(); tier++) {
                ResourceLocation texture = modLoc("block/pipe/" + kind.textureName(tier));
                for (Lane lane : PipeParts.lanes(kind)) {
                    BlockModelBuilder core = part(PipeParts.coreModel(kind, tier, lane)).texture("pipe", texture).texture("particle", texture);
                    box(core, PipeParts.core(lane), null, "#pipe", CORE_UV, CORE_UV, null);
                    for (Direction side : Direction.values()) {
                        BlockModelBuilder arm = part(PipeParts.armModel(kind, tier, lane, side)).texture("pipe", texture)
                                .texture("particle", texture);
                        box(arm, PipeParts.arm(lane, side), side.getAxis(), "#pipe", TUBE_UV, null, null);
                    }
                }
                if (kind.lane() != Lane.B) {
                    for (Direction side : Direction.values()) {
                        BlockModelBuilder adapter = part(PipeParts.adapterModel(kind, tier, side)).texture("pipe", texture)
                                .texture("particle", texture);
                        PipeParts.Bridge bridge = PipeParts.adapter(kind.lane(), side);
                        box(adapter, bridge.box(), bridge.longAxis(), "#pipe", TUBE_UV, null, null, bridge);
                        for (double[] joint : PipeParts.adapterJoints(kind.lane(), side)) {
                            box(adapter, joint, null, "#pipe", CORE_UV, CORE_UV, side);
                        }
                    }
                }
                itemModel(kind, tier, texture);
            }
        }
        for (Lane lane : Lane.values()) {
            for (Direction side : Direction.values()) {
                for (SideMode mode : PipeParts.PLATE_MODES) {
                    ResourceLocation texture = modLoc("block/pipe/connector_" + mode.name().toLowerCase(java.util.Locale.ROOT));
                    BlockModelBuilder plate = part(PipeParts.plateModel(lane, side, mode)).texture("plate", texture).texture("particle", texture);
                    box(plate, PipeParts.plate(lane, side), side.getAxis(), "#plate", PLATE_EDGE_UV, PLATE_FACE_UV, side);
                }
            }
        }
    }

    private BlockModelBuilder part(String name) {
        return models().getBuilder("block/pipe/part/" + name).ao(true);
    }

    /**
     * One box. With an {@code axis} the faces along it (the long sides of an arm, the rim of a plate) use {@code sideUv}
     * turned so the texture's v runs along the axis, and the two faces across it use {@code endUv} (none if null);
     * without an axis every face uses {@code sideUv}. {@code cull} culls the face on that block side.
     */
    private static <T extends ModelBuilder<T>> void box(T model, double[] b, Direction.Axis axis, String texture, float[] sideUv,
                                                        float[] endUv, Direction cull) {
        box(model, b, axis, texture, sideUv, endUv, cull, null);
    }

    /** {@link #box} turned as {@code bridge} says (the diagonal adapter pieces). */
    private static <T extends ModelBuilder<T>> void box(T model, double[] b, Direction.Axis axis, String texture, float[] sideUv,
                                                        float[] endUv, Direction cull, PipeParts.Bridge bridge) {
        var element = model.element().from((float) b[0], (float) b[1], (float) b[2]).to((float) b[3], (float) b[4], (float) b[5]);
        if (bridge != null) {
            element.rotation().angle(bridge.angle()).axis(bridge.rotationAxis())
                    .origin((float) bridge.origin()[0], (float) bridge.origin()[1], (float) bridge.origin()[2]).rescale(false).end();
        }
        for (Direction face : Direction.values()) {
            float[] uv;
            boolean turn = false;
            if (axis == null) {
                uv = sideUv;
            } else if (face.getAxis() == axis) {
                if (endUv == null) {
                    continue;
                }
                uv = endUv;
            } else {
                // the strip's u runs across the box, its v along the axis (as deep as the box is long)
                float length = (float) (axis == Direction.Axis.X ? b[3] - b[0] : axis == Direction.Axis.Y ? b[4] - b[1] : b[5] - b[2]);
                uv = new float[]{sideUv[0], sideUv[1], sideUv[2], sideUv[1] + Math.min(sideUv[3] - sideUv[1], length)};
                turn = !runsAlongV(face, axis);
            }
            var faceBuilder = element.face(face).uvs(uv[0], uv[1], uv[2], uv[3]).texture(texture);
            if (turn) {
                faceBuilder.rotation(ModelBuilder.FaceRotation.CLOCKWISE_90);
            }
            if (cull != null && face == cull) {
                faceBuilder.cullface(cull);
            }
            faceBuilder.end();
        }
        element.end();
    }

    /** Whether the texture's v direction on {@code face} runs along {@code axis} without turning. */
    private static boolean runsAlongV(Direction face, Direction.Axis axis) {
        return switch (axis) {
            case Y -> true;
            case X -> false;
            case Z -> face.getAxis() == Direction.Axis.Y;
        };
    }

    /** A straight piece through the centre with neutral plates at both ends, shown like a block. */
    private void itemModel(PipeKind kind, int tier, ResourceLocation texture) {
        ItemModelBuilder item = itemModels().getBuilder(kind.itemName(tier)).parent(new ModelFile.UncheckedModelFile(mcLoc("block/block")))
                .texture("pipe", texture).texture("plate", modLoc("block/pipe/connector_neutral")).texture("particle", texture);
        Lane lane = Lane.B;
        box(item, PipeParts.core(lane), null, "#pipe", CORE_UV, CORE_UV, null);
        for (Direction side : new Direction[]{Direction.NORTH, Direction.SOUTH}) {
            box(item, PipeParts.arm(lane, side), side.getAxis(), "#pipe", TUBE_UV, null, null);
            box(item, PipeParts.plate(lane, side), side.getAxis(), "#plate", PLATE_EDGE_UV, PLATE_FACE_UV, null);
        }
    }
}
