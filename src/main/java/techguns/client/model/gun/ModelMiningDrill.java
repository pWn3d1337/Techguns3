package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;
import techguns.client.render.TGMatrixOps;

public class ModelMiningDrill extends TGModel {
	private final HeadPart headPart = new HeadPart();

	/** Render part 1 is the nested drill head model. */
	@Override
	public TGModel partModel(int part) {
		return part == 1 ? headPart : this;
	}

	/** The drill head turns once per recoil animation. */
	@Override
	public void transformPart(PoseStack matrices, @Nullable LivingEntity entity, ItemDisplayContext context, int part, int ammoLeft,
			float reloadProgress, float fireProgress) {
		if (part == 1) {
			matrices.translate(0, 0.1d, 0.04d);
			TGMatrixOps.rotate(matrices, 360f * fireProgress, 1, 0, 0);
			matrices.translate(0, -0.1d, -0.04d);
		}
	}

	TGModelPart Shape33;
	TGModelPart Shape32;
	TGModelPart Shape135;
	TGModelPart Shape9;
	TGModelPart Shape31;
	TGModelPart Shape134;
	TGModelPart Grip02;
	TGModelPart Shape13;
	TGModelPart Shape151;
	TGModelPart Shape34;
	TGModelPart Shape92;
	TGModelPart Shape132;
	TGModelPart Grip;
	TGModelPart Shape91;
	TGModelPart Shape15;
	TGModelPart Shape131_1;
	TGModelPart Grip01;
	TGModelPart Grip2_03;
	TGModelPart Grip03_1;
	TGModelPart Shape93;
	TGModelPart Shape136;
	TGModelPart Shape131;
	TGModelPart Grip03;
	TGModelPart Shape152;
	TGModelPart Grip2_01;
	TGModelPart Shape133;
	TGModelPart Grip2_02;

	public ModelMiningDrill() {
		super(RenderType::entitySolid);
		textureWidth = 64;
		textureHeight = 64;
		this.Shape33 = new TGModelPart(this, 0, 27);
		this.Shape33.setPivot(3.0F, -1.5F, -1.5F);
		this.Shape33.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 4, 0.0F);
		this.Shape32 = new TGModelPart(this, 1, 32);
		this.Shape32.setPivot(3.0F, 4.0F, -1.0F);
		this.Shape32.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 3, 0.0F);
		this.Shape135 = new TGModelPart(this, 0, 36);
		this.Shape135.setPivot(-9.0F, -2.0F, -1.5F);
		this.Shape135.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 4, 0.0F);
		this.Shape9 = new TGModelPart(this, 45, 25);
		this.Shape9.setPivot(5.0F, -0.5F, -3.0F);
		this.Shape9.addCuboid(-1.0F, -4.0F, 0.0F, 1, 4, 7, 0.0F);
		this.setRotation(Shape9, 0.0F, 0.0F, -0.39269908169872414F);
		this.Shape31 = new TGModelPart(this, 0, 27);
		this.Shape31.setPivot(3.0F, 3.5F, -1.5F);
		this.Shape31.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 4, 0.0F);
		this.Shape134 = new TGModelPart(this, 0, 36);
		this.Shape134.setPivot(-1.5F, -1.9F, -1.5F);
		this.Shape134.addCuboid(0.0F, 0.0F, 0.0F, 5, 1, 4, 0.0F);
		this.Grip02 = new TGModelPart(this, 12, 14);
		this.Grip02.setPivot(-12.0F, -2.0F, -0.5F);
		this.Grip02.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 2, 0.0F);
		this.Shape13 = new TGModelPart(this, 24, 36);
		this.Shape13.setPivot(-9.0F, 3.0F, -1.5F);
		this.Shape13.addCuboid(0.0F, 0.0F, 0.0F, 12, 1, 4, 0.0F);
		this.Shape151 = new TGModelPart(this, 47, 0);
		this.Shape151.setPivot(-7.0F, -2.5F, -2.5F);
		this.Shape151.addCuboid(0.0F, 0.0F, 0.0F, 1, 2, 6, 0.0F);
		this.Shape34 = new TGModelPart(this, 1, 32);
		this.Shape34.setPivot(3.0F, -2.0F, -1.0F);
		this.Shape34.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 3, 0.0F);
		this.Shape92 = new TGModelPart(this, 13, 26);
		this.Shape92.setPivot(4.0F, -0.5F, -2.5F);
		this.Shape92.addCuboid(0.0F, 0.0F, 0.0F, 2, 4, 6, 0.0F);
		this.Shape132 = new TGModelPart(this, 0, 20);
		this.Shape132.setPivot(-6.0F, -2.0F, -1.5F);
		this.Shape132.addCuboid(0.0F, 0.0F, 0.0F, 4, 1, 4, 0.0F);
		this.Grip = new TGModelPart(this, 47, 42);
		this.Grip.setPivot(0.5F, 1.9F, 3.5F);
		this.Grip.addCuboid(-1.0F, -1.0F, 0.0F, 1, 1, 5, 0.0F);
		this.setRotation(Grip, 0.0F, -0.0F, 0.7853981633974483F);
		this.Shape91 = new TGModelPart(this, 29, 23);
		this.Shape91.setPivot(4.0F, -0.5F, -3.0F);
		this.Shape91.addCuboid(0.0F, 0.0F, 0.0F, 1, 6, 7, 0.0F);
		this.Shape15 = new TGModelPart(this, 27, 9);
		this.Shape15.setPivot(-6.5F, -0.5F, -2.5F);
		this.Shape15.addCuboid(0.0F, 0.0F, 0.0F, 11, 4, 6, 0.0F);
		this.Shape131_1 = new TGModelPart(this, 10, 41);
		this.Shape131_1.setPivot(-9.0F, -1.0F, -2.0F);
		this.Shape131_1.addCuboid(0.0F, 0.0F, 0.0F, 13, 4, 5, 0.0F);
		this.Grip01 = new TGModelPart(this, 0, 10);
		this.Grip01.setPivot(-17.0F, 2.9F, -0.5F);
		this.Grip01.addCuboid(0.0F, 0.0F, 0.0F, 9, 1, 2, 0.0F);
		this.Grip2_03 = new TGModelPart(this, 0, 4);
		this.Grip2_03.setPivot(0.0F, -5.3F, -2.1F);
		this.Grip2_03.addCuboid(0.0F, 0.0F, 0.0F, 1, 5, 1, 0.0F);
		this.Grip03_1 = new TGModelPart(this, 39, 26);
		this.Grip03_1.setPivot(-15.8F, -0.6F, -0.43F);
		this.Grip03_1.addCuboid(-4.0F, 0.0F, 0.0F, 4, 1, 2, 0.0F);
		this.setRotation(Grip03_1, 0.0F, 0.0F, -1.265363707695889F);
		this.Shape93 = new TGModelPart(this, 54, 49);
		this.Shape93.setPivot(0.5F, 0.9F, 2.5F);
		this.Shape93.addCuboid(-1.5F, -1.5F, 0.0F, 3, 3, 2, 0.0F);
		this.Shape136 = new TGModelPart(this, 0, 14);
		this.Shape136.setPivot(-5.5F, -2.5F, -1.0F);
		this.Shape136.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 3, 0.0F);
		this.Shape131 = new TGModelPart(this, 0, 50);
		this.Shape131.setPivot(-5.0F, 4.0F, -1.0F);
		this.Shape131.addCuboid(0.0F, 0.0F, 0.0F, 8, 1, 3, 0.0F);
		this.Grip03 = new TGModelPart(this, 39, 23);
		this.Grip03.setPivot(-12.0F, -2.0F, -0.45F);
		this.Grip03.addCuboid(-4.0F, 0.0F, 0.0F, 4, 1, 2, 0.0F);
		this.setRotation(Grip03, 0.0F, 0.0F, -0.36128315516282616F);
		this.Shape152 = new TGModelPart(this, 47, 0);
		this.Shape152.setPivot(-2.0F, -2.5F, -2.5F);
		this.Shape152.addCuboid(0.0F, 0.0F, 0.0F, 1, 2, 6, 0.0F);
		this.Grip2_01 = new TGModelPart(this, 47, 42);
		this.Grip2_01.setPivot(0.5F, -4.5F, -2.0F);
		this.Grip2_01.addCuboid(-1.0F, -1.0F, 0.0F, 1, 1, 5, 0.0F);
		this.setRotation(Grip2_01, 0.0F, -0.0F, 0.7853981633974483F);
		this.Shape133 = new TGModelPart(this, 0, 41);
		this.Shape133.setPivot(-10.0F, -1.5F, -1.5F);
		this.Shape133.addCuboid(0.0F, 0.0F, 0.0F, 1, 5, 4, 0.0F);
		this.Grip2_02 = new TGModelPart(this, 0, 4);
		this.Grip2_02.setPivot(0.0F, -5.3F, 2.1F);
		this.Grip2_02.addCuboid(0.0F, 0.0F, 0.0F, 1, 5, 1, 0.0F);
		// Tabula scaled these grips (formerly pose stack code in render)
		this.Grip.setRenderScale(1.5f, 1.5f, 1.0f);
		this.Grip03.setRenderScale(1.0f, 1.0f, 0.98f);
		this.Grip03_1.setRenderScale(1.0f, 1.0f, 0.96f);
	}

	@Override
	public void render(@Nullable LivingEntity entity, PoseStack matrices, VertexConsumer vertices, int ammoLeft, float reloadProgress,
			ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
		if (part == 0) {
			Shape33.render(matrices, vertices, light, overlay);
			Shape32.render(matrices, vertices, light, overlay);
			Shape135.render(matrices, vertices, light, overlay);
			Shape9.render(matrices, vertices, light, overlay);
			Shape31.render(matrices, vertices, light, overlay);
			Shape134.render(matrices, vertices, light, overlay);
			Grip02.render(matrices, vertices, light, overlay);
			Shape13.render(matrices, vertices, light, overlay);
			Shape151.render(matrices, vertices, light, overlay);
			Shape34.render(matrices, vertices, light, overlay);
			Shape92.render(matrices, vertices, light, overlay);
			Shape132.render(matrices, vertices, light, overlay);
			Grip.render(matrices, vertices, light, overlay);
			Shape91.render(matrices, vertices, light, overlay);
			Shape15.render(matrices, vertices, light, overlay);
			Shape131_1.render(matrices, vertices, light, overlay);
			Grip01.render(matrices, vertices, light, overlay);
			Grip2_03.render(matrices, vertices, light, overlay);
			Grip03_1.render(matrices, vertices, light, overlay);
			Shape93.render(matrices, vertices, light, overlay);
			Shape136.render(matrices, vertices, light, overlay);
			Shape131.render(matrices, vertices, light, overlay);
			Grip03.render(matrices, vertices, light, overlay);
			Shape152.render(matrices, vertices, light, overlay);
			Grip2_01.render(matrices, vertices, light, overlay);
			Shape133.render(matrices, vertices, light, overlay);
			Grip2_02.render(matrices, vertices, light, overlay);
		} else {
			matrices.pushPose();
			this.transformPart(matrices, entity, transformType, part, ammoLeft, reloadProgress, fireProgress);
			this.partModel(part).render(entity, matrices, vertices, ammoLeft, reloadProgress, transformType, part, fireProgress, chargeProgress, light, overlay);
			matrices.popPose();
		}
	}


	protected static class HeadPart extends TGModel {

		TGModelPart Drill011;
		TGModelPart Drill06;
		TGModelPart Drill07;
		TGModelPart Drill03;
		TGModelPart Drill02;
		TGModelPart Drill08;
		TGModelPart Drill09;
		TGModelPart Drill04;
		TGModelPart Drill010;
		TGModelPart Drill01;
		TGModelPart Drill05;

		public HeadPart() {
			super(RenderType::entitySolid);
			textureWidth = 64;
			textureHeight = 64;
			this.Drill011 = new TGModelPart(this, 48, 0);
			this.Drill011.setPivot(16.5F, 1.5F, 0.5F);
			this.Drill011.addCuboid(0.0F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
			this.setRotation(Drill011, 0.7853981633974483F, 0.0F, 0.0F);
			this.Drill06 = new TGModelPart(this, 17, 14);
			this.Drill06.setPivot(11.0F, 1.5F, 0.5F);
			this.Drill06.addCuboid(0.0F, -3.0F, -3.0F, 2, 6, 6, 0.0F);
			this.Drill07 = new TGModelPart(this, 0, 56);
			this.Drill07.setPivot(12.0F, 1.5F, 0.5F);
			this.Drill07.addCuboid(0.0F, -2.0F, -2.0F, 2, 4, 4, 0.0F);
			this.setRotation(Drill07, 0.7853981633974483F, 0.0F, 0.0F);
			this.Drill03 = new TGModelPart(this, 22, 50);
			this.Drill03.setPivot(8.0F, 1.5F, 0.5F);
			this.Drill03.addCuboid(0.0F, -3.5F, -3.5F, 2, 7, 7, 0.0F);
			this.setRotation(Drill03, 0.7853981633974483F, 0.0F, 0.0F);
			this.Drill02 = new TGModelPart(this, 41, 48);
			this.Drill02.setPivot(6.0F, 1.5F, 0.5F);
			this.Drill02.addCuboid(0.0F, -4.0F, -4.0F, 2, 8, 8, 0.0F);
			this.setRotation(Drill02, 0.7853981633974483F, 0.0F, 0.0F);
			this.Drill08 = new TGModelPart(this, 0, 56);
			this.Drill08.setPivot(13.0F, 1.5F, 0.5F);
			this.Drill08.addCuboid(0.0F, -2.0F, -2.0F, 2, 4, 4, 0.0F);
			this.Drill09 = new TGModelPart(this, 56, 0);
			this.Drill09.setPivot(14.5F, 1.5F, 0.5F);
			this.Drill09.addCuboid(0.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
			this.Drill04 = new TGModelPart(this, 22, 50);
			this.Drill04.setPivot(9.0F, 1.5F, 0.5F);
			this.Drill04.addCuboid(0.0F, -3.5F, -3.5F, 2, 7, 7, 0.0F);
			this.Drill010 = new TGModelPart(this, 56, 0);
			this.Drill010.setPivot(14.0F, 1.5F, 0.5F);
			this.Drill010.addCuboid(0.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
			this.setRotation(Drill010, 0.7853981633974483F, 0.0F, 0.0F);
			this.Drill01 = new TGModelPart(this, 41, 48);
			this.Drill01.setPivot(7.0F, 1.5F, 0.5F);
			this.Drill01.addCuboid(0.0F, -4.0F, -4.0F, 2, 8, 8, 0.0F);
			this.Drill05 = new TGModelPart(this, 17, 14);
			this.Drill05.setPivot(10.0F, 1.5F, 0.5F);
			this.Drill05.addCuboid(0.0F, -3.0F, -3.0F, 2, 6, 6, 0.0F);
			this.setRotation(Drill05, 0.7853981633974483F, 0.0F, 0.0F);
		}

		@Override
		public void render(@Nullable LivingEntity entity, PoseStack matrices, VertexConsumer vertices, int ammoLeft, float reloadProgress,
				ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
			Drill011.render(matrices, vertices, light, overlay);
			Drill06.render(matrices, vertices, light, overlay);
			Drill07.render(matrices, vertices, light, overlay);
			Drill03.render(matrices, vertices, light, overlay);
			Drill02.render(matrices, vertices, light, overlay);
			Drill08.render(matrices, vertices, light, overlay);
			Drill09.render(matrices, vertices, light, overlay);
			Drill04.render(matrices, vertices, light, overlay);
			Drill010.render(matrices, vertices, light, overlay);
			Drill01.render(matrices, vertices, light, overlay);
			Drill05.render(matrices, vertices, light, overlay);
		}

	}
}
