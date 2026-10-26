package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;

public class ModelBlasterRifle extends TGModel {
    public TGModelPart Grip1;
    public TGModelPart Trigger01;
    public TGModelPart Trigger02;
    public TGModelPart BarrelGuard;
    public TGModelPart Barrel;
    public TGModelPart Receiver03;
    public TGModelPart BarrelRails04;
    public TGModelPart MagSocket;
    public TGModelPart Receiver01;
    public TGModelPart BarrelCooling1;
    public TGModelPart BarrelCooling2;
    public TGModelPart MagSocket_1;
    public TGModelPart Magazine;
    public TGModelPart MagRoundThing1;
    public TGModelPart MagRoundThing2;
    public TGModelPart MagSocket2;
    public TGModelPart Receiver01_1;
    public TGModelPart ReceiverTop;
    public TGModelPart SideBox01;
    public TGModelPart scope02;
    public TGModelPart scope01;
    public TGModelPart scope03;
    public TGModelPart FrontPart;

    public ModelBlasterRifle() {
        super(RenderType::entitySolid);
        this.textureWidth = 128;
        this.textureHeight = 64;
        this.Magazine = new TGModelPart(this, 92, 18);
        this.Magazine.setPivot(5.0F, -3.5F, -3.5F);
        this.Magazine.addCuboid(0.0F, 0.0F, 0.0F, 5, 2, 5, 0.0F);
        this.Receiver01_1 = new TGModelPart(this, 0, 31);
        this.Receiver01_1.setPivot(-2.5F, -5.0F, -6.0F);
        this.Receiver01_1.addCuboid(0.0F, 0.0F, 0.0F, 4, 5, 28, 0.0F);
        this.SideBox01 = new TGModelPart(this, 77, 18);
        this.SideBox01.setPivot(1.5F, -8.0F, 6.0F);
        this.SideBox01.addCuboid(0.0F, 0.0F, 0.0F, 3, 5, 8, 0.0F);
        this.scope01 = new TGModelPart(this, 0, 13);
        this.scope01.setPivot(-0.5F, -7.5F, 3.0F);
        this.scope01.addCuboid(-1.5F, -1.5F, 0.0F, 3, 3, 5, 0.0F);
        this.setRotation(scope01, 0.0F, 0.0F, 0.7853981633974483F);
        this.Trigger02 = new TGModelPart(this, 83, 5);
        this.Trigger02.setPivot(-1.0F, 1.5F, 5.0F);
        this.Trigger02.addCuboid(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
        this.setRotation(Trigger02, -0.2617993877991494F, -0.0F, 0.0F);
        this.Receiver03 = new TGModelPart(this, 92, 0);
        this.Receiver03.setPivot(-2.0F, 0.0F, -5.0F);
        this.Receiver03.addCuboid(0.0F, 0.0F, 0.0F, 3, 2, 15, 0.0F);
        this.BarrelRails04 = new TGModelPart(this, 37, 36);
        this.BarrelRails04.setPivot(-1.5F, -0.5F, -20.0F);
        this.BarrelRails04.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 15, 0.0F);
        this.MagRoundThing1 = new TGModelPart(this, 97, 26);
        this.MagRoundThing1.setPivot(2.75F, -4.25F, -4.0F);
        this.MagRoundThing1.addCuboid(-0.5F, -0.5F, 0.0F, 1, 1, 6, 0.0F);
        this.setRotation(MagRoundThing1, 0.0F, 0.0F, 0.7853981633974483F);
        this.BarrelGuard = new TGModelPart(this, 82, 41);
        this.BarrelGuard.setPivot(-0.5F, -2.5F, -25.0F);
        this.BarrelGuard.addCuboid(-2.0F, -2.0F, 0.0F, 4, 4, 19, 0.0F);
        this.setRotation(BarrelGuard, 0.0F, 0.0F, 0.7853981633974483F);
        this.MagSocket2 = new TGModelPart(this, 92, 0);
        this.MagSocket2.setPivot(2.0F, -5.0F, 2.0F);
        this.MagSocket2.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 1, 0.0F);
        this.FrontPart = new TGModelPart(this, 57, 41);
        this.FrontPart.setPivot(-2.0F, -1.25F, -24.0F);
        this.FrontPart.addCuboid(0.0F, 0.0F, 0.0F, 3, 2, 7, 0.0F);
        this.setRotation(FrontPart, -0.22759093446006054F, 0.0F, 0.0F);
        this.BarrelCooling2 = new TGModelPart(this, 66, 38);
        this.BarrelCooling2.setPivot(-0.5F, -2.5F, -22.0F);
        this.BarrelCooling2.addCuboid(-0.5F, -2.5F, 0.0F, 1, 5, 16, 0.0F);
        this.setRotation(BarrelCooling2, 0.0F, 0.0F, -0.7853981633974483F);
        this.ReceiverTop = new TGModelPart(this, 0, 0);
        this.ReceiverTop.setPivot(-1.5F, -6.0F, -5.0F);
        this.ReceiverTop.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 27, 0.0F);
        this.Grip1 = new TGModelPart(this, 68, 0);
        this.Grip1.setPivot(-2.0F, 2.0F, 6.0F);
        this.Grip1.addCuboid(0.0F, 0.0F, 0.0F, 3, 9, 4, 0.0F);
        this.setRotation(Grip1, 0.4461433291435241F, 0.0F, 0.0F);
        this.scope02 = new TGModelPart(this, 34, 14);
        this.scope02.setPivot(-0.5F, -8.0F, 8.0F);
        this.scope02.addCuboid(-1.5F, -1.5F, 0.0F, 3, 3, 8, 0.0F);
        this.setRotation(scope02, 0.0F, 0.0F, 0.7853981633974483F);
        this.BarrelCooling1 = new TGModelPart(this, 66, 38);
        this.BarrelCooling1.setPivot(-0.5F, -2.5F, -22.0F);
        this.BarrelCooling1.addCuboid(-0.5F, -2.5F, 0.0F, 1, 5, 16, 0.0F);
        this.setRotation(BarrelCooling1, 0.0F, 0.0F, 0.7853981633974483F);
        this.MagSocket = new TGModelPart(this, 83, 0);
        this.MagSocket.setPivot(-1.5F, 2.0F, 2.0F);
        this.MagSocket.addCuboid(0.0F, 0.0F, 0.0F, 2, 3, 1, 0.0F);
        this.MagSocket_1 = new TGModelPart(this, 106, 19);
        this.MagSocket_1.setPivot(1.0F, -4.0F, -4.0F);
        this.MagSocket_1.addCuboid(0.0F, 0.0F, 0.0F, 4, 3, 7, 0.0F);
        this.Barrel = new TGModelPart(this, 0, 54);
        this.Barrel.setPivot(-0.5F, -2.5F, -26.0F);
        this.Barrel.addCuboid(-1.5F, -1.5F, 0.0F, 3, 3, 1, 0.0F);
        this.setRotation(Barrel, 0.0F, 0.0F, 0.7853981633974483F);
        this.Trigger01 = new TGModelPart(this, 78, 9);
        this.Trigger01.setPivot(-1.5F, 4.0F, 3.0F);
        this.Trigger01.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 5, 0.0F);
        this.MagRoundThing2 = new TGModelPart(this, 97, 26);
        this.MagRoundThing2.setPivot(4.25F, -4.25F, -4.0F);
        this.MagRoundThing2.addCuboid(-0.5F, -0.5F, 0.0F, 1, 1, 6, 0.0F);
        this.setRotation(MagRoundThing2, 0.0F, 0.0F, 0.7853981633974483F);
        this.Receiver01 = new TGModelPart(this, 0, 0);
        this.Receiver01.setPivot(-0.5F, -3.0F, 22.0F);
        this.Receiver01.addCuboid(-2.5F, -2.5F, 0.0F, 5, 5, 2, 0.0F);
        this.setRotation(Receiver01, 0.0F, -0.0F, 0.7853981633974483F);
        this.scope03 = new TGModelPart(this, 1, 8);
        this.scope03.setPivot(-0.5F, -7.0F, 1.0F);
        this.scope03.addCuboid(-1.0F, -1.0F, 0.0F, 2, 2, 2, 0.0F);
        this.setRotation(scope03, 0.0F, 0.0F, 0.7853981633974483F);
    }

	@Override
	public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
			float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
			int overlay) {
		this.Magazine.render(matrices, vertices, light, overlay);
		this.Receiver01_1.render(matrices, vertices, light, overlay);
		this.SideBox01.render(matrices, vertices, light, overlay);
		this.scope01.render(matrices, vertices, light, overlay);
		this.Trigger02.render(matrices, vertices, light, overlay);
		this.Receiver03.render(matrices, vertices, light, overlay);
		this.BarrelRails04.render(matrices, vertices, light, overlay);
		this.MagRoundThing1.render(matrices, vertices, light, overlay);
		this.BarrelGuard.render(matrices, vertices, light, overlay);
		this.MagSocket2.render(matrices, vertices, light, overlay);
		this.FrontPart.render(matrices, vertices, light, overlay);
		this.BarrelCooling2.render(matrices, vertices, light, overlay);
		this.ReceiverTop.render(matrices, vertices, light, overlay);
		this.Grip1.render(matrices, vertices, light, overlay);
		this.scope02.render(matrices, vertices, light, overlay);
		this.BarrelCooling1.render(matrices, vertices, light, overlay);
		this.MagSocket.render(matrices, vertices, light, overlay);
		this.MagSocket_1.render(matrices, vertices, light, overlay);
		this.Barrel.render(matrices, vertices, light, overlay);
		this.Trigger01.render(matrices, vertices, light, overlay);
		this.MagRoundThing2.render(matrices, vertices, light, overlay);
		this.Receiver01.render(matrices, vertices, light, overlay);
		this.scope03.render(matrices, vertices, light, overlay);
	}

}
