package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;

public class ModelHandgun extends TGModel
{
  //fields
    TGModelPart Shape1;
    TGModelPart Shape2;
    TGModelPart Shape3;
    TGModelPart Shape4;
    TGModelPart Shape5;
    TGModelPart Shape6;
    TGModelPart Shape7;
    TGModelPart Shape8;
    TGModelPart Shape9;
    TGModelPart Shape10;
    TGModelPart Shape11;
    TGModelPart Shape12;
    //TGModelPart Shape13;
    TGModelPart Shape14;
    TGModelPart Shape15;
  
  public ModelHandgun()
  {
        super(RenderType::entitySolid);
    textureWidth = 64;
    textureHeight = 32;
    
      Shape1 = new TGModelPart(this, 4, 0);
      Shape1.addCuboid(0F, 0F, 0F, 22, 3, 1);
      Shape1.setPivot(0F, -1F, 2F);
      Shape1.setTextureSize(64, 32);
      Shape1.mirror = true;
      setRotation(Shape1, 0F, 0F, 0F);
      Shape2 = new TGModelPart(this, 4, 0);
      Shape2.addCuboid(0F, 0F, 0F, 22, 3, 1);
      Shape2.setPivot(0F, -1F, -2F);
      Shape2.setTextureSize(64, 32);
      Shape2.mirror = true;
      setRotation(Shape2, 0F, 0F, 0F);
      Shape3 = new TGModelPart(this, 0, 1);
      Shape3.addCuboid(0F, 0F, 0F, 22, 5, 3);
      Shape3.setPivot(0F, -2F, -1F);
      Shape3.setTextureSize(64, 32);
      Shape3.mirror = true;
      setRotation(Shape3, 0F, 0F, 0F);
      Shape4 = new TGModelPart(this, 0, 0);
      Shape4.addCuboid(0F, 0F, 0F, 1, 1, 1);
      Shape4.setPivot(21F, -3F, 0F);
      Shape4.setTextureSize(64, 32);
      Shape4.mirror = true;
      setRotation(Shape4, 0F, 0F, 0F);
      Shape5 = new TGModelPart(this, 32, 9);
      Shape5.addCuboid(0F, 0F, 0F, 2, 1, 3);
      Shape5.setPivot(-8F, -3F, -1F);
      Shape5.setTextureSize(64, 32);
      Shape5.mirror = true;
      setRotation(Shape5, 0F, 0F, 0F);
      Shape6 = new TGModelPart(this, 0, 9);
      Shape6.addCuboid(0F, 0F, 0F, 2, 5, 5);
      Shape6.setPivot(-8F, -2F, -2F);
      Shape6.setTextureSize(64, 32);
      Shape6.mirror = true;
      setRotation(Shape6, 0F, 0F, 0F);
      Shape7 = new TGModelPart(this, 14, 9);
      Shape7.addCuboid(0F, 0F, 0F, 4, 4, 5);
      Shape7.setPivot(-6F, -1F, -2F);
      Shape7.setTextureSize(64, 32);
      Shape7.mirror = true;
      setRotation(Shape7, 0F, 0F, 0F);
      Shape8 = new TGModelPart(this, 32, 9);
      Shape8.addCuboid(0F, 0F, 0F, 2, 1, 3);
      Shape8.setPivot(-2F, -3F, -1F);
      Shape8.setTextureSize(64, 32);
      Shape8.mirror = true;
      setRotation(Shape8, 0F, 0F, 0F);
      Shape9 = new TGModelPart(this, 0, 9);
      Shape9.addCuboid(0F, 0F, 0F, 2, 5, 5);
      Shape9.setPivot(-2F, -2F, -2F);
      Shape9.setTextureSize(64, 32);
      Shape9.mirror = true;
      setRotation(Shape9, 0F, 0F, 0F);
      Shape10 = new TGModelPart(this, 44, 14);
      Shape10.addCuboid(0F, 0F, 0F, 7, 4, 3);
      Shape10.setPivot(-15F, -1F, -1F);
      Shape10.setTextureSize(64, 32);
      Shape10.mirror = true;
      setRotation(Shape10, 0F, 0F, 0F);
      Shape11 = new TGModelPart(this, 48, 11);
      Shape11.addCuboid(0F, 0F, 0F, 7, 2, 1);
      Shape11.setPivot(-15F, 0F, -2F);
      Shape11.setTextureSize(64, 32);
      Shape11.mirror = true;
      setRotation(Shape11, 0F, 0F, 0F);
      Shape12 = new TGModelPart(this, 51, 3);
      Shape12.addCuboid(0F, 0F, 0F, 2, 1, 3);
      Shape12.setPivot(-15F, 3F, -1F);
      Shape12.setTextureSize(64, 32);
      Shape12.mirror = true;
      setRotation(Shape12, 0F, 0F, 0F);
//      Shape13 = new TGModelPart(this, 0, 0);
//      Shape13.addCuboid(0F, 0F, 0F, 22, 5, 3);
//      Shape13.setPivot(0F, -2F, -1F);
//      Shape13.setTextureSize(64, 32);
//      Shape13.mirror = true;
//      setRotation(Shape13, 0F, 0F, 0F);
      Shape14 = new TGModelPart(this, 48, 11);
      Shape14.addCuboid(0F, 0F, 0F, 7, 2, 1);
      Shape14.setPivot(-15F, 0F, 2F);
      Shape14.setTextureSize(64, 32);
      Shape14.mirror = true;
      setRotation(Shape14, 0F, 0F, 0F);
      Shape15 = new TGModelPart(this, 50, 0);
      Shape15.addCuboid(0F, 0F, 0F, 2, 6, 5);
      Shape15.setPivot(-17F, -1F, -2F);
      Shape15.setTextureSize(64, 32);
      Shape15.mirror = true;
      setRotation(Shape15, 0F, 0F, 0F);
  }
  
  @Override
  public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
					   float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
	int overlay) {
    
    Shape1.render(matrices, vertices, light, overlay);
    Shape2.render(matrices, vertices, light, overlay);
    Shape3.render(matrices, vertices, light, overlay);
    Shape4.render(matrices, vertices, light, overlay);
    Shape5.render(matrices, vertices, light, overlay);
    Shape6.render(matrices, vertices, light, overlay);
    Shape7.render(matrices, vertices, light, overlay);
    Shape8.render(matrices, vertices, light, overlay);
    Shape9.render(matrices, vertices, light, overlay);
    Shape10.render(matrices, vertices, light, overlay);
    Shape11.render(matrices, vertices, light, overlay);
    Shape12.render(matrices, vertices, light, overlay);
//    Shape13.render(f5);
    Shape14.render(matrices, vertices, light, overlay);
    Shape15.render(matrices, vertices, light, overlay);
  }

}