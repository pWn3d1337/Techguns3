package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.GeoBones;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;

public class ModelChainsaw extends TGModel {
	private final BladePart bladePart = new BladePart();

	/** Render part 1 is the nested blade model. */
	@Override
	public TGModel partModel(int part) {
		return part == 1 ? bladePart : this;
	}

	/** Blade frame (0 or 1) of the running chain. */
	private static int bladeFrame(float fireProgress) {
		return Math.round(fireProgress * 40f) % 2;
	}

	TGModelPart shape87_17;
	TGModelPart Shape26_1;
	TGModelPart Shape26_9;
	TGModelPart shape87_10;
	TGModelPart shape87_23;
	TGModelPart shape87_3;
	TGModelPart shape87_9;
	TGModelPart Shape26;
	TGModelPart shape87_2;
	TGModelPart shape87_33;
	TGModelPart shape87_25;
	TGModelPart Shape26_4;
	TGModelPart shape87_27;
	TGModelPart Shape26_7;
	TGModelPart shape87_26;
	TGModelPart shape87_20;
	TGModelPart Shape26_6;
	TGModelPart shape87;
	TGModelPart shape87_12;
	TGModelPart shape87_11;
	TGModelPart shape87_30;
	TGModelPart shape87_21;
	TGModelPart shape87_13;
	TGModelPart Shape26_3;
	TGModelPart shape87_1;
	TGModelPart shape87_4;
	TGModelPart shape87_15;
	TGModelPart shape87_29;
	TGModelPart Shape30;
	TGModelPart shape87_18;
	TGModelPart shape87_16;
	TGModelPart shape87_28;
	TGModelPart shape87_8;
	TGModelPart Shape26_5;
	TGModelPart shape87_5;
	TGModelPart shape87_6;
	TGModelPart shape87_22;
	TGModelPart Shape26_2;
	TGModelPart shape87_31;
	TGModelPart shape87_19;
	TGModelPart Shape26_8;
	TGModelPart shape87_7;
	TGModelPart shape87_14;
	TGModelPart shape87_24;
	TGModelPart shape87_34;

	public ModelChainsaw() {
		super(RenderType::entityCutout);
		textureWidth = 128;
		textureHeight = 64;
		this.shape87_17 = new TGModelPart(this, 88, 39);
		this.shape87_17.setPivot(4.5F, 4.5F, -3.0F);
		this.shape87_17.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 11, 0.0F);
		this.Shape26_1 = new TGModelPart(this, 98, 16);
		this.Shape26_1.setPivot(-9.0F, -3.0F, 0.0F);
		this.Shape26_1.addCuboid(0.0F, 0.0F, 0.0F, 1, 5, 2, 0.0F);
		this.Shape26_9 = new TGModelPart(this, 88, 1);
		this.Shape26_9.setPivot(-19.0F, 3.0F, 0.0F);
		this.Shape26_9.addCuboid(0.0F, 0.0F, 0.0F, 9, 2, 2, 0.0F);
		this.shape87_10 = new TGModelPart(this, 61, 14);
		this.shape87_10.setPivot(-7.5F, 0.5F, 5.0F);
		this.shape87_10.addCuboid(0.0F, 0.0F, 0.0F, 4, 4, 1, 0.0F);
		this.shape87_23 = new TGModelPart(this, 120, 33);
		this.shape87_23.setPivot(-6.5F, -4.0F, -5.0F);
		this.shape87_23.addCuboid(0.0F, 0.0F, 0.0F, 2, 3, 2, 0.0F);
		this.shape87_3 = new TGModelPart(this, 48, 34);
		this.shape87_3.setPivot(7.0F, -7.0F, -2.0F);
		this.shape87_3.addCuboid(0.0F, 0.0F, 0.0F, 3, 3, 6, 0.0F);
		this.shape87_9 = new TGModelPart(this, 33, 10);
		this.shape87_9.setPivot(-7.0F, -3.0F, 5.0F);
		this.shape87_9.addCuboid(0.0F, 0.0F, 0.0F, 3, 3, 1, 0.0F);
		this.Shape26 = new TGModelPart(this, 88, 19);
		this.Shape26.setPivot(-11.0F, -5.0F, 0.0F);
		this.Shape26.addCuboid(0.0F, 0.0F, 0.0F, 3, 2, 2, 0.0F);
		this.shape87_2 = new TGModelPart(this, 66, 31);
		this.shape87_2.setPivot(10.0F, -3.0F, -2.0F);
		this.shape87_2.addCuboid(0.0F, 0.0F, 0.0F, 1, 7, 7, 0.0F);
		this.shape87_33 = new TGModelPart(this, 0, 1);
		this.shape87_33.setPivot(45.0F, -2.0F, -3.5F);
		this.shape87_33.addCuboid(0.0F, 0.0F, 0.0F, 1, 5, 1, 0.0F);
		this.shape87_25 = new TGModelPart(this, 92, 41);
		this.shape87_25.setPivot(12.5F, -9.0F, -3.0F);
		this.shape87_25.addCuboid(-1.0F, 0.0F, 0.0F, 1, 6, 1, 0.0F);
		this.setRotation(shape87_25, 0.0F, 0.0F, 0.7285004297824331F);
		this.Shape26_4 = new TGModelPart(this, 88, 12);
		this.Shape26_4.setPivot(-21.0F, 2.0F, 0.0F);
		this.Shape26_4.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 2, 0.0F);
		this.shape87_27 = new TGModelPart(this, 86, 42);
		this.shape87_27.setPivot(13.5F, -14.0F, 4.0F);
		this.shape87_27.addCuboid(-2.0F, 0.0F, 0.0F, 1, 5, 1, 0.0F);
		this.Shape26_7 = new TGModelPart(this, 94, 7);
		this.Shape26_7.setPivot(-21.0F, -3.0F, 0.0F);
		this.Shape26_7.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 2, 0.0F);
		this.shape87_26 = new TGModelPart(this, 92, 41);
		this.shape87_26.setPivot(12.5F, -9.0F, 4.0F);
		this.shape87_26.addCuboid(-1.0F, 0.0F, 0.0F, 1, 6, 1, 0.0F);
		this.setRotation(shape87_26, 0.0F, 0.0F, 0.7285004297824331F);
		this.shape87_20 = new TGModelPart(this, 100, 60);
		this.shape87_20.setPivot(1.5F, -13.0F, -5.0F);
		this.shape87_20.addCuboid(-12.0F, 0.0F, 0.0F, 12, 2, 2, 0.0F);
		this.setRotation(shape87_20, 0.0F, 0.0F, -0.8477064176936457F);
		this.Shape26_6 = new TGModelPart(this, 96, 13);
		this.Shape26_6.setPivot(-22.0F, 2.0F, 0.0F);
		this.Shape26_6.addCuboid(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
		this.shape87 = new TGModelPart(this, 86, 37);
		this.shape87.setPivot(5.0F, 3.0F, 8.0F);
		this.shape87.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 2, 0.0F);
		this.shape87_12 = new TGModelPart(this, 48, 14);
		this.shape87_12.setPivot(5.0F, -8.0F, -2.0F);
		this.shape87_12.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 6, 0.0F);
		this.shape87_11 = new TGModelPart(this, 1, 24);
		this.shape87_11.setPivot(10.0F, -3.5F, -4.0F);
		this.shape87_11.addCuboid(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
		this.shape87_30 = new TGModelPart(this, 84, 42);
		this.shape87_30.setPivot(13.5F, -12.0F, -2.0F);
		this.shape87_30.addCuboid(-2.0F, 0.0F, 0.0F, 1, 1, 6, 0.0F);
		this.shape87_21 = new TGModelPart(this, 110, 51);
		this.shape87_21.setPivot(5.0F, -13.0F, 0.0F);
		this.shape87_21.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 7, 0.0F);
		this.shape87_13 = new TGModelPart(this, 37, 23);
		this.shape87_13.setPivot(-3.0F, -7.0F, -3.0F);
		this.shape87_13.addCuboid(0.0F, 0.0F, 0.0F, 10, 3, 8, 0.0F);
		this.Shape26_3 = new TGModelPart(this, 88, 5);
		this.Shape26_3.setPivot(-22.0F, -2.0F, 0.0F);
		this.Shape26_3.addCuboid(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
		this.shape87_1 = new TGModelPart(this, 86, 33);
		this.shape87_1.setPivot(5.0F, 3.5F, 7.0F);
		this.shape87_1.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
		this.shape87_4 = new TGModelPart(this, 0, 3);
		this.shape87_4.setPivot(-8.0F, -7.0F, -3.0F);
		this.shape87_4.addCuboid(0.0F, 0.0F, 0.0F, 5, 12, 8, 0.0F);
		this.shape87_15 = new TGModelPart(this, 84, 42);
		this.shape87_15.setPivot(13.5F, -10.0F, -2.0F);
		this.shape87_15.addCuboid(-2.0F, 0.0F, 0.0F, 1, 1, 6, 0.0F);
		this.shape87_29 = new TGModelPart(this, 84, 42);
		this.shape87_29.setPivot(13.5F, -14.0F, -2.0F);
		this.shape87_29.addCuboid(-2.0F, 0.0F, 0.0F, 1, 1, 6, 0.0F);
		this.Shape30 = new TGModelPart(this, 88, 23);
		this.Shape30.setPivot(-19.0F, -1.0F, 0.0F);
		this.Shape30.addCuboid(0.0F, -2.0F, 0.0F, 9, 2, 2, 0.0F);
		this.setRotation(Shape30, 0.0F, 0.0F, -0.222529479629277F);
		this.shape87_18 = new TGModelPart(this, 112, 29);
		this.shape87_18.setPivot(5.0F, -7.0F, 11.0F);
		this.shape87_18.addCuboid(0.0F, -7.0F, -2.0F, 2, 7, 2, 0.0F);
		this.setRotation(shape87_18, 0.6058037833672317F, 0.0F, 0.0F);
		this.shape87_16 = new TGModelPart(this, 112, 42);
		this.shape87_16.setPivot(7.0F, -13.0F, 0.0F);
		this.shape87_16.addCuboid(-2.0F, 0.0F, -6.0F, 2, 2, 6, 0.0F);
		this.setRotation(shape87_16, 0.0F, 0.6127851003752092F, 0.0F);
		this.shape87_28 = new TGModelPart(this, 86, 42);
		this.shape87_28.setPivot(13.5F, -14.0F, -3.0F);
		this.shape87_28.addCuboid(-2.0F, 0.0F, 0.0F, 1, 5, 1, 0.0F);
		this.shape87_8 = new TGModelPart(this, 26, 15);
		this.shape87_8.setPivot(-3.0F, -9.0F, -2.0F);
		this.shape87_8.addCuboid(0.0F, 0.0F, 0.0F, 8, 2, 6, 0.0F);
		this.Shape26_5 = new TGModelPart(this, 88, 16);
		this.Shape26_5.setPivot(-20.0F, 4.0F, 0.0F);
		this.Shape26_5.addCuboid(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
		this.shape87_5 = new TGModelPart(this, 65, 13);
		this.shape87_5.setPivot(-4.0F, -8.0F, -2.0F);
		this.shape87_5.addCuboid(0.0F, 0.0F, 0.0F, 1, 1, 6, 0.0F);
		this.shape87_6 = new TGModelPart(this, 0, 23);
		this.shape87_6.setPivot(-3.0F, -4.0F, -5.0F);
		this.shape87_6.addCuboid(0.0F, 0.0F, 0.0F, 13, 9, 11, 0.0F);
		this.shape87_22 = new TGModelPart(this, 120, 38);
		this.shape87_22.setPivot(1.5F, -13.0F, -5.0F);
		this.shape87_22.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 2, 0.0F);
		this.Shape26_2 = new TGModelPart(this, 103, 7);
		this.Shape26_2.setPivot(-22.0F, -1.0F, 0.0F);
		this.Shape26_2.addCuboid(0.0F, 0.0F, 0.0F, 2, 3, 2, 0.0F);
		this.shape87_31 = new TGModelPart(this, 4, 3);
		this.shape87_31.setPivot(46.0F, -1.0F, -3.5F);
		this.shape87_31.addCuboid(0.0F, 0.0F, 0.0F, 1, 3, 1, 0.0F);
		this.shape87_19 = new TGModelPart(this, 105, 36);
		this.shape87_19.setPivot(5.0F, -7.0F, 9.0F);
		this.shape87_19.addCuboid(0.0F, 0.0F, 0.0F, 2, 10, 2, 0.0F);
		this.Shape26_8 = new TGModelPart(this, 109, 13);
		this.Shape26_8.setPivot(-10.0F, 2.0F, 0.0F);
		this.Shape26_8.addCuboid(0.0F, 0.0F, 0.0F, 2, 3, 2, 0.0F);
		this.shape87_7 = new TGModelPart(this, 65, 25);
		this.shape87_7.setPivot(1.5F, 0.5F, 6.0F);
		this.shape87_7.addCuboid(-2.5F, -2.5F, 0.0F, 5, 5, 1, 0.0F);
		this.setRotation(shape87_7, 0.0F, 0.0F, 0.7853981633974483F);
		this.shape87_14 = new TGModelPart(this, 86, 35);
		this.shape87_14.setPivot(5.0F, 3.0F, 10.0F);
		this.shape87_14.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
		this.shape87_24 = new TGModelPart(this, 121, 29);
		this.shape87_24.setPivot(-7.0F, -2.5F, -4.0F);
		this.shape87_24.addCuboid(0.0F, 0.0F, 0.0F, 3, 3, 1, 0.0F);
		this.shape87_34 = new TGModelPart(this, 18, 0);
		this.shape87_34.setPivot(12.0F, -2.5F, -3.5F);
		this.shape87_34.addCuboid(0.0F, 0.0F, 0.0F, 33, 6, 1, 0.0F);
	}

	@Override
	public void render(@Nullable LivingEntity entity, PoseStack matrices, VertexConsumer vertices, int ammoLeft, float reloadProgress,
			ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
		if (part == 0) {
			shape87_17.render(matrices, vertices, light, overlay);
			Shape26_1.render(matrices, vertices, light, overlay);
			Shape26_9.render(matrices, vertices, light, overlay);
			shape87_10.render(matrices, vertices, light, overlay);
			shape87_23.render(matrices, vertices, light, overlay);
			shape87_3.render(matrices, vertices, light, overlay);
			shape87_9.render(matrices, vertices, light, overlay);
			Shape26.render(matrices, vertices, light, overlay);
			shape87_2.render(matrices, vertices, light, overlay);
			shape87_33.render(matrices, vertices, light, overlay);
			shape87_25.render(matrices, vertices, light, overlay);
			Shape26_4.render(matrices, vertices, light, overlay);
			shape87_27.render(matrices, vertices, light, overlay);
			Shape26_7.render(matrices, vertices, light, overlay);
			shape87_26.render(matrices, vertices, light, overlay);
			shape87_20.render(matrices, vertices, light, overlay);
			Shape26_6.render(matrices, vertices, light, overlay);
			shape87.render(matrices, vertices, light, overlay);
			shape87_12.render(matrices, vertices, light, overlay);
			shape87_11.render(matrices, vertices, light, overlay);
			shape87_30.render(matrices, vertices, light, overlay);
			shape87_21.render(matrices, vertices, light, overlay);
			shape87_13.render(matrices, vertices, light, overlay);
			Shape26_3.render(matrices, vertices, light, overlay);
			shape87_1.render(matrices, vertices, light, overlay);
			shape87_4.render(matrices, vertices, light, overlay);
			shape87_15.render(matrices, vertices, light, overlay);
			shape87_29.render(matrices, vertices, light, overlay);
			Shape30.render(matrices, vertices, light, overlay);
			shape87_18.render(matrices, vertices, light, overlay);
			shape87_16.render(matrices, vertices, light, overlay);
			shape87_28.render(matrices, vertices, light, overlay);
			shape87_8.render(matrices, vertices, light, overlay);
			Shape26_5.render(matrices, vertices, light, overlay);
			shape87_5.render(matrices, vertices, light, overlay);
			shape87_6.render(matrices, vertices, light, overlay);
			shape87_22.render(matrices, vertices, light, overlay);
			Shape26_2.render(matrices, vertices, light, overlay);
			shape87_31.render(matrices, vertices, light, overlay);
			shape87_19.render(matrices, vertices, light, overlay);
			Shape26_8.render(matrices, vertices, light, overlay);
			shape87_7.render(matrices, vertices, light, overlay);
			shape87_14.render(matrices, vertices, light, overlay);
			shape87_24.render(matrices, vertices, light, overlay);
			shape87_34.render(matrices, vertices, light, overlay);
		} else {
			matrices.pushPose();
			this.transformPart(matrices, entity, transformType, part, ammoLeft, reloadProgress, fireProgress);
			this.partModel(part).render(entity, matrices, vertices, ammoLeft, reloadProgress, transformType, part, fireProgress, chargeProgress, light, overlay);
			matrices.popPose();
		}
	}


	protected static class BladePart extends TGModel {

		TGModelPart blade1;
		TGModelPart blade2;

		public BladePart() {
			super(RenderType::entityCutout);
			textureWidth = 128;
			textureHeight = 64;
			this.blade1 = new TGModelPart(this, 0, 56);
			this.blade1.setPivot(12.0F, -3.5F, -3.0F);
			this.blade1.addCuboid(0.0F, 0.0F, 0.0F, 36, 8, 0, 0.0F);
			this.blade2 = new TGModelPart(this, 0, 48);
			this.blade2.setPivot(12.0F, -3.5F, -3.0F);
			this.blade2.addCuboid(0.0F, 0.0F, 0.0F, 36, 8, 0, 0.0F);
		}

		@Override
		public void render(@Nullable LivingEntity entity, PoseStack matrices, VertexConsumer vertices, int ammoLeft, float reloadProgress,
				ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
			// the chain alternates between two blade textures while the saw runs
			if (bladeFrame(fireProgress) == 0) {
				blade1.render(matrices, vertices, light, overlay);
			} else {
				blade2.render(matrices, vertices, light, overlay);
			}
		}

	@Override
	public void animateGeoBones(GeoBones bones, int part, int ammoLeft, float reloadProgress, float fireProgress, ItemDisplayContext context) {
		super.animateGeoBones(bones, part, ammoLeft, reloadProgress, fireProgress, context);
		int frame = bladeFrame(fireProgress);
		bones.setHidden("blade1", frame != 0);
		bones.setHidden("blade2", frame == 0);
	}
	}
}
