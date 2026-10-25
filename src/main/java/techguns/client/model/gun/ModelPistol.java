package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;


public class ModelPistol extends TGModel {
	private final SlidePart secondary = new SlidePart();

	/** Render part 1 is the nested secondary model. */
	@Override
	public TGModel partModel(int part) {
		return part == 1 ? secondary : this;
	}


	//fields
    TGModelPart Top;
    TGModelPart Grip3;
    TGModelPart Barrel;
    TGModelPart Grip2;
    TGModelPart Trigger;
    TGModelPart Grip1;
  
  public ModelPistol()
  {
        super(RenderType::entitySolid);
    textureWidth = 64;
    textureHeight = 32;
    
      Top = new TGModelPart(this, 0, 17);
      Top.addCuboid(0F, 0F, 0F, 3, 1, 14);
      Top.setPivot(-2F, 2F, -2F);
      Top.setTextureSize(64, 32);
      Top.mirror = true;
      setRotation(Top, 0F, 0F, 0F);
      Grip3 = new TGModelPart(this, 3, 0);
      Grip3.addCuboid(0F, 0F, 0F, 2, 7, 3);
      Grip3.setPivot(-1.5F, 3F, 7F);
      Grip3.setTextureSize(64, 32);
      Grip3.mirror = true;
      setRotation(Grip3, 0.2181662F, 0F, 0F);
      Barrel = new TGModelPart(this, 0, 21);
      Barrel.addCuboid(0F, 0F, 0F, 1, 1, 6);
      Barrel.setPivot(-0.5F, 0.25F, -3F);
      Barrel.setTextureSize(64, 32);
      Barrel.mirror = true;
      setRotation(Barrel, 0F, 0F, 0.7853982F);
      Grip2 = new TGModelPart(this, 20, 5);
      Grip2.addCuboid(0F, 0F, 0F, 1, 1, 3);
      Grip2.setPivot(-1F, 5F, 5F);
      Grip2.setTextureSize(64, 32);
      Grip2.mirror = true;
      setRotation(Grip2, 0F, 0F, 0F);
      Trigger = new TGModelPart(this, 25, 0);
      Trigger.addCuboid(0F, 0F, 0F, 1, 2, 1);
      Trigger.setPivot(-1F, 2.5F, 6.5F);
      Trigger.setTextureSize(64, 32);
      Trigger.mirror = true;
      setRotation(Trigger, -0.4089647F, 0F, 0F);
      Grip1 = new TGModelPart(this, 20, 0);
      Grip1.addCuboid(0F, 0F, 0F, 1, 3, 1);
      Grip1.setPivot(-1F, 3F, 4F);
      Grip1.setTextureSize(64, 32);
      Grip1.mirror = true;
      setRotation(Grip1, 0F, 0F, 0F);
  }
  
  @Override
  public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
                     float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
                     int overlay) {
	    if(part==0){
		    Top.render(matrices, vertices, light, overlay);
		    Grip3.render(matrices, vertices, light, overlay);
		    Barrel.render(matrices, vertices, light, overlay);
		    Grip2.render(matrices, vertices, light, overlay);
		    Trigger.render(matrices, vertices, light, overlay);
		    Grip1.render(matrices, vertices, light, overlay);
	    } else {
			matrices.pushPose();
			this.transformPart(matrices, part, ammoLeft, reloadProgress, fireProgress);
			this.secondary.render(entityIn, matrices, vertices, ammoLeft, reloadProgress, transformType, part, fireProgress, chargeProgress, light, overlay);
			matrices.popPose();
	    }
  }


	/** Secondary render part (part 1), its own model so that it exports as its own geo file. */

	@Override
	public void transformPart(PoseStack matrices, int part, int ammoLeft, float reloadProgress, float fireProgress) {
		// the slide moves back during the first quarter of the recoil
		float progress = fireProgress * 2.0f;
		if (part == 1 && progress < 0.5f) {
			matrices.translate(0, 0, 0.15f * progress * 2.0f * scale);
		}
	}

	protected static class SlidePart extends TGModel {

		TGModelPart IS2;
		TGModelPart IS1;
		TGModelPart Slide;

		public SlidePart() {
			super(RenderType::entitySolid);
			textureWidth = 64;
			textureHeight = 32;
      IS2 = new TGModelPart(this, 0, 21);
      IS2.addCuboid(0F, 0F, 0F, 1, 1, 1);
      IS2.setPivot(-1F, -0.5333334F, -0.5F);
      IS2.setTextureSize(64, 32);
      IS2.mirror = true;
      setRotation(IS2, 0F, 0F, 0F);
      IS1 = new TGModelPart(this, 0, 24);
      IS1.addCuboid(0F, 0F, 0F, 1, 1, 1);
      IS1.setPivot(-1F, -0.5F, 9.5F);
      IS1.setTextureSize(64, 32);
      IS1.mirror = true;
      setRotation(IS1, 0F, 0F, 0F);
      Slide = new TGModelPart(this, 0, 0);
      Slide.addCuboid(0F, 0F, 0F, 3, 2, 14);
      Slide.setPivot(-2F, 0F, -2F);
      Slide.setTextureSize(64, 32);
      Slide.mirror = true;
      setRotation(Slide, 0F, 0F, 0F);
		}

		@Override
	  public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
	                     float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
	                     int overlay) {
		    	matrices.pushPose();
			    IS1.render(matrices, vertices, light, overlay);
			    IS2.render(matrices, vertices, light, overlay);
			    Slide.render(matrices, vertices, light, overlay);
			    matrices.popPose();
		}
	}
}
