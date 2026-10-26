package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;

public class ModelCombatShotgun extends TGModel {
	private final PumpPart secondary = new PumpPart();

	/** Render part 1 is the nested secondary model. */
	@Override
	public TGModel partModel(int part) {
		return part == 1 ? secondary : this;
	}

	// fields
	TGModelPart Shape1;
	TGModelPart Shape2;
	TGModelPart Shape3;
	TGModelPart Shape5;
	TGModelPart Shape6;
	TGModelPart Shape7;
	TGModelPart Shape8;
	TGModelPart Shape9;
	TGModelPart Shape10;
	TGModelPart Shape11;
	TGModelPart Shape12;
	TGModelPart Shape13;
	TGModelPart Shape14;
	TGModelPart Shape15;
	TGModelPart Shape16;
	TGModelPart Shape17;
	TGModelPart Shape18;
	TGModelPart Shape19;
	TGModelPart Shape20;

	public ModelCombatShotgun() {
        super(RenderType::entitySolid);
		textureWidth = 64;
		textureHeight = 32;

		Shape1 = new TGModelPart(this, 50, 4);
		Shape1.addCuboid(0F, 0F, 0F, 2, 1, 1);
		Shape1.setPivot(-7F, 2.5F, 0F);
		Shape1.setTextureSize(64, 32);
		Shape1.mirror = true;
		setRotation(Shape1, 0F, 0F, 0F);
		Shape2 = new TGModelPart(this, 54, 14);
		Shape2.addCuboid(0F, 0F, 0F, 3, 7, 2);
		Shape2.setPivot(-9F, 1.5F, -0.5F);
		Shape2.setTextureSize(64, 32);
		Shape2.mirror = true;
		setRotation(Shape2, 0F, 0F, 0.3346075F);
		Shape2.mirror = false;
		Shape3 = new TGModelPart(this, 0, 4);
		Shape3.addCuboid(0F, 0F, 0F, 24, 1, 1);
		Shape3.setPivot(8F, -1.5F, 1F);
		Shape3.setTextureSize(64, 32);
		Shape3.mirror = true;
		setRotation(Shape3, 0F, 0F, 0F);
		Shape5 = new TGModelPart(this, 0, 4);
		Shape5.addCuboid(0F, 0F, 0F, 24, 1, 1);
		Shape5.setPivot(8F, -1.5F, -1F);
		Shape5.setTextureSize(64, 32);
		Shape5.mirror = true;
		setRotation(Shape5, 0F, 0F, 0F);
		Shape6 = new TGModelPart(this, 0, 6);
		Shape6.addCuboid(0F, 0F, 0F, 22, 2, 2);
		Shape6.setPivot(8F, 1F, -0.5F);
		Shape6.setTextureSize(64, 32);
		Shape6.mirror = true;
		setRotation(Shape6, 0F, 0F, 0F);
		Shape6.mirror = false;
		Shape7 = new TGModelPart(this, 32, 14);
		Shape7.addCuboid(0F, 0F, 0F, 8, 4, 3);
		Shape7.setPivot(-10F, -1.5F, -1F);
		Shape7.setTextureSize(64, 32);
		Shape7.mirror = true;
		setRotation(Shape7, 0F, 0F, 0F);
		Shape8 = new TGModelPart(this, 32, 21);
		Shape8.addCuboid(0F, 0F, 0F, 10, 1, 2);
		Shape8.setPivot(-2F, -3F, -0.5F);
		Shape8.setTextureSize(64, 32);
		Shape8.mirror = true;
		setRotation(Shape8, 0F, 0F, 0F);
		Shape8.mirror = false;
		Shape9 = new TGModelPart(this, 0, 0);
		Shape9.addCuboid(0F, 0F, 0F, 24, 3, 1);
		Shape9.setPivot(8F, -2.5F, 0F);
		Shape9.setTextureSize(64, 32);
		Shape9.mirror = true;
		setRotation(Shape9, 0F, 0F, 0F);
		Shape10 = new TGModelPart(this, 32, 24);
		Shape10.addCuboid(0F, 0F, 0F, 10, 5, 3);
		Shape10.setPivot(-2F, -2F, -1F);
		Shape10.setTextureSize(64, 32);
		Shape10.mirror = true;
		setRotation(Shape10, 0F, 0F, 0F);
		Shape11 = new TGModelPart(this, 50, 0);
		Shape11.addCuboid(0F, 0F, 0F, 2, 1, 1);
		Shape11.setPivot(27F, 0.5F, 0F);
		Shape11.setTextureSize(64, 32);
		Shape11.mirror = true;
		setRotation(Shape11, 0F, 0F, 0F);
		Shape12 = new TGModelPart(this, 46, 9);
		Shape12.addCuboid(0F, 0F, 0F, 7, 1, 2);
		Shape12.setPivot(-9F, -2.5F, -0.5F);
		Shape12.setTextureSize(64, 32);
		Shape12.mirror = true;
		setRotation(Shape12, 0F, 0F, 0F);
		Shape13 = new TGModelPart(this, 50, 2);
		Shape13.addCuboid(0F, 0F, 0F, 2, 1, 1);
		Shape13.setPivot(-7.5F, -3.5F, 0F);
		Shape13.setTextureSize(64, 32);
		Shape13.mirror = true;
		setRotation(Shape13, 0F, 0F, 0F);
		Shape14 = new TGModelPart(this, 50, 0);
		Shape14.addCuboid(0F, 0F, 0F, 2, 1, 1);
		Shape14.setPivot(29F, -3.5F, 0F);
		Shape14.setTextureSize(64, 32);
		Shape14.mirror = true;
		setRotation(Shape14, 0F, 0F, 0F);
		Shape15 = new TGModelPart(this, 18, 10);
		Shape15.addCuboid(0F, 0F, 0F, 2, 5, 3);
		Shape15.setPivot(-21F, -0.5F, -1F);
		Shape15.setTextureSize(64, 32);
		Shape15.mirror = true;
		setRotation(Shape15, 0F, 0F, 0F);
		Shape16 = new TGModelPart(this, 0, 19);
		Shape16.addCuboid(0F, 0F, 0F, 12, 2, 2);
		Shape16.setPivot(-21F, 2.5F, -0.5F);
		Shape16.setTextureSize(64, 32);
		Shape16.mirror = true;
		setRotation(Shape16, 0F, 0F, -0.2617994F);
		Shape17 = new TGModelPart(this, 10, 10);
		Shape17.addCuboid(0F, 0F, 0F, 2, 3, 2);
		Shape17.setPivot(-23F, 5F, -0.5F);
		Shape17.setTextureSize(64, 32);
		Shape17.mirror = true;
		setRotation(Shape17, 0F, 0F, 0F);
		Shape17.mirror = false;
		Shape18 = new TGModelPart(this, 0, 10);
		Shape18.addCuboid(0F, 0F, 0F, 2, 5, 3);
		Shape18.setPivot(-23F, 0F, -1F);
		Shape18.setTextureSize(64, 32);
		Shape18.mirror = true;
		setRotation(Shape18, 0F, 0F, 0F);
		Shape18.mirror = false;
		Shape19 = new TGModelPart(this, 10, 15);
		Shape19.addCuboid(0F, 0F, 0F, 1, 2, 2);
		Shape19.setPivot(-21F, 4.5F, -0.5F);
		Shape19.setTextureSize(64, 32);
		Shape19.mirror = true;
		setRotation(Shape19, 0F, 0F, 0F);
		Shape20 = new TGModelPart(this, 29, 12);
		Shape20.addCuboid(0F, 0F, 0F, 10, 1, 1);
		Shape20.setPivot(-19.5F, 0F, 0F);
		Shape20.setTextureSize(64, 32);
		Shape20.mirror = true;
		setRotation(Shape20, 0F, 0F, -0.1396263F);
	}

	
	
	@Override
	public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
			float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
	int overlay) {

		if (part==0){
			Shape1.render(matrices, vertices, light, overlay);
			Shape2.render(matrices, vertices, light, overlay);
			Shape3.render(matrices, vertices, light, overlay);
			Shape5.render(matrices, vertices, light, overlay);
			Shape6.render(matrices, vertices, light, overlay);
			Shape7.render(matrices, vertices, light, overlay);
			Shape8.render(matrices, vertices, light, overlay);
			Shape9.render(matrices, vertices, light, overlay);
			Shape10.render(matrices, vertices, light, overlay);
			Shape11.render(matrices, vertices, light, overlay);
			Shape12.render(matrices, vertices, light, overlay);
			Shape13.render(matrices, vertices, light, overlay);
			Shape14.render(matrices, vertices, light, overlay);
			Shape15.render(matrices, vertices, light, overlay);
			Shape16.render(matrices, vertices, light, overlay);
			Shape17.render(matrices, vertices, light, overlay);
			Shape18.render(matrices, vertices, light, overlay);
			Shape19.render(matrices, vertices, light, overlay);
			Shape20.render(matrices, vertices, light, overlay);
		} else {
			matrices.pushPose();
			this.transformPart(matrices, part, ammoLeft, reloadProgress, fireProgress);
			this.secondary.render(entityIn, matrices, vertices, ammoLeft, reloadProgress, transformType, part, fireProgress, chargeProgress, light, overlay);
			matrices.popPose();
		}
	}

	/** the pump moves back and forth after a shot / at the end of the reload */
	@Override
	public void transformPart(PoseStack matrices, int part, int ammoLeft, float reloadProgress, float fireProgress) {
		if (part == 1) {
			float pumpprogress=0f;
			if(fireProgress>=0.65 && fireProgress<=0.95f) {
				pumpprogress = (fireProgress-0.65f)*3.333333f;
			} else if (reloadProgress>=0.9f) {
				pumpprogress = (reloadProgress-0.9f)*10f;
			}
			if(pumpprogress>0.5f) {
				pumpprogress=1f-pumpprogress;
			}
			matrices.translate(-0.8f * pumpprogress, 0, 0);
		}
	}

	protected static class PumpPart extends TGModel {

		TGModelPart Pump;

		public PumpPart() {
			super(RenderType::entitySolid);
			textureWidth = 64;
			textureHeight = 32;
		Pump = new TGModelPart(this, 0, 23);
		Pump.addCuboid(0F, 0F, 0F, 12, 5, 4);
		Pump.setPivot(7F, -1F, -1.5F);
		Pump.setTextureSize(64, 32);
		Pump.mirror = true;
		setRotation(Pump, 0F, 0F, 0F);
		}

		@Override
		public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
				float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
				int overlay) {
			Pump.render(matrices, vertices, light, overlay);
		}
	}
}
