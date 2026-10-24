package techguns.client.model.item;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;




public class ModelRocket extends TGModel
{
  //fields
    TGModelPart R9;
    TGModelPart R11;
    TGModelPart R8;
    TGModelPart R3;
    TGModelPart R10;
    TGModelPart R2;
    TGModelPart R4;
    TGModelPart R5;
    TGModelPart R6;
    TGModelPart R7;
    TGModelPart R1;
  
  public ModelRocket()
  {
	  super(RenderType::entitySolid);
      textureWidth = 64;
      textureHeight = 32;
    
      R9 = new TGModelPart(this, 0, 0);
      R9.addCuboid(0F, 0F, 0F, 6, 4, 4);
      R9.setPivot(-9.9F, -1.5F, -1.5F);
      R9.mirror = true;
      setRotation(R9, 0F, 0F, 0F);
      R11 = new TGModelPart(this, 30, 0);
      R11.addCuboid(0F, 0F, 0F, 2, 1, 6);
      R11.setPivot(-9.8F, 0F, -2.5F);
      R11.mirror = true;
      setRotation(R11, 0F, 0F, 0F);
      R8 = new TGModelPart(this, 0, 16);
      R8.addCuboid(0F, 0F, 0F, 10, 3, 3);
      R8.setPivot(-10F, -1F, -1F);
      R8.mirror = true;
      setRotation(R8, 0F, 0F, 0F);
      R3 = new TGModelPart(this, 20, 3);
      R3.addCuboid(0F, 0F, 0F, 1, 2, 2);
      R3.setPivot(10F, -0.5F, -0.5F);
      R3.mirror = true;
      setRotation(R3, 0F, 0F, 0F);
      R10 = new TGModelPart(this, 30, 7);
      R10.addCuboid(0F, 0F, 0F, 2, 6, 1);
      R10.setPivot(-9.8F, -2.5F, 0F);
      R10.mirror = true;
      setRotation(R10, 0F, 0F, 0F);
      R2 = new TGModelPart(this, 0, 16);
      R2.addCuboid(0F, 0F, 0F, 10, 3, 3);
      R2.setPivot(0F, -1F, -1F);
      R2.mirror = true;
      setRotation(R2, 0F, 0F, 0F);
      R4 = new TGModelPart(this, 12, 9);
      R4.addCuboid(0F, 0F, 0F, 4, 2, 5);
      R4.setPivot(4F, -0.5F, -2F);
      R4.mirror = true;
      setRotation(R4, 0F, 0F, 0F);
      R5 = new TGModelPart(this, 0, 8);
      R5.addCuboid(0F, 0F, 0F, 4, 5, 2);
      R5.setPivot(4F, -2F, -0.5F);
      R5.mirror = true;
      setRotation(R5, 0F, 0F, 0F);
      R6 = new TGModelPart(this, 30, 7);
      R6.addCuboid(0F, 0F, 0F, 2, 6, 1);
      R6.setPivot(4.1F, -2.5F, 0F);
      R6.mirror = true;
      setRotation(R6, 0F, 0F, 0F);
      R7 = new TGModelPart(this, 30, 0);
      R7.addCuboid(0F, 0F, 0F, 2, 1, 6);
      R7.setPivot(4.1F, 0F, -2.5F);
      R7.mirror = true;
      setRotation(R7, 0F, 0F, 0F);
      R1 = new TGModelPart(this, 0, 0);
      R1.addCuboid(0F, 0F, 0F, 6, 4, 4);
      R1.setPivot(3F, -1.5F, -1.5F);
      R1.mirror = true;
      setRotation(R1, 0F, 0F, 0F);
  }
  

	@Override
	public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft, float reloadProgress,
			ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
		 	R9.render(matrices, vertices, light, overlay);
		    R11.render(matrices, vertices, light, overlay);
		    R8.render(matrices, vertices, light, overlay);
		    R3.render(matrices, vertices, light, overlay);
		    R10.render(matrices, vertices, light, overlay);
		    R2.render(matrices, vertices, light, overlay);
		    R4.render(matrices, vertices, light, overlay);
		    R5.render(matrices, vertices, light, overlay);
		    R6.render(matrices, vertices, light, overlay);
		    R7.render(matrices, vertices, light, overlay);
		    R1.render(matrices, vertices, light, overlay);
	}

}