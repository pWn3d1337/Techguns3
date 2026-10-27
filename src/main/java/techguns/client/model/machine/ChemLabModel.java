package techguns.client.model.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Techguns 2 {@code ModelChemLab}: the lab bench, liquid filling the bottle, three glasses one after another and the
 * two tanks; the glassware is drawn translucent.
 */
// MIGRATION(26.x): ModelPart / LayerDefinition rendering changes with render states in 1.21.2+.
public final class ChemLabModel implements MachineModel {
    /** Liquid layers are stacked in half pixel steps (Techguns 2), 16 layers per glass. */
    private static final float LAYER = -0.03125f;
    private static final int LAYERS = 16;

    private final ModelPart base;
    private final ModelPart bottle;
    private final ModelPart glass;
    private final ModelPart[] glasses;
    private final ModelPart tankIn;
    private final ModelPart tankOut;

    private ChemLabModel(ModelPart root) {
        this.base = root.getChild("base");
        this.bottle = root.getChild("bottle");
        this.glass = root.getChild("glass");
        this.glasses = new ModelPart[]{root.getChild("l1"), root.getChild("l2"), root.getChild("l3")};
        this.tankIn = root.getChild("l7");
        this.tankOut = root.getChild("l8");
    }

    public static ChemLabModel bake() {
        return new ChemLabModel(createLayer().bakeRoot());
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition bottle = root.addOrReplaceChild("bottle", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition glass = root.addOrReplaceChild("glass", CubeListBuilder.create(), PartPose.ZERO);
        base.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 16F, 2F, 16F), PartPose.offsetAndRotation(-8F, 22F, -8F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(65, 5).addBox(0F, 0F, 0F, 6F, 6F, 1F), PartPose.offsetAndRotation(-3F, 13F, -8F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape3", CubeListBuilder.create().texOffs(0, 18).addBox(-2F, -2F, -2F, 5F, 5F, 5F), PartPose.offsetAndRotation(-5.5F, 15.5F, -0.5F, 0F, 0F, 0F));
        glass.addOrReplaceChild("g10", CubeListBuilder.create().texOffs(41, 22).addBox(0F, 0F, 0F, 4F, 9F, 4F), PartPose.offsetAndRotation(-7F, 12F, -7F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape4", CubeListBuilder.create().texOffs(0, 29).addBox(0F, 0F, 0F, 6F, 1F, 6F), PartPose.offsetAndRotation(-3F, 21F, -2.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape5", CubeListBuilder.create().texOffs(57, 29).addBox(0F, 0F, 0F, 5F, 1F, 5F), PartPose.offsetAndRotation(-7.5F, 21F, -7.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape6", CubeListBuilder.create().texOffs(79, 23).addBox(0F, 0F, 0F, 2F, 3F, 2F), PartPose.offsetAndRotation(-1F, 10.5F, -6F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape7", CubeListBuilder.create().texOffs(50, 0).addBox(-2F, -2F, -2F, 1F, 6F, 6F), PartPose.offsetAndRotation(-6F, 15F, -1F, 0F, 0F, 0F));
        root.addOrReplaceChild("l8", CubeListBuilder.create().texOffs(114, 27).addBox(0F, 0F, 0F, 3F, 1F, 3F), PartPose.offsetAndRotation(-6.5F, 19.5F, -6.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape8", CubeListBuilder.create().texOffs(41, 18).addBox(0F, 0F, 0F, 3F, 1F, 3F), PartPose.offsetAndRotation(-1.5F, 13F, -6.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape9", CubeListBuilder.create().texOffs(21, 33).addBox(0F, 0F, 0F, 5F, 9F, 5F), PartPose.offsetAndRotation(-2.5F, 13.5F, -7F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape10", CubeListBuilder.create().texOffs(0, 38).addBox(0F, 0F, 0F, 1F, 1F, 1F), PartPose.offsetAndRotation(2.5F, 21F, 3F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape11", CubeListBuilder.create().texOffs(57, 29).addBox(0F, 0F, 0F, 5F, 1F, 5F), PartPose.offsetAndRotation(2.5F, 21F, -7.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape12", CubeListBuilder.create().texOffs(50, 0).addBox(-2F, -2F, -2F, 1F, 6F, 6F), PartPose.offsetAndRotation(9F, 15F, -1F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape13", CubeListBuilder.create().texOffs(0, 18).addBox(-2F, -2F, -2F, 5F, 5F, 5F), PartPose.offsetAndRotation(4.5F, 15.5F, -0.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape14", CubeListBuilder.create().texOffs(51, 18).addBox(0F, 0F, 0F, 1F, 2F, 2F), PartPose.offsetAndRotation(-3F, 10.5F, -1F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape15", CubeListBuilder.create().texOffs(79, 18).addBox(0F, 0F, 0F, 12F, 2F, 2F), PartPose.offsetAndRotation(-6F, 8.5F, -6F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape16", CubeListBuilder.create().texOffs(88, 23).addBox(0F, 0F, 0F, 2F, 1F, 2F), PartPose.offsetAndRotation(-6F, 10.5F, -6F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape17", CubeListBuilder.create().texOffs(41, 18).addBox(0F, 0F, 0F, 3F, 1F, 3F), PartPose.offsetAndRotation(3.5F, 11F, -6.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape18", CubeListBuilder.create().texOffs(88, 23).addBox(0F, 0F, 0F, 2F, 1F, 2F), PartPose.offsetAndRotation(4F, 10.5F, -6F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape19", CubeListBuilder.create().texOffs(1, 30).addBox(0F, 0F, 0F, 1F, 3F, 1F), PartPose.offsetAndRotation(-2.5F, 18.5F, 1.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape20", CubeListBuilder.create().texOffs(80, 5).addBox(0F, 0F, 0F, 6F, 1F, 6F), PartPose.offsetAndRotation(-3F, 8F, -3F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape21", CubeListBuilder.create().texOffs(21, 18).addBox(0F, 0F, 0F, 5F, 10F, 5F), PartPose.offsetAndRotation(-2.5F, 8.5F, -2.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape22", CubeListBuilder.create().texOffs(106, 0).addBox(0F, 0F, 0F, 3F, 1F, 1F), PartPose.offsetAndRotation(-5.5F, 11F, -0.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape23", CubeListBuilder.create().texOffs(0, 37).addBox(0F, 0F, 0F, 5F, 1F, 5F), PartPose.offsetAndRotation(2.5F, 20F, 3F, 0F, 0F, 0F));
        glass.addOrReplaceChild("g3", CubeListBuilder.create().texOffs(87, 41).addBox(-1F, 0F, -1F, 2F, 9F, 2F), PartPose.offsetAndRotation(-6F, 12F, 5.5F, 0F, 0.785398F, 0F));
        glass.addOrReplaceChild("g9", CubeListBuilder.create().texOffs(12, 55).addBox(0F, 0F, 0F, 3F, 1F, 3F), PartPose.offsetAndRotation(3.5F, 19F, 4F, 0F, 0F, 0F));
        bottle.addOrReplaceChild("l5", CubeListBuilder.create().texOffs(107, 43).addBox(-1F, 0F, -1F, 2F, 1F, 2F), PartPose.offsetAndRotation(5F, 16F, 5.5F, 0F, 0F, 0F));
        bottle.addOrReplaceChild("l4", CubeListBuilder.create().texOffs(103, 46).addBox(0F, 0F, 0F, 3F, 2F, 3F), PartPose.offsetAndRotation(3.5F, 17F, 4F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape24", CubeListBuilder.create().texOffs(81, 38).addBox(-1F, 0F, -1F, 2F, 1F, 2F), PartPose.offsetAndRotation(-6F, 11F, 5.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape25", CubeListBuilder.create().texOffs(106, 0).addBox(0F, 0F, 0F, 1F, 2F, 1F), PartPose.offsetAndRotation(4.5F, 10F, 5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape26", CubeListBuilder.create().texOffs(1, 30).addBox(0F, 0F, 0F, 1F, 3F, 1F), PartPose.offsetAndRotation(1.5F, 18.5F, 1.5F, 0F, 0F, 0F));
        glass.addOrReplaceChild("g8", CubeListBuilder.create().texOffs(0, 55).addBox(0F, 0F, 0F, 4F, 2F, 4F), PartPose.offsetAndRotation(3F, 17F, 3.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape27", CubeListBuilder.create().texOffs(50, 41).addBox(0F, 0F, 0F, 10F, 10F, 0F), PartPose.offsetAndRotation(-8F, 12F, 7F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape28", CubeListBuilder.create().texOffs(0, 38).addBox(0F, 0F, 0F, 1F, 1F, 1F), PartPose.offsetAndRotation(6.5F, 21F, 3F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape29", CubeListBuilder.create().texOffs(0, 38).addBox(0F, 0F, 0F, 1F, 1F, 1F), PartPose.offsetAndRotation(6.5F, 21F, 7F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape30", CubeListBuilder.create().texOffs(0, 38).addBox(0F, 0F, 0F, 1F, 1F, 1F), PartPose.offsetAndRotation(2.5F, 21F, 7F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape31", CubeListBuilder.create().texOffs(50, 37).addBox(0F, 0F, 0F, 10F, 0F, 3F), PartPose.offsetAndRotation(-8F, 12F, 4F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape32", CubeListBuilder.create().texOffs(43, 37).addBox(0F, 0F, 0F, 0F, 10F, 3F), PartPose.offsetAndRotation(2F, 12F, 4F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape33", CubeListBuilder.create().texOffs(43, 37).addBox(0F, 0F, 0F, 0F, 10F, 3F), PartPose.offsetAndRotation(-8F, 12F, 4F, 0F, 0F, 0F));
        glass.addOrReplaceChild("g5", CubeListBuilder.create().texOffs(8, 45).addBox(-0.5F, 0F, -0.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(5F, 13F, 5.5F, 0F, 0.785398F, 0F));
        glass.addOrReplaceChild("g4", CubeListBuilder.create().texOffs(0, 44).addBox(-1F, 0F, -1F, 2F, 1F, 2F), PartPose.offsetAndRotation(5F, 12F, 5.5F, 0F, 0F, 0F));
        root.addOrReplaceChild("l3", CubeListBuilder.create().texOffs(123, 47).addBox(-0.5F, 0F, -0.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(-6F, 19.5F, 5.5F, 0F, 0.785398F, 0F));
        glass.addOrReplaceChild("g2", CubeListBuilder.create().texOffs(79, 41).addBox(-1F, 0F, -1F, 2F, 9F, 2F), PartPose.offsetAndRotation(-3F, 12F, 5.5F, 0F, 0.785398F, 0F));
        base.addOrReplaceChild("shape34", CubeListBuilder.create().texOffs(81, 32).addBox(-1F, 0F, -1F, 2F, 1F, 2F), PartPose.offsetAndRotation(0F, 11F, 5.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape35", CubeListBuilder.create().texOffs(81, 35).addBox(-1F, 0F, -1F, 2F, 1F, 2F), PartPose.offsetAndRotation(-3F, 11F, 5.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape36", CubeListBuilder.create().texOffs(41, 18).addBox(0F, 0F, 0F, 3F, 1F, 3F), PartPose.offsetAndRotation(-6.5F, 11F, -6.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape37", CubeListBuilder.create().texOffs(106, 0).addBox(0F, 0F, 0F, 1F, 1F, 10F), PartPose.offsetAndRotation(4.5F, 9F, -4F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape38", CubeListBuilder.create().texOffs(106, 0).addBox(0F, 0F, 0F, 1F, 1F, 1F), PartPose.offsetAndRotation(-0.5F, 10F, 5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape39", CubeListBuilder.create().texOffs(106, 0).addBox(0F, 0F, 0F, 1F, 1F, 1F), PartPose.offsetAndRotation(-5.5F, 10F, -0.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape40", CubeListBuilder.create().texOffs(106, 0).addBox(0F, 0F, 0F, 1F, 1F, 5F), PartPose.offsetAndRotation(-5.5F, 9F, -4.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape41", CubeListBuilder.create().texOffs(105, 3).addBox(0F, 0F, 0F, 2F, 1F, 1F), PartPose.offsetAndRotation(-7.5F, 9F, -0.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape42", CubeListBuilder.create().texOffs(106, 0).addBox(0F, 0F, 0F, 1F, 1F, 6F), PartPose.offsetAndRotation(-7.5F, 9F, 0F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape43", CubeListBuilder.create().texOffs(106, 0).addBox(0F, 0F, 0F, 7F, 1F, 1F), PartPose.offsetAndRotation(-6.5F, 9F, 5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape44", CubeListBuilder.create().texOffs(106, 0).addBox(0F, 0F, 0F, 1F, 1F, 1F), PartPose.offsetAndRotation(-6.5F, 10F, 5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape45", CubeListBuilder.create().texOffs(106, 0).addBox(0F, 0F, 0F, 1F, 1F, 1F), PartPose.offsetAndRotation(-3.5F, 10F, 5F, 0F, 0F, 0F));
        glass.addOrReplaceChild("g1", CubeListBuilder.create().texOffs(71, 41).addBox(-1F, 0F, -1F, 2F, 9F, 2F), PartPose.offsetAndRotation(0F, 12F, 5.5F, 0F, 0.785398F, 0F));
        root.addOrReplaceChild("l1", CubeListBuilder.create().texOffs(123, 41).addBox(-0.5F, 0F, -0.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, 19.5F, 5.5F, 0F, 0.785398F, 0F));
        root.addOrReplaceChild("l2", CubeListBuilder.create().texOffs(123, 44).addBox(-0.5F, 0F, -0.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(-3F, 19.5F, 5.5F, 0F, 0.785398F, 0F));
        glass.addOrReplaceChild("g7", CubeListBuilder.create().texOffs(0, 50).addBox(0F, 0F, 0F, 3F, 2F, 3F), PartPose.offsetAndRotation(3.5F, 15F, 4F, 0F, 0F, 0F));
        glass.addOrReplaceChild("g6", CubeListBuilder.create().texOffs(0, 47).addBox(-1F, 0F, -1F, 2F, 1F, 2F), PartPose.offsetAndRotation(5F, 14F, 5.5F, 0F, 0F, 0F));
        glass.addOrReplaceChild("g11", CubeListBuilder.create().texOffs(41, 22).addBox(0F, 0F, 0F, 4F, 9F, 4F), PartPose.offsetAndRotation(3F, 12F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("l7", CubeListBuilder.create().texOffs(114, 22).addBox(0F, 0F, 0F, 3F, 1F, 3F), PartPose.offsetAndRotation(3.5F, 19.5F, -6.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape46", CubeListBuilder.create().texOffs(50, 41).addBox(0F, 0F, 0F, 10F, 10F, 0F), PartPose.offsetAndRotation(-8F, 12F, 4F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float progress) {
        base.render(poseStack, consumer, light, overlay);
        if (progress <= 0.0f) {
            return;
        }
        bottle.render(poseStack, consumer, light, overlay);
        // the glasses fill one after another (from 0.2, 0.4, 0.6) and all empty in the last fifth
        for (int i = 0; i < glasses.length; i++) {
            float start = 0.2f * (i + 1);
            if (progress > start) {
                float fill = progress > 0.8f ? 1.0f - (progress - 0.8f) * 5.0f : (float) Math.sqrt(Math.min(1.0f, (progress - start) * 5.0f));
                renderLayers(glasses[i], poseStack, consumer, light, overlay, Math.round(fill * LAYERS));
            }
        }
        // the input tank empties in the first third, the output tank fills in the last third
        float in = progress < 0.333f ? 1.0f - Math.min(1.0f, progress * 3.0f) : 0.0f;
        float out = progress < 0.666f ? 0.0f : Math.min(1.0f, (progress - 0.666f) * 3.0f);
        renderLayers(tankIn, poseStack, consumer, light, overlay, Math.round(in * LAYERS));
        renderLayers(tankOut, poseStack, consumer, light, overlay, Math.round(out * LAYERS));
    }

    @Override
    public void renderTranslucent(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float progress) {
        glass.render(poseStack, consumer, light, overlay);
    }

    private static void renderLayers(ModelPart part, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int count) {
        poseStack.pushPose();
        for (int i = 0; i < count; i++) {
            part.render(poseStack, consumer, light, overlay);
            poseStack.translate(0.0f, LAYER, 0.0f);
        }
        poseStack.popPose();
    }
}
