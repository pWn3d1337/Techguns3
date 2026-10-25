package techguns.client.model.item;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;


public class ModelLmgMag extends TGModel {
	// fields
	TGModelPart Mag03;
	TGModelPart Mag01;
	TGModelPart Mag02;
	TGModelPart Bullet06;
	TGModelPart Bullet05;
	TGModelPart Bullet04;
	TGModelPart Bullet03;
	TGModelPart Bullet02;
	TGModelPart Bullet01;

	protected boolean empty;

	public ModelLmgMag(boolean empty) {
		super(RenderType::entitySolid);
		textureWidth = 64;
		textureHeight = 32;
		this.empty = empty;

		Mag03 = new TGModelPart(this, 0, 0);
		Mag03.addCuboid(0F, 0F, 0F, 3, 6, 7);
		Mag03.setPivot(-3.5F, 1F, -6F);
		Mag03.mirror = true;
		setRotation(Mag03, 0F, 0F, 0.7888888F);
		Mag01 = new TGModelPart(this, 0, 14);
		Mag01.addCuboid(0F, 0F, 0F, 13, 6, 7);
		Mag01.setPivot(-7.5F, 5F, -6F);
		Mag01.mirror = true;
		setRotation(Mag01, 0F, 0F, 0F);
		Mag02 = new TGModelPart(this, 22, 2);
		Mag02.addCuboid(0F, 0F, 0F, 9, 4, 7);
		Mag02.setPivot(-3.5F, 1F, -6F);
		Mag02.mirror = false;
		setRotation(Mag02, 0F, 0F, 0F);
		if (!empty) {
			Bullet06 = new TGModelPart(this, 41, 20);
			Bullet06.addCuboid(0F, 0F, 0F, 1, 1, 6);
			Bullet06.setPivot(4.5F, 0.5F, -5.5F);
			Bullet06.mirror = true;
			setRotation(Bullet06, 0F, 0F, 0.7853982F);
			Bullet05 = new TGModelPart(this, 41, 20);
			Bullet05.addCuboid(0F, 0F, 0F, 1, 1, 6);
			Bullet05.setPivot(4F, -0.5F, -5.5F);
			Bullet05.mirror = true;
			setRotation(Bullet05, 0F, 0F, 0.7853982F);
			Bullet04 = new TGModelPart(this, 41, 20);
			Bullet04.addCuboid(0F, 0F, 0F, 1, 1, 6);
			Bullet04.setPivot(3F, -0.5F, -5.5F);
			Bullet04.mirror = true;
			setRotation(Bullet04, 0F, 0F, 0.7853982F);
			Bullet03 = new TGModelPart(this, 41, 20);
			Bullet03.addCuboid(0F, 0F, 0F, 1, 1, 6);
			Bullet03.setPivot(2F, -0.5F, -5.5F);
			Bullet03.mirror = true;
			setRotation(Bullet03, 0F, 0F, 0.7853982F);
			Bullet02 = new TGModelPart(this, 41, 20);
			Bullet02.addCuboid(0F, 0F, 0F, 1, 1, 6);
			Bullet02.setPivot(1F, -0.5F, -5.5F);
			Bullet02.mirror = true;
			setRotation(Bullet02, 0F, 0F, 0.7853982F);
			Bullet01 = new TGModelPart(this, 41, 20);
			Bullet01.addCuboid(0F, 0F, 0F, 1, 1, 6);
			Bullet01.setPivot(0F, -0.5F, -5.5F);
			Bullet01.mirror = true;
			setRotation(Bullet01, 0F, 0F, 0.7853982F);
		}
	}

	@Override
	public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
			float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
			int overlay) {

		Mag03.render(matrices, vertices, light, overlay);
		Mag01.render(matrices, vertices, light, overlay);
		Mag02.render(matrices, vertices, light, overlay);

		if (!empty) {
			Bullet06.render(matrices, vertices, light, overlay);
			Bullet05.render(matrices, vertices, light, overlay);
			Bullet04.render(matrices, vertices, light, overlay);
			Bullet03.render(matrices, vertices, light, overlay);
			Bullet02.render(matrices, vertices, light, overlay);
			Bullet01.render(matrices, vertices, light, overlay);
		}
	}

}