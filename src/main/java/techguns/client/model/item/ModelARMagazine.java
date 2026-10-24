package techguns.client.model.item;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;




public class ModelARMagazine extends TGModel {

	// fields
	TGModelPart Magazine02;
	TGModelPart Magazine01;
	TGModelPart Magazine03;
	TGModelPart Magazine04;

	protected boolean empty=false;
	
	public ModelARMagazine(boolean empty) {
		super(RenderType::entitySolid);
		
		textureWidth = 32;
		textureHeight = 32;

		this.empty=empty;
		
		Magazine02 = new TGModelPart(this, 0, 0);
		Magazine02.addCuboid(0F, 0F, 0F, 2, 4, 6);
		Magazine02.setPivot(-1.5F, 9F, -4.4F);
		Magazine02.mirror = true;
		setRotation(Magazine02, -0.1570796F, 0F, 0F);
		Magazine01 = new TGModelPart(this, 0, 11);
		Magazine01.addCuboid(0F, 0F, 0F, 2, 5, 6);
		Magazine01.setPivot(-1.5F, 5F, -4.5F);
		Magazine01.mirror = true;
		setRotation(Magazine01, 0F, 0F, 0F);
		Magazine03 = new TGModelPart(this, 0, 11);
		Magazine03.addCuboid(0F, 0F, 0F, 2, 2, 6);
		Magazine03.setPivot(-1.5F, 3F, -4.5F);
		Magazine03.mirror = true;
		setRotation(Magazine03, 0F, 0F, 0F);
		
		if (!empty){
			Magazine04 = new TGModelPart(this, 0, 22);
			Magazine04.addCuboid(0F, 0F, 0F, 1, 1, 5);
			Magazine04.setPivot(-1.25F, 3F, -4F);
			Magazine04.mirror = true;
			setRotation(Magazine04, 0F, 0F, -0.7853982F);
		}
	}
	
	@Override
	public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
			float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
			int overlay) {
		Magazine02.render(matrices, vertices, light, overlay);
		Magazine01.render(matrices, vertices, light, overlay);
		Magazine03.render(matrices, vertices, light, overlay);
		if (!empty){
			Magazine04.render(matrices, vertices, light, overlay);
		}
	}

}
