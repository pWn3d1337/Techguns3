package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;

public class ModelLaserPistol extends TGModel {
    public TGModelPart Barrel1;
    public TGModelPart Grip1;
    public TGModelPart Trigger;
    public TGModelPart grip2;
    public TGModelPart Barrel2;
    public TGModelPart BottomRound;
    public TGModelPart grip3;
    public TGModelPart BarrelFront;
    public TGModelPart BottomFront;
    public TGModelPart Front1;
    public TGModelPart Front2;
    public TGModelPart Barrel4;
    public TGModelPart Barrel5;
    public TGModelPart Cylinder1;
    public TGModelPart Cylinder2;
    public TGModelPart Side1;
    public TGModelPart CylinderFront;
    public TGModelPart TopRound;

    public ModelLaserPistol() {
        super(RenderType::entitySolid);
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.Grip1 = new TGModelPart(this, 37, 7);
        this.Grip1.setPivot(-1.5F, 3.0F, 7.0F);
        this.Grip1.addCuboid(0.0F, 0.0F, 0.0F, 2, 6, 3, 0.0F);
        this.setRotation(Grip1, 0.2181661564992912F, -0.0F, 0.0F);
        this.grip3 = new TGModelPart(this, 56, 1);
        this.grip3.setPivot(-1.5F, 8.0F, 5.5F);
        this.grip3.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 2, 0.0F);
        this.grip2 = new TGModelPart(this, 37, 0);
        this.grip2.setPivot(-2.0F, 8.0F, 7.5F);
        this.grip2.addCuboid(0.0F, 0.0F, 0.0F, 3, 2, 4, 0.0F);
        this.Trigger = new TGModelPart(this, 58, 8);
        this.Trigger.setPivot(-1.0F, 3.0F, 6.0F);
        this.Trigger.addCuboid(0.0F, 0.0F, 0.0F, 1, 2, 2, 0.0F);
        this.BarrelFront = new TGModelPart(this, 30, 6);
        this.BarrelFront.setPivot(-1.5F, 3.8F, -3.0F);
        this.BarrelFront.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 2, 0.0F);
        this.CylinderFront = new TGModelPart(this, 8, 0);
        this.CylinderFront.setPivot(-2.8F, -0.8F, -3.1F);
        this.CylinderFront.addCuboid(-0.5F, -0.5F, 0.0F, 1, 1, 4, 0.0F);
        this.setRotation(CylinderFront, 0.0F, 0.0F, 0.7853981633974483F);
        this.BottomFront = new TGModelPart(this, 28, 10);
        this.BottomFront.setPivot(-0.5F, 8.3F, -2.5F);
        this.BottomFront.addCuboid(-1.0F, -1.0F, 0.0F, 2, 2, 2, 0.0F);
        this.setRotation(BottomFront, 0.0F, 0.0F, 0.7853981633974483F);
        this.Barrel5 = new TGModelPart(this, 9, 26);
        this.Barrel5.setPivot(-2.0F, -1.5F, 6.0F);
        this.Barrel5.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 4, 0.0F);
        this.Cylinder2 = new TGModelPart(this, 19, 0);
        this.Cylinder2.setPivot(-3.0F, 3.0F, 6.0F);
        this.Cylinder2.addCuboid(-0.5F, -0.5F, 0.0F, 1, 1, 5, 0.0F);
        this.setRotation(Cylinder2, 0.0F, 0.0F, 0.7853981633974483F);
        this.Barrel2 = new TGModelPart(this, 44, 22);
        this.Barrel2.setPivot(-3.0F, -1.2F, -3.0F);
        this.Barrel2.addCuboid(0.0F, 0.0F, 0.0F, 5, 5, 5, 0.0F);
        this.Cylinder1 = new TGModelPart(this, 19, 0);
        this.Cylinder1.setPivot(2.0F, 3.0F, 6.0F);
        this.Cylinder1.addCuboid(-0.5F, -0.5F, 0.0F, 1, 1, 5, 0.0F);
        this.setRotation(Cylinder1, 0.0F, 0.0F, 0.7853981633974483F);
        this.Front2 = new TGModelPart(this, 34, 0);
        this.Front2.mirror = true;
        this.Front2.setPivot(0.3F, 4.3F, -2.0F);
        this.Front2.addCuboid(0.0F, 0.0F, 0.0F, 0, 4, 1, 0.0F);
        this.BottomRound = new TGModelPart(this, 25, 15);
        this.BottomRound.setPivot(-0.5F, 9.0F, -0.5F);
        this.BottomRound.addCuboid(-0.5F, -0.5F, 0.0F, 1, 1, 6, 0.0F);
        this.setRotation(BottomRound, 0.0F, 0.0F, 0.7853981633974483F);
        this.Front1 = new TGModelPart(this, 34, 0);
        this.Front1.setPivot(-1.3F, 4.3F, -2.0F);
        this.Front1.addCuboid(0.0F, 0.0F, 0.0F, 0, 4, 1, 0.0F);
        this.Side1 = new TGModelPart(this, 17, 8);
        this.Side1.setPivot(2.0F, -0.7F, 10.0F);
        this.Side1.addCuboid(-2.0F, 0.0F, -3.0F, 2, 3, 3, 0.0F);
        this.setRotation(Side1, 0.0F, -0.2617993877991494F, 0.0F);
        this.Barrel4 = new TGModelPart(this, 25, 23);
        this.Barrel4.setPivot(-3.0F, -1.5F, 10.0F);
        this.Barrel4.addCuboid(0.0F, 0.0F, 0.0F, 5, 5, 4, 0.0F);
        this.Barrel1 = new TGModelPart(this, 40, 9);
        this.Barrel1.setPivot(-2.5F, -1.0F, 2.0F);
        this.Barrel1.addCuboid(0.0F, 0.0F, 0.0F, 4, 4, 8, 0.0F);
        this.TopRound = new TGModelPart(this, 28, 0);
        this.TopRound.setPivot(-1.0F, -1.8F, 8.5F);
        this.TopRound.addCuboid(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
    }
	@Override
	public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
		float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
		int overlay) {

        this.Grip1.render(matrices, vertices, light, overlay);
        this.grip3.render(matrices, vertices, light, overlay);
        this.grip2.render(matrices, vertices, light, overlay);
        this.Trigger.render(matrices, vertices, light, overlay);
        this.BarrelFront.render(matrices, vertices, light, overlay);
        this.CylinderFront.render(matrices, vertices, light, overlay);
        this.BottomFront.render(matrices, vertices, light, overlay);
        this.Barrel5.render(matrices, vertices, light, overlay);
        this.Cylinder2.render(matrices, vertices, light, overlay);
        this.Barrel2.render(matrices, vertices, light, overlay);
        this.Cylinder1.render(matrices, vertices, light, overlay);
        this.Front2.render(matrices, vertices, light, overlay);
        this.BottomRound.render(matrices, vertices, light, overlay);
        this.Front1.render(matrices, vertices, light, overlay);
        this.Side1.render(matrices, vertices, light, overlay);
        this.Barrel4.render(matrices, vertices, light, overlay);
        this.Barrel1.render(matrices, vertices, light, overlay);
        this.TopRound.render(matrices, vertices, light, overlay);
    }
}


