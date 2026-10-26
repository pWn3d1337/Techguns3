package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;


public class ModelVector extends TGModel {

    public TGModelPart Grip1;
    public TGModelPart Trigger02;
    public TGModelPart Grip02;
    public TGModelPart Eotech01;
    public TGModelPart Receiver03;
    public TGModelPart TopRails;
    public TGModelPart Eotech02;
    public TGModelPart Eotech03;
    public TGModelPart Eotech04;
    public TGModelPart Grip01;
    public TGModelPart RedDot;
    public TGModelPart Receiver02;
    public TGModelPart Receiver03_1;
    public TGModelPart Receiver04;
    public TGModelPart FrontRails01;
    public TGModelPart Grip03;
    public TGModelPart Bottom;
    public TGModelPart Receiver05;
    public TGModelPart Bottom2;
    public TGModelPart Barrel;
    public TGModelPart Barrel02;
    public TGModelPart Magazine;
    public TGModelPart stock;
    public TGModelPart stock02;
    public TGModelPart stock03;
    public TGModelPart stock04;
    public TGModelPart stock05;
    public TGModelPart stock06;

    public ModelVector() {
        super(RenderType::entitySolid);
        this.textureWidth = 128;
        this.textureHeight = 64;
        this.stock03 = new TGModelPart(this, 82, 16);
        this.stock03.setPivot(-2.0F, -0.2F, 25.0F);
        this.stock03.addCuboid(0.0F, 0.0F, 0.0F, 3, 10, 2, 0.0F);
        this.Receiver04 = new TGModelPart(this, 34, 0);
        this.Receiver04.setPivot(-2.0F, 0.0F, -14.0F);
        this.Receiver04.addCuboid(0.0F, 0.0F, 0.0F, 3, 3, 10, 0.0F);
        this.Eotech03 = new TGModelPart(this, 0, 10);
        this.Eotech03.setPivot(-0.5F, -7.7F, 0.4F);
        this.Eotech03.addCuboid(-2.0F, -0.5F, -2.0F, 4, 1, 4, 0.0F);
        this.stock = new TGModelPart(this, 72, 0);
        this.stock.setPivot(0.0F, -2.0F, 11.0F);
        this.stock.addCuboid(-2.0F, 0.0F, 0.0F, 3, 2, 11, 0.0F);
        this.Receiver03 = new TGModelPart(this, 0, 0);
        this.Receiver03.setPivot(-0.5F, -3.0F, -14.0F);
        this.Receiver03.addCuboid(-2.0F, 0.0F, 0.0F, 4, 3, 25, 0.0F);
        this.Eotech02 = new TGModelPart(this, 65, 32);
        this.Eotech02.setPivot(-0.5F, -4.5F, -1.0F);
        this.Eotech02.addCuboid(-2.0F, -1.5F, -4.5F, 4, 3, 9, 0.0F);
        this.stock04 = new TGModelPart(this, 72, 20);
        this.stock04.setPivot(-0.5F, 7.3F, 23.0F);
        this.stock04.addCuboid(-1.0F, -1.0F, 0.0F, 2, 2, 2, 0.0F);
        this.setRotation(stock04, 0.0F, 0.0F, 0.7853981633974483F);
        this.Barrel02 = new TGModelPart(this, 10, 0);
        this.Barrel02.setPivot(-0.5F, 1.2F, -17.0F);
        this.Barrel02.addCuboid(-1.0F, -1.0F, 0.0F, 2, 2, 1, 0.0F);
        this.setRotation(Barrel02, 0.0F, -0.0F, 0.7853981633974483F);
        this.FrontRails01 = new TGModelPart(this, 37, 16);
        this.FrontRails01.setPivot(-1.5F, 3.0F, -13.5F);
        this.FrontRails01.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 7, 0.0F);
        this.Grip1 = new TGModelPart(this, 0, 29);
        this.Grip1.setPivot(-2.0F, -0.1F, 5.9F);
        this.Grip1.addCuboid(0.0F, 0.0F, 0.0F, 3, 10, 4, 0.0F);
        this.setRotation(Grip1, 0.3490658503988659F, -0.0F, 0.0F);
        this.Bottom = new TGModelPart(this, 94, 12);
        this.Bottom.setPivot(-1.0F, 9.4F, -4.2F);
        this.Bottom.addCuboid(-1.0F, 0.0F, 0.0F, 3, 1, 10, 0.0F);
        this.Magazine = new TGModelPart(this, 60, 0);
        this.Magazine.setPivot(-0.5F, 9.0F, -4.3F);
        this.Magazine.addCuboid(-1.0F, 0.0F, 0.0F, 2, 11, 3, 0.0F);
        this.setRotation(Magazine, 0.3490658503988659F, -0.0F, 0.0F);
        this.Receiver05 = new TGModelPart(this, 11, 16);
        this.Receiver05.setPivot(-0.5F, 9.6F, 5.9F);
        this.Receiver05.addCuboid(-1.5F, -1.0F, -1.0F, 3, 1, 2, 0.0F);
        this.setRotation(Receiver05, -0.3490658503988659F, 0.0F, 0.0F);
        this.Barrel = new TGModelPart(this, 17, 0);
        this.Barrel.setPivot(-0.5F, 1.2F, -16.0F);
        this.Barrel.addCuboid(-0.5F, -0.5F, 0.0F, 1, 1, 2, 0.0F);
        this.setRotation(Barrel, 0.0F, 0.0F, 0.7853981633974483F);
        this.Grip02 = new TGModelPart(this, 35, 13);
        this.Grip02.setPivot(-2.0F, 4.0F, -10.5F);
        this.Grip02.addCuboid(0.0F, 0.0F, 0.0F, 2, 7, 2, 0.0F);
        this.setRotation(Grip02, 0.0F, 0.7853981633974483F, 0.0F);
        this.TopRails = new TGModelPart(this, 36, 23);
        this.TopRails.setPivot(-1.5F, -4.0F, -13.0F);
        this.TopRails.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 23, 0.0F);
        this.Bottom2 = new TGModelPart(this, 91, 0);
        this.Bottom2.setPivot(-1.0F, 8.4F, -3.5F);
        this.Bottom2.addCuboid(-1.0F, 0.0F, 0.0F, 3, 1, 9, 0.0F);
        this.RedDot = new TGModelPart(this, 0, 11);
        this.RedDot.setPivot(-1.0F+0.45F, -7.1F+0.45F, 1.0F+0.45f);
        this.RedDot.addCuboid(0.0F, 0.0F, 0.0F, 0.1f, 0.1f, 0, 0.0F);
        this.Grip01 = new TGModelPart(this, 49, 14);
        this.Grip01.setPivot(-2.0F, 3.1F, -12.5F);
        this.Grip01.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 4, 0.0F);
        this.Eotech01 = new TGModelPart(this, 0, 3);
        this.Eotech01.setPivot(0.7F, -6.5F, 2.0F);
        this.Eotech01.addCuboid(-0.5F, -1.0F, -4.0F, 1, 2, 4, 0.0F);
        this.stock02 = new TGModelPart(this, 64, 15);
        this.stock02.setPivot(-1.0F, -1.1F, 20.1F);
        this.stock02.addCuboid(0.0F, 0.0F, 0.0F, 1, 10, 2, 0.0F);
        this.setRotation(stock02, 0.3490658503988659F, -0.0F, 0.0F);
        this.stock05 = new TGModelPart(this, 72, 25);
        this.stock05.setPivot(-0.5F, 1.8F, 21.0F);
        this.stock05.addCuboid(-1.0F, -1.0F, 0.0F, 2, 2, 4, 0.0F);
        this.setRotation(stock05, 0.0F, -0.0F, 0.7853981633974483F);
        this.Receiver02 = new TGModelPart(this, 16, 29);
        this.Receiver02.setPivot(-1.0F, 0.0F, -3.0F);
        this.Receiver02.addCuboid(-1.0F, 0.0F, 0.0F, 3, 11, 5, 0.0F);
        this.setRotation(Receiver02, 0.3490658503988659F, -0.0F, 0.0F);
        this.stock06 = new TGModelPart(this, 83, 30);
        this.stock06.setPivot(-0.5F, -3.0F, 11.0F);
        this.stock06.addCuboid(-1.5F, 0.0F, 0.0F, 3, 1, 3, 0.0F);
        this.setRotation(stock06, -0.3490658503988659F, 0.0F, 0.0F);
        this.Eotech04 = new TGModelPart(this, 0, 3);
        this.Eotech04.setPivot(-1.7F, -6.5F, 0.4F);
        this.Eotech04.addCuboid(-0.5F, -1.0F, -2.0F, 1, 2, 4, 0.0F);
        this.Receiver03_1 = new TGModelPart(this, 34, 29);
        this.Receiver03_1.setPivot(-1.0F, 0.0F, -8.0F);
        this.Receiver03_1.addCuboid(-1.0F, 0.0F, 0.0F, 3, 11, 5, 0.0F);
        this.setRotation(Receiver03_1, 0.3490658503988659F, -0.0F, 0.0F);
        this.Grip03 = new TGModelPart(this, 0, 19);
        this.Grip03.setPivot(-0.5F, 9.5F, 5.3F);
        this.Grip03.addCuboid(-1.5F, 0.0F, 0.0F, 3, 1, 4, 0.0F);
        this.setRotation(Grip03, 0.31066860685499065F, -0.0F, 0.0F);
        this.Trigger02 = new TGModelPart(this, 20, 20);
        this.Trigger02.setPivot(-1.0F, -0.5F, 5.0F);
        this.Trigger02.addCuboid(0.0F, 0.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotation(Trigger02, -0.17453292519943295F, -0.0F, 0.0F);
    		// static part scales (were pose stack code in render)
		this.Eotech03.setRenderScale(0.8f, 0.8f, 0.8f);
		this.Receiver03.setRenderScale(0.8f, 1.0f, 1.0f);
		this.Eotech02.setRenderScale(0.8f, 0.8f, 0.8f);
		this.Barrel02.setRenderScale(0.7f, 0.7f, 1.0f);
		this.Magazine.setRenderScale(0.99f, 1.0f, 1.0f);
		this.Receiver05.setRenderScale(0.98f, 1.0f, 1.0f);
		this.Eotech01.setRenderScale(0.8f, 0.8f, 0.8f);
		this.Receiver02.setRenderScale(0.99f, 1.0f, 1.0f);
		this.stock06.setRenderScale(0.99f, 1.0f, 1.0f);
		this.Eotech04.setRenderScale(0.8f, 0.8f, 0.8f);
		this.Receiver03_1.setRenderScale(0.99f, 1.0f, 1.0f);
		this.Grip03.setRenderScale(0.99f, 1.0f, 1.0f);
	}

	@Override
	public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
                       float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
                       int overlay) {

        this.stock03.render(matrices, vertices, light, overlay);
        this.Receiver04.render(matrices, vertices, light, overlay);
        this.Eotech03.render(matrices, vertices, light, overlay);
        this.stock.render(matrices, vertices, light, overlay);
        this.Receiver03.render(matrices, vertices, light, overlay);
        this.Eotech02.render(matrices, vertices, light, overlay);
        this.stock04.render(matrices, vertices, light, overlay);
        this.Barrel02.render(matrices, vertices, light, overlay);
        this.FrontRails01.render(matrices, vertices, light, overlay);
        this.Grip1.render(matrices, vertices, light, overlay);
        this.Bottom.render(matrices, vertices, light, overlay);
        this.Magazine.render(matrices, vertices, light, overlay);
        this.Receiver05.render(matrices, vertices, light, overlay);
        this.Barrel.render(matrices, vertices, light, overlay);
        this.Grip02.render(matrices, vertices, light, overlay);
        this.TopRails.render(matrices, vertices, light, overlay);
        this.Bottom2.render(matrices, vertices, light, overlay);
        this.RedDot.render(matrices, vertices, bright_light, overlay);
        this.Grip01.render(matrices, vertices, light, overlay);
        this.Eotech01.render(matrices, vertices, light, overlay);
        this.stock02.render(matrices, vertices, light, overlay);
        this.stock05.render(matrices, vertices, light, overlay);
        this.Receiver02.render(matrices, vertices, light, overlay);
        this.stock06.render(matrices, vertices, light, overlay);
        this.Eotech04.render(matrices, vertices, light, overlay);
        this.Receiver03_1.render(matrices, vertices, light, overlay);
        this.Grip03.render(matrices, vertices, light, overlay);
        this.Trigger02.render(matrices, vertices, light, overlay);
    }

}
