package techguns.client.model.slot;

import java.util.function.Supplier;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Techguns 2 extra-slot models ({@code client/models/armor}: ModelGasMask, ModelFaceMask, ModelNightVisionGoggles,
 * ModelJetPack, ModelGlider, ModelOxygenTanks, ModelAntiGravPack, ModelGloves) as vanilla parts, baked on first use.
 * All 1.12 boxes were added before {@code mirror = true}, so none is mirrored except the left glove arm.
 */
// MIGRATION(26.x): LayerDefinition / ModelPart APIs change (render states) in 1.21.2+.
public final class SlotGearModels {
    /** Glider wing parts that only show while gliding ({@code BackBack}, the pack itself, is always visible). */
    public static final String[] GLIDER_WINGS = {"MidR01", "TopR01", "TopR02", "MidR02", "TopL01", "TopL02", "InnerR02", "MidL02",
            "MidL01", "InnerR01", "InnerL02", "InnerL01"};

    private static final String[] GLIDER_PARTS = new String[GLIDER_WINGS.length + 1];

    static {
        GLIDER_PARTS[0] = "BackBack";
        System.arraycopy(GLIDER_WINGS, 0, GLIDER_PARTS, 1, GLIDER_WINGS.length);
    }

    private static final Supplier<SlotGearModel> GAS_MASK = lazy(SlotGearModel.Attach.HEAD, SlotGearModels::gasMaskDefinition);
    private static final Supplier<SlotGearModel> FACE_MASK = lazy(SlotGearModel.Attach.HEAD, SlotGearModels::faceMaskDefinition);
    private static final Supplier<SlotGearModel> NIGHT_VISION = lazy(SlotGearModel.Attach.HEAD, SlotGearModels::nightVisionDefinition);
    private static final Supplier<SlotGearModel> JETPACK = lazy(SlotGearModel.Attach.BACK, () -> jetPackDefinition(0), "BackBack");
    private static final Supplier<SlotGearModel> JUMP_PACK = lazy(SlotGearModel.Attach.BACK, () -> jetPackDefinition(1), "BackBack");
    private static final Supplier<SlotGearModel> OXYGEN_TANKS = lazy(SlotGearModel.Attach.BACK, SlotGearModels::oxygenTanksDefinition, "BackBack");
    private static final Supplier<SlotGearModel> ANTI_GRAV_PACK = lazy(SlotGearModel.Attach.BACK, SlotGearModels::antiGravPackDefinition, "BackBack");
    private static final Supplier<SlotGearModel> GLIDER = lazy(SlotGearModel.Attach.BACK, SlotGearModels::gliderDefinition, GLIDER_PARTS);
    private static final Supplier<SlotGearModel> GLOVES = lazy(SlotGearModel.Attach.ARMS, () -> glovesDefinition(false));
    private static final Supplier<SlotGearModel> GLOVES_SLIM = lazy(SlotGearModel.Attach.ARMS, () -> glovesDefinition(true));

    private SlotGearModels() {
    }

    public static SlotGearModel gasMask() {
        return GAS_MASK.get();
    }

    /** Techguns 2 {@code ModelFaceMask(true)} (tactical mask, oxygen mask). */
    public static SlotGearModel faceMask() {
        return FACE_MASK.get();
    }

    public static SlotGearModel nightVisionGoggles() {
        return NIGHT_VISION.get();
    }

    public static SlotGearModel jetPack() {
        return JETPACK.get();
    }

    public static SlotGearModel jumpPack() {
        return JUMP_PACK.get();
    }

    public static SlotGearModel oxygenTanks() {
        return OXYGEN_TANKS.get();
    }

    public static SlotGearModel antiGravPack() {
        return ANTI_GRAV_PACK.get();
    }

    public static SlotGearModel glider() {
        return GLIDER.get();
    }

    public static SlotGearModel gloves(boolean slim) {
        return (slim ? GLOVES_SLIM : GLOVES).get();
    }

    private static Supplier<SlotGearModel> lazy(SlotGearModel.Attach attach, Supplier<LayerDefinition> definition, String... followers) {
        return new Supplier<>() {
            private SlotGearModel baked;

            @Override
            public SlotGearModel get() {
                if (baked == null) {
                    baked = new SlotGearModel(definition.get().bakeRoot(), attach, followers);
                }
                return baked;
            }
        };
    }

    /** One 1.12 ModelRenderer with a single box (rotation point, rotation, box), added below {@code parent}. */
    private static PartDefinition box(PartDefinition parent, String name, int u, int v, float px, float py, float pz,
                                      float rx, float ry, float rz, float x, float y, float z, int w, int h, int d) {
        return box(parent, name, u, v, false, px, py, pz, rx, ry, rz, x, y, z, w, h, d, CubeDeformation.NONE);
    }

    private static PartDefinition box(PartDefinition parent, String name, int u, int v, boolean mirror, float px, float py, float pz,
                                      float rx, float ry, float rz, float x, float y, float z, int w, int h, int d, CubeDeformation inflate) {
        return parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).mirror(mirror).addBox(x, y, z, w, h, d, inflate),
                PartPose.offsetAndRotation(px, py, pz, rx, ry, rz));
    }

    /** The 8x8x8 head box every face model starts with, below the {@code head} part. */
    private static PartDefinition headBase(MeshDefinition mesh) {
        PartDefinition head = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        box(head, "Head", 0, 0, 0, 0, 0, 0, 0, 0, -4, -8, -4, 8, 8, 8);
        return head;
    }

    private static LayerDefinition gasMaskDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition head = headBase(mesh);
        box(head, "Front", 9, 17, -2, -3, -5, 0, 0, 0, 0, 0, 0, 4, 3, 1);
        box(head, "FilterMount", 0, 17, -1.5f, -2.2f, -8, 0.3346075f, 0, 0, 0, 0, 0, 3, 3, 1);
        box(head, "Filter", 20, 17, 0, -0.9f, -7, 0.3490659f, -0.2268928f, 0.7853982f, -1, -1, 0, 2, 2, 2);
        return LayerDefinition.create(mesh, 32, 32);
    }

    private static LayerDefinition faceMaskDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition head = headBase(mesh);
        box(head, "Front", 9, 17, -2, -3, -5, 0, 0, 0, 0, 0, 0, 4, 3, 1);
        return LayerDefinition.create(mesh, 32, 32);
    }

    private static LayerDefinition nightVisionDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition head = headBase(mesh);
        // goggles on the eyes
        box(head, "Right", 0, 17, -1.7f, -4, -8, 0, 0, 0.7853982f, -1, -1, 0, 2, 2, 4);
        box(head, "Left", 0, 17, 1.7f, -4, -8, 0, 0, 0.7853982f, -1, -1, 0, 2, 2, 4);
        box(head, "Mount", 13, 21, 0, -5, -6.5f, 0.7853982f, 0, 0, -1, 0, 0, 2, 1, 4);
        box(head, "Mid", 13, 17, -1.5f, -5, -7, 0, 0, 0, 0, 0, 0, 3, 1, 2);
        // pushed up on the forehead
        box(head, "Right_off", 0, 17, -1.7f, -9, -5, -2.356194f, -0.5410521f, 0.6108652f, -1, -1, -2, 2, 2, 4);
        box(head, "Left_off", 0, 17, 1.7f, -9, -5, -2.356194f, -0.5410521f, 0.6108652f, -1, -1, -2, 2, 2, 4);
        box(head, "Mount_off", 13, 21, 0, -6, -3.5f, -1.301251f, 0, 0, -1, 0, -4, 2, 1, 4);
        box(head, "Mid_off", 13, 17, -0.5f, -10, -5.5f, 0.6108652f, 0, 0, -1, 0, -1, 3, 1, 2);
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** Techguns 2 {@code ModelJetPack(variant)}: variant 0 (jetpack) adds the side wings. */
    private static LayerDefinition jetPackDefinition(int variant) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition back = box(mesh.getRoot(), "BackBack", 16, 0, 0, 1, 2, 0, 0, 0, -2, 0, 0, 4, 7, 4);
        PartDefinition rocketR = box(back, "RocketR", 0, 0, -2, -1, 0.5f, 0, 0, 0, -3, 0, 0, 3, 9, 3);
        PartDefinition rocketL = box(back, "RocketL", 0, 0, 5, -1, 0.5f, 0, 0, 0, -3, 0, 0, 3, 9, 3);
        box(rocketR, "RocketTopR", 0, 13, -3.5f, -2, 4, 0, 0, 0, 1, 0, -3.5f, 2, 2, 2);
        box(rocketL, "RocketTopL", 0, 13, 3.5f, -2, 4, 0, 0, 0, -6, 0, -3.5f, 2, 2, 2);
        if (variant == 0) {
            PartDefinition innerR = box(back, "InnerR02", 0, 24, -10, 2, 3.5f, 0, 0, 0, 0, -1, -2, 5, 6, 1);
            box(innerR, "TopR02", 13, 15, -10, 1, 3, 0, 0, 0, 10, -3, -5.5f, 5, 1, 2);
            box(innerR, "MidR02", 21, 19, -11, 2, 3, 0, 0, 0, 10, -3, -5.5f, 1, 6, 2);
            PartDefinition innerL = box(back, "InnerL02", 0, 24, 5, 2, 3.5f, 0, 0, 0, 0, -1, -2, 5, 6, 1);
            box(innerL, "TopL02", 13, 15, 5, 1, 3, 0, 0, 0, -5, -3, -5.5f, 5, 1, 2);
            box(innerL, "MidL02", 21, 19, 10, 2, 3, 0, 0, 0, -5, -3, -5.5f, 1, 6, 2);
        }
        return LayerDefinition.create(mesh, 32, 32);
    }

    private static LayerDefinition oxygenTanksDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition back = box(mesh.getRoot(), "BackBack", 16, 0, 1, 1, 2, 0, 0, 0, -2, 0, 0, 2, 6, 3);
        box(back, "RocketTopR", 0, 13, -2.5f, -1, 4, 0, 0, 0, -2, -1, -3, 2, 1, 2);
        box(back, "RocketR", 0, 0, -1, -1, 0.5f, 0, 0, 0, -4, 0, 0, 3, 9, 3);
        box(back, "RocketL", 0, 0, 4, -1, 0.5f, 0, 0, 0, -4, 0, 0, 3, 9, 3);
        box(back, "RocketTopL", 0, 13, 2.5f, -1, 4, 0, 0, 0, -2, -1, -3, 2, 1, 2);
        return LayerDefinition.create(mesh, 32, 32);
    }

    private static LayerDefinition antiGravPackDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        float pi = 3.141592653589793f;
        PartDefinition back = box(mesh.getRoot(), "BackBack", 16, 0, -1, 1, 2, 0, 0, 0, -2, 0, 0, 6, 6, 3);
        box(back, "RocketL", 0, 0, 6, -0.5f, 0.5f, 0, 0, 0, -2, 0, 0, 2, 7, 2);
        box(back, "box1", 0, 10, 5, 3.5f, 2.5f, 0, 0, 0, -2, 0, 0, 2, 1, 1);
        box(back, "Back3", 6, 10, 0, 4, 6, 0, 0, 0.7853981633974483f, -1.5f, -3, -3.5f, 3, 3, 3);
        box(back, "Back2", 20, 11, -0.5f, 2, 5, 0, 0, 0, -1, -1, -2, 5, 4, 1);
        box(back, "box3", 0, 10, -2, 4.5f, 5.5f, 0, pi, 0, -1, -1, 2, 2, 1, 1);
        box(back, "RocketR", 0, 0, -2, -0.5f, 0.5f, 0, 0, 0, -2, 0, 0, 2, 7, 2);
        box(back, "box4", 0, 10, -2, 2.5f, 5.5f, 0, pi, 0, -1, -1, 2, 2, 1, 1);
        box(back, "box2", 0, 10, 5, 1.5f, 2.5f, 0, 0, 0, -2, 0, 0, 2, 1, 1);
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** Techguns 2 {@code ModelGlider}: a pack plus loose wing parts, each rotated by the body's X rotation on its own. */
    private static LayerDefinition gliderDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        box(root, "BackBack", 0, 0, -0.5f, 0, 0, 0, 0, 0, -3, 0.5f, 2, 7, 10, 4);
        box(root, "MidR01", 23, 0, -9.5f, 3, 3, 0, 0, 0, 0, -1, 1, 1, 7, 2);
        box(root, "TopR01", 13, 15, -9.5f, 2, 3, 0, 0, 0, 0, -1, 1, 6, 1, 2);
        box(root, "TopR02", 13, 15, -15.5f, 3, 3, 0, 0, 0, 0, -1, 1, 6, 1, 2);
        box(root, "MidR02", 21, 19, -16.5f, 4, 3, 0, 0, 0, 0, -1, 1, 1, 6, 2);
        box(root, "TopL01", 13, 15, 3.5f, 2, 3, 0, 0, 0, 0, -1, 1, 6, 1, 2);
        box(root, "TopL02", 13, 15, 9.5f, 3, 3, 0, 0, 0, 0, -1, 1, 6, 1, 2);
        box(root, "InnerR02", 0, 24, -15.5f, 4, 3.5f, 0, 0, 0, 0, -1, 1, 6, 6, 1);
        box(root, "MidL02", 21, 19, 15.5f, 4, 3, 0, 0, 0, 0, -1, 1, 1, 6, 2);
        box(root, "MidL01", 23, 0, 8.5f, 3, 3, 0, 0, 0, 0, -1, 1, 1, 7, 2);
        box(root, "InnerR01", 0, 15, -8.5f, 3, 3.5f, 0, 0, 0, 0, -1, 1, 5, 7, 1);
        box(root, "InnerL02", 0, 24, 9.5f, 4, 3.5f, 0, 0, 0, 0, -1, 1, 6, 6, 1);
        box(root, "InnerL01", 0, 15, 3.5f, 3, 3.5f, 0, 0, 0, 0, -1, 1, 5, 7, 1);
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** Techguns 2 {@code ModelGloves(0.45, smallArms)}: inflated arm boxes on a 16x16 texture layout. */
    private static LayerDefinition glovesDefinition(boolean slim) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation inflate = new CubeDeformation(0.45f);
        if (slim) {
            box(root, "left_arm", 0, 0, true, 5, 2.5f, 0, 0, 0, 0, -1, -2, -2, 3, 12, 4, inflate);
            box(root, "right_arm", 0, 0, false, -5, 2.5f, 0, 0, 0, 0, -2, -2, -2, 3, 12, 4, inflate);
        } else {
            box(root, "right_arm", 0, 0, false, -5, 2, 0, 0, 0, 0, -3, -2, -2, 4, 12, 4, inflate);
            box(root, "left_arm", 0, 0, true, 5, 2, 0, 0, 0, 0, -1, -2, -2, 4, 12, 4, inflate);
        }
        return LayerDefinition.create(mesh, 16, 16);
    }
}
