package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;

/** Stielgranate (stick grenade), Techguns 2 only. */
public class ModelStielgranate extends TGModel {

	// fields
	TGModelPart Granate01;
	TGModelPart Stiel01;
	TGModelPart Stiel02;

	public ModelStielgranate() {
		super(RenderType::entitySolid);
		textureWidth = 32;
		textureHeight = 32;

		Granate01 = new TGModelPart(this, 0, 5);
		Granate01.addCuboid(0F, 0F, 0F, 2, 12, 2);
		Granate01.setPivot(-1F, -22F, 0.5F);
		Granate01.setTextureSize(32, 32);
		Granate01.mirror = true;
		setRotation(Granate01, 0F, 0.7853982F, 0F);
		Stiel01 = new TGModelPart(this, 13, 0);
		Stiel01.addCuboid(0F, 0F, 0F, 4, 5, 4);
		Stiel01.setPivot(-1.5F, -27F, -1.5F);
		Stiel01.setTextureSize(32, 32);
		Stiel01.mirror = true;
		setRotation(Stiel01, 0F, 0F, 0F);
		Stiel02 = new TGModelPart(this, 0, 0);
		Stiel02.addCuboid(0F, 0F, 0F, 3, 1, 3);
		Stiel02.setPivot(-1F, -10F, -1F);
		Stiel02.setTextureSize(32, 32);
		Stiel02.mirror = true;
		setRotation(Stiel02, 0F, 0F, 0F);
	}

	@Override
	public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft, float reloadProgress,
			ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
		Granate01.render(matrices, vertices, light, overlay);
		Stiel01.render(matrices, vertices, light, overlay);
		Stiel02.render(matrices, vertices, light, overlay);
	}

}
