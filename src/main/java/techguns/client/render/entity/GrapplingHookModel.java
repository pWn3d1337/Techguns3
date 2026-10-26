package techguns.client.render.entity;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;

/** Hook of the grappling hook projectile (Fabric {@code ModelGrapplingHookProjectile}). */
class GrapplingHookModel extends TGModel {
    private static final float QUARTER_PI = 0.7853981633974483F;

    private final TGModelPart hook01;
    private final TGModelPart hook01_1;
    private final TGModelPart hook02;
    private final TGModelPart hook03;
    private final TGModelPart hook04;
    private final TGModelPart hook05;
    private final TGModelPart hook06;

    GrapplingHookModel() {
        super(RenderType::entitySolid);
        textureWidth = 64;
        textureHeight = 32;

        hook05 = new TGModelPart(this, 0, 0);
        hook05.setPivot(1.4F, 0.0F, 0.0F);
        hook05.addCuboid(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, -0.2F, -0.2F, -0.2F);
        setRotation(hook05, 0.0F, 0.0F, QUARTER_PI);
        hook06 = new TGModelPart(this, 0, 0);
        hook06.setPivot(1.4F, 0.0F, 0.0F);
        hook06.addCuboid(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, -0.2F, -0.2F, -0.2F);
        setRotation(hook06, 0.0F, -QUARTER_PI, 0.0F);
        hook01_1 = new TGModelPart(this, 19, 20);
        hook01_1.setPivot(3.0F, 0.0F, 0.0F);
        hook01_1.addCuboid(0.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F);
        setRotation(hook01_1, QUARTER_PI, 0.0F, 0.0F);
        hook04 = new TGModelPart(this, 0, 0);
        hook04.setPivot(1.4F, 0.0F, 0.0F);
        hook04.addCuboid(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, -0.2F, -0.2F, -0.2F);
        setRotation(hook04, 0.0F, QUARTER_PI, 0.0F);
        hook03 = new TGModelPart(this, 0, 0);
        hook03.setPivot(1.4F, 0.0F, 0.0F);
        hook03.addCuboid(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, -0.2F, -0.2F, -0.2F);
        setRotation(hook03, 0.0F, 0.0F, -QUARTER_PI);
        hook02 = new TGModelPart(this, 24, 19);
        hook02.setPivot(1.0F, 0.0F, 0.0F);
        hook02.addCuboid(0.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F);
        setRotation(hook02, QUARTER_PI, 0.0F, 0.0F);
        hook01 = new TGModelPart(this, 31, 19);
        hook01.setPivot(0.0F, 0.0F, 0.0F);
        hook01.addCuboid(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, 0.0F, -0.3F, -0.3F);
        setRotation(hook01, QUARTER_PI, 0.0F, 0.0F);
    }

    @Override
    public void render(@Nullable LivingEntity entity, PoseStack poseStack, VertexConsumer vertices, int ammoLeft, float reloadProgress,
                       ItemDisplayContext context, int part, float fireProgress, float chargeProgress, int light, int overlay) {
        hook05.render(poseStack, vertices, light, overlay);
        hook06.render(poseStack, vertices, light, overlay);
        hook01_1.render(poseStack, vertices, light, overlay);
        hook04.render(poseStack, vertices, light, overlay);
        hook03.render(poseStack, vertices, light, overlay);
        hook02.render(poseStack, vertices, light, overlay);
        hook01.render(poseStack, vertices, light, overlay);
    }
}
