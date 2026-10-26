package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;

/** Frag grenade (item with the pin ring, projectile without). The ring is hidden while the throw is wound up. */
public class ModelFragGrenade extends TGModel {

    public TGModelPart Top;
    public TGModelPart MainRot1;
    public TGModelPart MainRot2;
    public TGModelPart Main;
    public TGModelPart TopRotated;
    public TGModelPart Bottom;
    public TGModelPart BottomRotated;
    public TGModelPart TopPart;
    public TGModelPart Box01;
    public TGModelPart Box02;
    public TGModelPart Ring;

    protected boolean showRing;
    
    public ModelFragGrenade(boolean showRing) {
    	// cutout: the pin ring texture has a transparent hole (Techguns 2 drew items with an alpha test)
    	super(RenderType::entityCutout);
    	
        this.textureWidth = 32;
        this.textureHeight = 32;
        this.Main = new TGModelPart(this, 13, 0);
        this.Main.setPivot(-1.5F, -16.0F, -1.5F);
        this.Main.addCuboid(0.0F, 0.0F, 0.0F, 4, 4, 4, 0.0F);
        this.MainRot2 = new TGModelPart(this, 13, 16);
        this.MainRot2.setPivot(0.5F, -14.0F, 0.5F);
        this.MainRot2.addCuboid(-2.0F, 0.0F, -2.0F, 4, 2, 4, -0.1F, -0.1F, -0.1F);
        this.setRotation(MainRot2, 0.0F, 0.7853981633974483F, 0.0F);
        this.BottomRotated = new TGModelPart(this, 15, 11);
        this.BottomRotated.setPivot(0.5F, -12.0F, 0.5F);
        this.BottomRotated.addCuboid(-1.5F, 0.0F, -1.5F, 3, 1, 3, -0.1F, -0.1F, -0.1F);
        this.setRotation(BottomRotated, 0.0F, 0.7853981633974483F, 0.0F);
        this.TopPart = new TGModelPart(this, 15, 23);
        this.TopPart.setPivot(-0.5F, -18.7F, -0.5F);
        this.TopPart.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 2, -0.2F, -0.2F, -0.2F);
        this.Box02 = new TGModelPart(this, 1, 28);
        this.Box02.setPivot(4.1F, -17.3F, -0.5F);
        this.Box02.addCuboid(0.0F, 0.0F, 0.0F, 5, 1, 2, -0.3F, -0.3F, -0.3F);
        this.setRotation(Box02, 0.0F, 0.0F, 1.5707963267948966F);
        this.TopRotated = new TGModelPart(this, 15, 11);
        this.TopRotated.setPivot(0.5F, -17.0F, 0.5F);
        this.TopRotated.addCuboid(-1.5F, 0.0F, -1.5F, 3, 1, 3, -0.1F, -0.1F, -0.1F);
        this.setRotation(TopRotated, 0.0F, 0.7853981633974483F, 0.0F);
        
        this.showRing=showRing;
        if (showRing){
	        this.Ring = new TGModelPart(this, 0, 7);
	        this.Ring.setPivot(-0.5F, -20.2F, -3.3F);
	        this.Ring.addCuboid(0.0F, 0.0F, 0.0F, 2, 4, 4, -0.9F, -0.9F, -0.9F);
        }
        
        this.MainRot1 = new TGModelPart(this, 13, 16);
        this.MainRot1.setPivot(0.5F, -16.0F, 0.5F);
        this.MainRot1.addCuboid(-2.0F, 0.0F, -2.0F, 4, 2, 4, -0.1F, -0.1F, -0.1F);
        this.setRotation(MainRot1, 0.0F, 0.7853981633974483F, 0.0F);
        this.Top = new TGModelPart(this, 15, 11);
        this.Top.setPivot(-1.0F, -17.0F, -1.0F);
        this.Top.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 3, 0.0F);
        this.Box01 = new TGModelPart(this, 19, 28);
        this.Box01.setPivot(0.6F, -18.8F, -0.5F);
        this.Box01.addCuboid(0.0F, 0.0F, 0.0F, 4, 1, 2, -0.3F, -0.3F, -0.3F);
        this.setRotation(Box01, 0.0F, 0.0F, 0.40980330836826856F);
        this.Bottom = new TGModelPart(this, 15, 11);
        this.Bottom.setPivot(-1.0F, -12.0F, -1.0F);
        this.Bottom.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 3, 0.0F);
    }    
    
	@Override
	public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
		float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
	int overlay) {
		
		this.Main.render(matrices, vertices, light, overlay);
        this.BottomRotated.render(matrices, vertices, light, overlay);
        this.MainRot2.render(matrices, vertices, light, overlay);
        this.Box02.render(matrices, vertices, light, overlay);
        this.Bottom.render(matrices, vertices, light, overlay);
        if (showRing && fireProgress<=0.0f){
        	this.Ring.render(matrices, vertices, light, overlay);
        }
        this.TopPart.render(matrices, vertices, light, overlay);
        this.MainRot1.render(matrices, vertices, light, overlay);
        this.Top.render(matrices, vertices, light, overlay);
        this.TopRotated.render(matrices, vertices, light, overlay);
        this.Box01.render(matrices, vertices, light, overlay);
	}

}
