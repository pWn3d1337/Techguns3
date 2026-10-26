package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;


public class ModelScar extends TGModel {
	private final BoltPart secondary = new BoltPart();

	/** Render part 1 is the nested secondary model. */
	@Override
	public TGModel partModel(int part) {
		return part == 1 ? secondary : this;
	}


	public TGModelPart Grip1;
	public TGModelPart Trigger01;
	public TGModelPart Trigger02;
	public TGModelPart BarrelRails01;
	public TGModelPart BarrelRails02;
	public TGModelPart Barrel;
	public TGModelPart Receiver03;
	public TGModelPart Magazine01;
	public TGModelPart TopRails;
	public TGModelPart Grip01;
	public TGModelPart BarrelRails04;
	public TGModelPart MagSocket;
	public TGModelPart Receiver01;
	public TGModelPart Receiver04;
	public TGModelPart Receiver05;
	public TGModelPart Stock01;
	public TGModelPart Stock02;
	public TGModelPart StockMount;
	public TGModelPart Stock03;
	public TGModelPart Stock04;
	public TGModelPart Stock05;
	public TGModelPart BarrelMount01;
	public TGModelPart BarrelMiniPart;
	public TGModelPart BarrelMount02;
	public TGModelPart Muzzle;
	public TGModelPart FrontSights;
	public TGModelPart FrontSightsMount;
	public TGModelPart BackSightsMount;
	public TGModelPart BackSightsMount2;
	public TGModelPart BackSights;
	public TGModelPart Grip02;
	public TGModelPart Grip03;
	public TGModelPart Grip04;
	public TGModelPart Grip05;
	public TGModelPart Grip06;
	public TGModelPart ScopeMount;
	public TGModelPart ScopePlate;
	public TGModelPart Scope01;
	public TGModelPart Scope02;
	public TGModelPart Scope03;
	public TGModelPart Scope04;
	public TGModelPart Scope05;
	public TGModelPart Stock06;
	public TGModelPart StockMount02;

	public ModelScar() {
		super(RenderType::entitySolid);
		
		this.textureWidth = 128;
		this.textureHeight = 64;
		this.StockMount = new TGModelPart(this, 0, 0);
		this.StockMount.setPivot(-2.5F, -6.5F, 11.5F);
		this.StockMount.addCuboid(0.0F, 0.0F, 0.0F, 4, 7, 2, 0.0F);
		this.Grip03 = new TGModelPart(this, 111, 9);
		this.Grip03.setPivot(-1.5F, -0.5F, -16.5F);
		this.Grip03.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 6, 0.0F);
		this.setRotation(Grip03, -0.6981317007977318F, 0.0F, 0.0F);
		this.Grip04 = new TGModelPart(this, 98, 7);
		this.Grip04.setPivot(-1.5F, -0.1F, -10.5F);
		this.Grip04.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 5, 0.0F);
		this.setRotation(Grip04, -1.9198621771937625F, 0.0F, 0.0F);
		this.Receiver03 = new TGModelPart(this, 46, 36);
		this.Receiver03.setPivot(-2.0F, -1.0F, -6.0F);
		this.Receiver03.addCuboid(0.0F, 0.0F, 0.0F, 3, 3, 16, 0.0F);
		this.Grip1 = new TGModelPart(this, 68, 0);
		this.Grip1.setPivot(-0.5F, 2.0F, 6.0F);
		this.Grip1.addCuboid(-1.5F, 0.0F, 0.0F, 3, 9, 4, 0.0F);
		this.setRotation(Grip1, 0.4461061568097506F, -0.0F, 0.0F);
		this.Stock01 = new TGModelPart(this, 34, 0);
		this.Stock01.setPivot(-2.0F, -2.5F, 13.0F);
		this.Stock01.addCuboid(0.0F, 0.0F, 0.0F, 3, 3, 10, 0.0F);
		this.BackSightsMount2 = new TGModelPart(this, 22, 5);
		this.BackSightsMount2.setPivot(-0.5F, -7.0F, 9.0F);
		this.BackSightsMount2.addCuboid(-2.0F, -1.0F, -1.0F, 4, 2, 2, 0.0F);
		this.setRotation(BackSightsMount2, -0.7853981633974483F, 0.0F, 0.0F);
		this.Grip05 = new TGModelPart(this, 53, 0);
		this.Grip05.setPivot(-1.5F, 0.3F, -21.3F);
		this.Grip05.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
		this.Muzzle = new TGModelPart(this, 70, 36);
		this.Muzzle.setPivot(-0.5F, -4.2F, -44.0F);
		this.Muzzle.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 6, 0.0F);
		this.setRotation(Muzzle, 0.0F, -0.0F, 0.7853981633974483F);
		this.BarrelMount02 = new TGModelPart(this, 46, 36);
		this.BarrelMount02.setPivot(-0.5F, -5.8F, -30.0F);
		this.BarrelMount02.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 5, 0.0F);
		this.setRotation(BarrelMount02, 0.0F, -0.0F, 0.7853981633974483F);
		this.Stock03 = new TGModelPart(this, 23, 16);
		this.Stock03.setPivot(-0.5F, -3.5F, 21.0F);
		this.Stock03.addCuboid(-1.5F, 0.0F, 0.0F, 3, 10, 3, 0.0F);
		this.setRotation(Stock03, 0.2617993877991494F, -0.0F, 0.0F);
		this.FrontSightsMount = new TGModelPart(this, 0, 48);
		this.FrontSightsMount.setPivot(-1.5F, -6.8F, -26.5F);
		this.FrontSightsMount.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 1, 0.0F);
		this.BackSightsMount = new TGModelPart(this, 22, 0);
		this.BackSightsMount.setPivot(-0.5F, -7.5F, 6.5F);
		this.BackSightsMount.addCuboid(-1.5F, 0.0F, 0.0F, 3, 1, 2, 0.0F);
		this.ScopeMount = new TGModelPart(this, 113, 50);
		this.ScopeMount.setPivot(-0.5F, -7.5F, 0.5F);
		this.ScopeMount.addCuboid(-1.5F, 0.0F, 0.0F, 3, 1, 4, 0.0F);
		this.Receiver04 = new TGModelPart(this, 83, 6);
		this.Receiver04.setPivot(-2.0F, -1.5F, 10.0F);
		this.Receiver04.addCuboid(0.0F, 0.0F, 0.0F, 3, 2, 2, 0.0F);
		this.Grip02 = new TGModelPart(this, 92, 0);
		this.Grip02.setPivot(-2.0F, -0.5F, -15.5F);
		this.Grip02.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 5, 0.0F);
		this.Magazine01 = new TGModelPart(this, 109, 18);
		this.Magazine01.setPivot(-1.5F, 5.0F, -5.5F);
		this.Magazine01.addCuboid(0.0F, 0.0F, 0.0F, 2, 6, 7, 0.0F);
		this.Scope03 = new TGModelPart(this, 115, 33);
		this.Scope03.setPivot(-0.5F, -10.2F, 8.0F);
		this.Scope03.addCuboid(-1.5F, -1.5F, 0.0F, 3, 3, 3, 0.0F);
		this.setRotation(Scope03, 0.0F, 0.0F, 0.7853981633974483F);
		this.Grip01 = new TGModelPart(this, 109, 0);
		this.Grip01.setPivot(-2.0F, -1.5F, -21.5F);
		this.Grip01.addCuboid(0.0F, 0.0F, 0.0F, 3, 2, 6, 0.0F);
		this.Receiver05 = new TGModelPart(this, 76, 20);
		this.Receiver05.setPivot(-2.0F, -1.5F, -8.0F);
		this.Receiver05.addCuboid(0.0F, 0.0F, 0.0F, 3, 2, 2, 0.0F);
		this.BarrelRails04 = new TGModelPart(this, 46, 17);
		this.BarrelRails04.setPivot(-1.5F, -1.0F, -25.0F);
		this.BarrelRails04.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 17, 0.0F);
		this.Receiver01 = new TGModelPart(this, 0, 22);
		this.Receiver01.setPivot(-2.5F, -6.0F, -25.0F);
		this.Receiver01.addCuboid(0.0F, 0.0F, 0.0F, 4, 5, 37, 0.0F);
		this.Grip06 = new TGModelPart(this, 53, 0);
		this.Grip06.setPivot(-1.3F, 1.1F, -15.3F);
		this.Grip06.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
		this.setRotation(Grip06, -0.6981317007977318F, 0.0F, 0.0F);
		this.Scope01 = new TGModelPart(this, 99, 36);
		this.Scope01.setPivot(-0.5F, -10.2F, -3.0F);
		this.Scope01.addCuboid(-1.5F, -1.5F, 0.0F, 3, 3, 5, 0.0F);
		this.setRotation(Scope01, 0.0F, 0.0F, 0.7853981633974483F);
		this.FrontSights = new TGModelPart(this, 0, 53);
		this.FrontSights.setPivot(-1.5F, -7.0F, -30.5F);
		this.FrontSights.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 4, 0.0F);
		this.Stock06 = new TGModelPart(this, 37, 18);
		this.Stock06.setPivot(0.8F, -1.0F, 23.8F);
		this.Stock06.addCuboid(-0.5F, -0.5F, -0.5F, 1, 1, 2, 0.0F);
		this.setRotation(Stock06, 0.0F, 0.0F, 0.7853981633974483F);
		this.MagSocket = new TGModelPart(this, 14, 47);
		this.MagSocket.setPivot(-2.0F, 2.0F, -6.0F);
		this.MagSocket.addCuboid(0.0F, 0.0F, 0.0F, 3, 3, 8, 0.0F);
		this.BarrelRails01 = new TGModelPart(this, 9, 31);
		this.BarrelRails01.setPivot(1.5F, -4.25F, -24.5F);
		this.BarrelRails01.addCuboid(0.0F, 0.0F, 0.0F, 1, 2, 12, 0.0F);
		this.StockMount02 = new TGModelPart(this, 0, 17);
		this.StockMount02.setPivot(-2.5F, -6.0F, 12.5F);
		this.StockMount02.addCuboid(-1.0F, 0.0F, -1.0F, 2, 6, 2, 0.0F);
		this.setRotation(StockMount02, 0.0F, 0.7853981633974483F, 0.0F);
		this.Scope04 = new TGModelPart(this, 93, 36);
		this.Scope04.setPivot(1.2F, -10.2F, 5.0F);
		this.Scope04.addCuboid(-2.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
		this.setRotation(Scope04, -0.7853981633974483F, 0.0F, 0.0F);
		this.Trigger02 = new TGModelPart(this, 87, 1);
		this.Trigger02.setPivot(-1.0F, 1.5F, 5.0F);
		this.Trigger02.addCuboid(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
		this.setRotation(Trigger02, -0.2617993950843811F, -0.0F, 0.0F);
		this.ScopePlate = new TGModelPart(this, 111, 40);
		this.ScopePlate.setPivot(0.0F, -10.0F, 0.0F);
		this.ScopePlate.addCuboid(-1.5F, 0.0F, 0.0F, 2, 3, 6, 0.0F);
		this.BarrelMiniPart = new TGModelPart(this, 0, 36);
		this.BarrelMiniPart.setPivot(-0.5F, -3.8F, -26.0F);
		this.BarrelMiniPart.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 1, 0.0F);
		this.setRotation(BarrelMiniPart, 0.0F, -0.0F, 0.7853981633974483F);
		this.Stock02 = new TGModelPart(this, 2, 0);
		this.Stock02.setPivot(-0.5F, -6.5F, 13.0F);
		this.Stock02.addCuboid(-2.0F, 0.0F, 0.0F, 4, 4, 11, 0.0F);
		this.setRotation(Stock02, -0.17453292519943295F, -0.0F, 0.0F);
		this.Stock04 = new TGModelPart(this, 10, 16);
		this.Stock04.setPivot(-2.0F, -4.5F, 23.8F);
		this.Stock04.addCuboid(0.0F, 0.0F, 0.0F, 3, 11, 3, 0.0F);
		this.BackSights = new TGModelPart(this, 35, 3);
		this.BackSights.setPivot(-0.5F, -7.7F, 9.5F);
		this.BackSights.addCuboid(-0.5F, 0.0F, 0.0F, 1, 1, 3, 0.0F);
		this.BarrelRails02 = new TGModelPart(this, 9, 31);
		this.BarrelRails02.setPivot(-3.5F, -4.25F, -24.5F);
		this.BarrelRails02.addCuboid(0.0F, 0.0F, 0.0F, 1, 2, 12, 0.0F);
		this.Scope05 = new TGModelPart(this, 101, 31);
		this.Scope05.setPivot(-0.5F, -11.3F, 5.0F);
		this.Scope05.addCuboid(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
		this.setRotation(Scope05, 0.0F, 0.7853981633974483F, 0.0F);
		this.Stock05 = new TGModelPart(this, 47, 14);
		this.Stock05.setPivot(-2.5F, -5.0F, 25.8F);
		this.Stock05.addCuboid(0.0F, 0.0F, 0.0F, 4, 12, 2, 0.0F);
		this.Trigger01 = new TGModelPart(this, 78, 12);
		this.Trigger01.setPivot(-1.5F, 4.0F, 2.0F);
		this.Trigger01.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 6, 0.0F);
		this.Barrel = new TGModelPart(this, 0, 32);
		this.Barrel.setPivot(-0.5F, -3.8F, -38.0F);
		this.Barrel.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 8, 0.0F);
		this.setRotation(Barrel, 0.0F, 0.0F, 0.7853981633974483F);
		this.TopRails = new TGModelPart(this, 51, 21);
		this.TopRails.setPivot(-1.5F, -7.0F, -24.5F);
		this.TopRails.addCuboid(0.0F, 0.0F, 0.0F, 2, 1, 36, 0.0F);
		this.Scope02 = new TGModelPart(this, 95, 46);
		this.Scope02.setPivot(-0.5F, -9.4F, 2.0F);
		this.Scope02.addCuboid(-1.5F, -1.5F, 0.0F, 2, 2, 6, 0.0F);
		this.setRotation(Scope02, 0.0F, 0.0F, 0.7853981633974483F);
		this.BarrelMount01 = new TGModelPart(this, 24, 36);
		this.BarrelMount01.setPivot(-0.5F, -4.2F, -30.0F);
		this.BarrelMount01.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 4, 0.0F);
		this.setRotation(BarrelMount01, 0.0F, -0.0F, 0.7853981633974483F);
			// static part scales (were pose stack code in render)
		this.Grip1.setRenderScale(0.99f, 1.0f, 1.0f);
		this.BackSightsMount2.setRenderScale(0.8f, 0.7f, 0.7f);
		this.BarrelMount02.setRenderScale(0.8f, 0.8f, 1.0f);
		this.Stock03.setRenderScale(0.99f, 1.0f, 1.0f);
		this.FrontSightsMount.setRenderScale(0.8f, 1.0f, 1.0f);
		this.BackSightsMount.setRenderScale(0.9f, 1.0f, 1.0f);
		this.ScopeMount.setRenderScale(0.9f, 1.0f, 1.0f);
		this.Scope03.setRenderScale(0.9f, 1.0f, 1.0f);
		this.Grip06.setRenderScale(0.8f, 1.0f, 1.0f);
		this.Scope01.setRenderScale(0.9f, 1.0f, 1.0f);
		this.StockMount02.setRenderScale(0.6f, 1.0f, 0.6f);
		this.Scope04.setRenderScale(0.8f, 0.7f, 0.7f);
		this.ScopePlate.setRenderScale(0.9f, 1.0f, 1.0f);
		this.BarrelMiniPart.setRenderScale(0.7f, 0.7f, 1.0f);
		this.Stock02.setRenderScale(0.99f, 1.0f, 1.0f);
		this.Scope05.setRenderScale(0.7f, 0.7f, 0.7f);
		this.Barrel.setRenderScale(0.7f, 0.7f, 1.0f);
		this.Scope02.setRenderScale(0.9f, 1.0f, 1.0f);
	}

	@Override
	public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
					   float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
					   int overlay) {
		float scale = 0.0625f;
		if (part == 0) {

			this.StockMount.render(matrices, vertices, light, overlay);
			this.Grip03.render(matrices, vertices, light, overlay);
			this.Grip04.render(matrices, vertices, light, overlay);
			this.Receiver03.render(matrices, vertices, light, overlay);
			
			this.Grip1.render(matrices, vertices, light, overlay);
			this.Stock01.render(matrices, vertices, light, overlay);
			this.BackSightsMount2.render(matrices, vertices, light, overlay);
			this.Grip05.render(matrices, vertices, light, overlay);
			this.Muzzle.render(matrices, vertices, light, overlay);
			this.BarrelMount02.render(matrices, vertices, light, overlay);
			this.Stock03.render(matrices, vertices, light, overlay);
			this.FrontSightsMount.render(matrices, vertices, light, overlay);
			this.BackSightsMount.render(matrices, vertices, light, overlay);
			this.ScopeMount.render(matrices, vertices, light, overlay);
			this.Receiver04.render(matrices, vertices, light, overlay);
			this.Grip02.render(matrices, vertices, light, overlay);
			this.Magazine01.render(matrices, vertices, light, overlay);
			this.Scope03.render(matrices, vertices, light, overlay);
			this.Grip01.render(matrices, vertices, light, overlay);
			this.Receiver05.render(matrices, vertices, light, overlay);
			this.BarrelRails04.render(matrices, vertices, light, overlay);
			this.Receiver01.render(matrices, vertices, light, overlay);
			this.Grip06.render(matrices, vertices, light, overlay);
			this.Scope01.render(matrices, vertices, light, overlay);
			this.FrontSights.render(matrices, vertices, light, overlay);
			this.Stock06.render(matrices, vertices, light, overlay);
			this.MagSocket.render(matrices, vertices, light, overlay);
			this.BarrelRails01.render(matrices, vertices, light, overlay);
			this.StockMount02.render(matrices, vertices, light, overlay);
			this.Scope04.render(matrices, vertices, light, overlay);
			this.Trigger02.render(matrices, vertices, light, overlay);

			this.ScopePlate.render(matrices, vertices, light, overlay);
			this.BarrelMiniPart.render(matrices, vertices, light, overlay);
			this.Stock02.render(matrices, vertices, light, overlay);
			this.Stock04.render(matrices, vertices, light, overlay);
			this.BackSights.render(matrices, vertices, light, overlay);
			this.BarrelRails02.render(matrices, vertices, light, overlay);
			this.Scope05.render(matrices, vertices, light, overlay);
			this.Stock05.render(matrices, vertices, light, overlay);
			this.Trigger01.render(matrices, vertices, light, overlay);
			this.Barrel.render(matrices, vertices, light, overlay);
			this.TopRails.render(matrices, vertices, light, overlay);
			this.Scope02.render(matrices, vertices, light, overlay);
			this.BarrelMount01.render(matrices, vertices, light, overlay);
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
		// the bolt moves back and forth once per shot
		if (part == 1 && fireProgress > 0) {
			float movebolt = fireProgress > 0.4f ? 1.0f - (fireProgress - 0.4f) / 0.6f : fireProgress / 0.4f;
			matrices.translate(0, 0, movebolt * 0.6f);
		}
	}

	protected static class BoltPart extends TGModel {

		TGModelPart Bolt;

		public BoltPart() {
			super(RenderType::entitySolid);
			textureWidth = 128;
			textureHeight = 64;
		this.Bolt = new TGModelPart(this, 36, 0);
		this.Bolt.setPivot(2.0F, -4.8F, -10.5F);
		this.Bolt.addCuboid(-0.5F, -0.5F, -0.5F, 2, 1, 1, 0.0F);
		this.setRotation(Bolt, 0.7853981633974483F, 0.0F, 0.0F);
				// static part scales (were pose stack code in render)
		this.Bolt.setRenderScale(1.0f, 0.7f, 0.7f);
	}

		@Override
		public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
						   float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
						   int overlay) {
			float scale = 0.0625f;
				this.Bolt.render(matrices, vertices, light, overlay);
		}
	}
}
