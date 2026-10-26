package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;

public class ModelNDR extends TGModel {
	private final GlowPart secondary = new GlowPart();

	public TGModelPart shape14;
	public TGModelPart shape15;
	public TGModelPart shape14_1;
	public TGModelPart shape14_2;
	public TGModelPart GLOW;
	public TGModelPart shape14_3;
	public TGModelPart shape1;
	public TGModelPart shape1_1;
	public TGModelPart sixside;
	public TGModelPart shape14_4;
	public TGModelPart shape14_5;
	public TGModelPart shape14_6;
	public TGModelPart shape14_7;
	public TGModelPart shape14_8;
	public TGModelPart shape14_9;
	public TGModelPart shape14_10;
	public TGModelPart shape84;
	public TGModelPart shape84_1;
	public TGModelPart shape84_2;
	public TGModelPart shape84_3;
	public TGModelPart shape84_4;
	public TGModelPart shape84_5;
	public TGModelPart shape84_6;
	public TGModelPart shape84_7;
	public TGModelPart shape99;
	public TGModelPart shape99_1;
	public TGModelPart shape99_2;
	public TGModelPart shape1_;
	public TGModelPart shape1__1;
	public TGModelPart shape1__2;
	public TGModelPart shape1__3;
	public TGModelPart shape1__4;
	public TGModelPart shape1_2;
	public TGModelPart shape1_3;
	public TGModelPart shape1_4;
	public TGModelPart shape1_5;
	public TGModelPart shape1_6;
	public TGModelPart shape1_7;
	public TGModelPart shape1_8;
	public TGModelPart shape1_9;
	public TGModelPart shape1_10;
	public TGModelPart shape1_11;
	public TGModelPart sixside_1;
	public TGModelPart shape14_11;
	public TGModelPart shape14_12;
	public TGModelPart shape1__5;
	public TGModelPart shape1__6;
	public TGModelPart shape1__7;
	public TGModelPart shape1__8;
	public TGModelPart shape1__9;
	public TGModelPart shape45_;
	public TGModelPart shape45;
	public TGModelPart shape45_1;
	public TGModelPart shape48;
	public TGModelPart shape48_1;
	public TGModelPart shape48_2;
	public TGModelPart shape58;
	public TGModelPart shape58_1;
	public TGModelPart shape58_2;
	public TGModelPart shape59;
	public TGModelPart shape60;
	public TGModelPart shape59_1;
	public TGModelPart shape60_1;
	public TGModelPart shape82;
	public TGModelPart shape83;
	public TGModelPart shape59_2;
	public TGModelPart shape60_2;

	/** Render part 0 is the gun, part 1 the nested translucent glow rings drawn while firing. */
	@Override
	public TGModel partModel(int part) {
		return part == 1 ? secondary : this;
	}

	@Override
	public RenderType getRenderType(ResourceLocation texture, int part) {
		return part == 0 ? this.renderType(texture) : RenderType.entityTranslucent(texture);
	}

	public ModelNDR() {
		super(RenderType::entityCutout);
		this.textureWidth = 64;
		this.textureHeight = 64;
		this.shape14_1 = new TGModelPart(this, 54, 15);
		this.shape14_1.setPivot(3.0F, -3.0F, -2.0F);
		this.shape14_1.addCuboid(0.0F, 0.0F, 0.0F, 1, 1, 4, 0.0F);
		this.shape1_8 = new TGModelPart(this, 52, 10);
		this.shape1_8.setPivot(4.0F, -2.6F, -1.5F);
		this.shape1_8.addCuboid(0.0F, 0.0F, -3.0F, 3, 2, 3, 0.0F);
		this.setRotation(shape1_8, 1.0471975511965976F, 0.0F, 0.0F);
		this.shape45_1 = new TGModelPart(this, 0, 55);
		this.shape45_1.setPivot(0.0F, 0.0F, 3.0F);
		this.shape45_1.addCuboid(0.0F, 0.0F, 0.0F, 8, 3, 6, 0.0F);
		this.setRotation(shape45_1, -1.0471975511965976F, 0.0F, 0.0F);
		this.shape1__9 = new TGModelPart(this, 0, 0);
		this.shape1__9.setPivot(0.0F, 8.66F, 0.0F);
		this.shape1__9.addCuboid(0.0F, -3.0F, 0.0F, 3, 3, 5, 0.0F);
		this.GLOW = new TGModelPart(this, 42, 45);
		this.GLOW.setPivot(-4.0F, -2.0F, -2.0F);
		this.GLOW.addCuboid(0.0F, 0.0F, 0.0F, 7, 4, 4, 0.0F);
		this.shape14_9 = new TGModelPart(this, 42, 35);
		this.shape14_9.setPivot(-4.0F, -3.0F, -2.0F);
		this.shape14_9.addCuboid(0.0F, 0.0F, 0.0F, 7, 6, 4, 0.0F);
		this.shape1_2 = new TGModelPart(this, 50, 0);
		this.shape1_2.setPivot(7.0F, 3.46F, -2.0F);
		this.shape1_2.addCuboid(0.0F, -1.0F, -4.0F, 3, 1, 4, 0.0F);
		this.setRotation(shape1_2, -1.0471975511965976F, 0.0F, 0.0F);
		this.sixside = new TGModelPart(this, 0, 0);
		this.sixside.setPivot(-8.0F, -4.33F, -2.5F);
		this.sixside.addCuboid(0.0F, 0.0F, 0.0F, 3, 3, 5, 0.0F);
		this.shape14_3 = new TGModelPart(this, 46, 15);
		this.shape14_3.setPivot(3.0F, -2.0F, -3.0F);
		this.shape14_3.addCuboid(0.0F, 0.0F, 0.0F, 1, 4, 6, 0.0F);
		this.shape99 = new TGModelPart(this, 23, 41);
		this.shape99.setPivot(-5.5F, 5.0F, -2.5F);
		this.shape99.addCuboid(-1.0F, -1.0F, 0.0F, 1, 3, 1, 0.0F);
		this.setRotation(shape99, 0.0F, 0.0F, -0.40980330836826856F);
		this.shape14_6 = new TGModelPart(this, 20, 12);
		this.shape14_6.setPivot(-15.0F, -7.0F, -1.5F);
		this.shape14_6.addCuboid(0.0F, 0.0F, 0.0F, 6, 1, 3, 0.0F);
		this.shape58 = new TGModelPart(this, 24, 4);
		this.shape58.setPivot(-6.5F, 0.0F, 0.0F);
		this.shape58.addCuboid(0.0F, -5.33F, -1.0F, 2, 2, 2, 0.0F);
		this.shape14_5 = new TGModelPart(this, 28, 4);
		this.shape14_5.setPivot(-13.8F, -8.6F, -2.0F);
		this.shape14_5.addCuboid(0.0F, 0.0F, 0.0F, 1, 4, 4, 0.0F);
		this.setRotation(shape14_5, 0.0F, 0.0F, 0.7853981633974483F);
		this.shape1_7 = new TGModelPart(this, 52, 10);
		this.shape1_7.setPivot(4.0F, -2.6F, 1.5F);
		this.shape1_7.addCuboid(0.0F, 0.0F, 0.0F, 3, 2, 3, 0.0F);
		this.setRotation(shape1_7, -1.0471975511965976F, 0.0F, 0.0F);
		this.shape1__4 = new TGModelPart(this, 0, 0);
		this.shape1__4.setPivot(0.0F, 8.66F, 0.0F);
		this.shape1__4.addCuboid(0.0F, -3.0F, 0.0F, 3, 3, 5, 0.0F);
		this.shape14_12 = new TGModelPart(this, 38, 5);
		this.shape14_12.setPivot(-18.0F, -4.0F, -2.0F);
		this.shape14_12.addCuboid(0.0F, 0.0F, 0.0F, 1, 7, 4, 0.0F);
		this.shape1__3 = new TGModelPart(this, 0, 0);
		this.shape1__3.setPivot(0.0F, 8.66F, 5.0F);
		this.shape1__3.addCuboid(0.0F, -3.0F, 0.0F, 3, 3, 5, 0.0F);
		this.setRotation(shape1__3, 1.0471975511965976F, 0.0F, 0.0F);
		this.shape99_2 = new TGModelPart(this, 15, 38);
		this.shape99_2.setPivot(-6.0F, 5.0F, -3.0F);
		this.shape99_2.addCuboid(-2.0F, 0.0F, 0.0F, 2, 5, 2, 0.0F);
		this.shape45_ = new TGModelPart(this, 0, 49);
		this.shape45_.setPivot(-17.0F, -3.5F, -1.5F);
		this.shape45_.addCuboid(0.0F, 0.0F, 0.0F, 8, 3, 3, 0.0F);
		this.shape1__5 = new TGModelPart(this, 0, 0);
		this.shape1__5.setPivot(0.0F, 0.0F, 0.0F);
		this.shape1__5.addCuboid(0.0F, 0.0F, -5.0F, 3, 3, 5, 0.0F);
		this.setRotation(shape1__5, 1.0471975511965976F, 0.0F, 0.0F);
		this.shape1__6 = new TGModelPart(this, 0, 0);
		this.shape1__6.setPivot(0.0F, 0.0F, 5.0F);
		this.shape1__6.addCuboid(0.0F, 0.0F, 0.0F, 3, 3, 5, 0.0F);
		this.setRotation(shape1__6, -1.0471975511965976F, 0.0F, 0.0F);
		this.shape59_1 = new TGModelPart(this, 20, 0);
		this.shape59_1.setPivot(1.5F, -4.83F, -0.5F);
		this.shape59_1.addCuboid(0.0F, 0.0F, 0.0F, 9, 1, 1, 0.0F);
		this.shape1__1 = new TGModelPart(this, 0, 0);
		this.shape1__1.setPivot(0.0F, 0.0F, 5.0F);
		this.shape1__1.addCuboid(0.0F, 0.0F, 0.0F, 3, 3, 5, 0.0F);
		this.setRotation(shape1__1, -1.0471975511965976F, 0.0F, 0.0F);
		this.shape59_2 = new TGModelPart(this, 20, 0);
		this.shape59_2.setPivot(1.5F, -4.83F, -0.5F);
		this.shape59_2.addCuboid(0.0F, 0.0F, 0.0F, 9, 1, 1, 0.0F);
		this.shape14_10 = new TGModelPart(this, 46, 15);
		this.shape14_10.setPivot(-5.0F, -2.0F, -3.0F);
		this.shape14_10.addCuboid(0.0F, 0.0F, 0.0F, 1, 4, 6, 0.0F);
		this.shape59 = new TGModelPart(this, 20, 0);
		this.shape59.setPivot(1.5F, -4.83F, -0.5F);
		this.shape59.addCuboid(0.0F, 0.0F, 0.0F, 9, 1, 1, 0.0F);
		this.shape58_1 = new TGModelPart(this, 18, 8);
		this.shape58_1.setPivot(-6.5F, 0.0F, 0.0F);
		this.shape58_1.addCuboid(-1.0F, -5.33F, -1.0F, 3, 2, 2, 0.0F);
		this.setRotation(shape58_1, 2.0943951023931953F, 0.0F, 0.0F);
		this.shape84_6 = new TGModelPart(this, 12, 25);
		this.shape84_6.setPivot(-32.0F, 0.0F, -0.5F);
		this.shape84_6.addCuboid(0.0F, 0.0F, 0.0F, 1, 4, 1, 0.0F);
		this.shape84_7 = new TGModelPart(this, 16, 25);
		this.shape84_7.setPivot(-32.0F, -2.0F, -0.5F);
		this.shape84_7.addCuboid(0.0F, 0.0F, 0.0F, 6, 2, 1, 0.0F);
		this.shape60_1 = new TGModelPart(this, 30, 2);
		this.shape60_1.setPivot(10.5F, -4.83F, -0.5F);
		this.shape60_1.addCuboid(0.0F, 0.0F, 0.0F, 4, 1, 1, 0.0F);
		this.setRotation(shape60_1, 0.0F, 0.0F, 0.7853981633974483F);
		this.shape1_11 = new TGModelPart(this, 52, 10);
		this.shape1_11.setPivot(4.0F, 2.6F, -1.5F);
		this.shape1_11.addCuboid(0.0F, -2.0F, 0.0F, 3, 2, 3, 0.0F);
		this.shape14_8 = new TGModelPart(this, 54, 15);
		this.shape14_8.setPivot(-5.0F, -3.0F, -2.0F);
		this.shape14_8.addCuboid(0.0F, 0.0F, 0.0F, 1, 1, 4, 0.0F);
		this.shape14_4 = new TGModelPart(this, 16, 16);
		this.shape14_4.setPivot(-20.0F, -5.0F, -2.0F);
		this.shape14_4.addCuboid(0.0F, 0.0F, 0.0F, 14, 1, 4, 0.0F);
		this.shape84_2 = new TGModelPart(this, 34, 25);
		this.shape84_2.setPivot(-25.0F, -1.0F, 0.5F);
		this.shape84_2.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 1, 0.0F);
		this.shape60_2 = new TGModelPart(this, 30, 2);
		this.shape60_2.setPivot(10.5F, -4.83F, -0.5F);
		this.shape60_2.addCuboid(0.0F, 0.0F, 0.0F, 4, 1, 1, 0.0F);
		this.setRotation(shape60_2, 0.0F, 0.0F, 0.7853981633974483F);
		this.shape1_1 = new TGModelPart(this, 50, 0);
		this.shape1_1.setPivot(7.0F, -3.46F, -2.0F);
		this.shape1_1.addCuboid(0.0F, 0.0F, 0.0F, 3, 1, 4, 0.0F);
		this.shape1_ = new TGModelPart(this, 0, 0);
		this.shape1_.setPivot(0.0F, 0.0F, 0.0F);
		this.shape1_.addCuboid(0.0F, 0.0F, -5.0F, 3, 3, 5, 0.0F);
		this.setRotation(shape1_, 1.0471975511965976F, 0.0F, 0.0F);
		this.shape99_1 = new TGModelPart(this, 24, 36);
		this.shape99_1.setPivot(-8.5F, 3.0F, -3.0F);
		this.shape99_1.addCuboid(0.0F, 0.0F, 0.0F, 3, 2, 2, 0.0F);
		this.shape15 = new TGModelPart(this, 38, 25);
		this.shape15.setPivot(-4.0F, -2.0F, -3.0F);
		this.shape15.addCuboid(0.0F, 0.0F, 0.0F, 7, 4, 6, 0.0F);
		this.sixside_1 = new TGModelPart(this, 0, 0);
		this.sixside_1.setPivot(-21.0F, -4.33F, -2.5F);
		this.sixside_1.addCuboid(0.0F, 0.0F, 0.0F, 3, 3, 5, 0.0F);
		this.shape84_5 = new TGModelPart(this, 40, 1);
		this.shape84_5.setPivot(-31.0F, 0.0F, 0.0F);
		this.shape84_5.addCuboid(0.0F, 0.0F, 0.0F, 5, 4, 0, 0.0F);
		this.shape14_11 = new TGModelPart(this, 38, 5);
		this.shape14_11.setPivot(-9.0F, -4.0F, -2.0F);
		this.shape14_11.addCuboid(0.0F, 0.0F, 0.0F, 1, 7, 4, 0.0F);
		this.shape1__7 = new TGModelPart(this, 0, 0);
		this.shape1__7.setPivot(0.0F, 8.66F, 0.0F);
		this.shape1__7.addCuboid(0.0F, -3.0F, -5.0F, 3, 3, 5, 0.0F);
		this.setRotation(shape1__7, -1.0471975511965976F, 0.0F, 0.0F);
		this.shape60 = new TGModelPart(this, 30, 2);
		this.shape60.setPivot(10.5F, -4.83F, -0.5F);
		this.shape60.addCuboid(0.0F, 0.0F, 0.0F, 4, 1, 1, 0.0F);
		this.setRotation(shape60, 0.0F, 0.0F, 0.7853981633974483F);
		this.shape84_3 = new TGModelPart(this, 38, 28);
		this.shape84_3.setPivot(-31.0F, 0.0F, -0.5F);
		this.shape84_3.addCuboid(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
		this.shape1 = new TGModelPart(this, 48, 5);
		this.shape1.setPivot(7.0F, -3.46F, 2.0F);
		this.shape1.addCuboid(0.0F, 0.0F, 0.0F, 4, 1, 4, 0.0F);
		this.setRotation(shape1, -1.0471975511965976F, 0.0F, 0.0F);
		this.shape1_9 = new TGModelPart(this, 52, 10);
		this.shape1_9.setPivot(4.0F, -2.6F, -1.5F);
		this.shape1_9.addCuboid(0.0F, 0.0F, 0.0F, 3, 2, 3, 0.0F);
		this.shape45 = new TGModelPart(this, 0, 55);
		this.shape45.setPivot(0.0F, 0.0F, 0.0F);
		this.shape45.addCuboid(0.0F, 0.0F, -6.0F, 8, 3, 6, 0.0F);
		this.setRotation(shape45, 1.0471975511965976F, 0.0F, 0.0F);
		this.shape14_2 = new TGModelPart(this, 54, 15);
		this.shape14_2.setPivot(3.0F, 2.0F, -2.0F);
		this.shape14_2.addCuboid(0.0F, 0.0F, 0.0F, 1, 1, 4, 0.0F);
		this.shape84 = new TGModelPart(this, 34, 25);
		this.shape84.setPivot(-25.0F, -1.0F, -1.5F);
		this.shape84.addCuboid(0.0F, 0.0F, 0.0F, 2, 2, 1, 0.0F);
		this.shape1_4 = new TGModelPart(this, 48, 5);
		this.shape1_4.setPivot(7.0F, -3.46F, -2.0F);
		this.shape1_4.addCuboid(0.0F, 0.0F, -4.0F, 4, 1, 4, 0.0F);
		this.setRotation(shape1_4, 1.0471975511965976F, 0.0F, 0.0F);
		this.shape1_10 = new TGModelPart(this, 52, 10);
		this.shape1_10.setPivot(4.0F, 2.6F, 1.5F);
		this.shape1_10.addCuboid(0.0F, -2.0F, 0.0F, 3, 2, 3, 0.0F);
		this.setRotation(shape1_10, 1.0471975511965976F, 0.0F, 0.0F);
		this.shape48_1 = new TGModelPart(this, 0, 49);
		this.shape48_1.setPivot(0.0F, 7.79F, 4.5F);
		this.shape48_1.addCuboid(0.0F, -3.0F, 0.0F, 8, 3, 3, 0.0F);
		this.setRotation(shape48_1, 1.0471975511965976F, 0.0F, 0.0F);
		this.shape84_1 = new TGModelPart(this, 26, 25);
		this.shape84_1.setPivot(-23.0F, -3.0F, -2.0F);
		this.shape84_1.addCuboid(0.0F, 0.0F, 0.0F, 2, 6, 4, 0.0F);
		this.shape84_4 = new TGModelPart(this, 34, 35);
		this.shape84_4.setPivot(-26.0F, -2.0F, -0.5F);
		this.shape84_4.addCuboid(0.0F, 0.0F, 0.0F, 3, 4, 1, 0.0F);
		this.shape14 = new TGModelPart(this, 54, 15);
		this.shape14.setPivot(-5.0F, 2.0F, -2.0F);
		this.shape14.addCuboid(0.0F, 0.0F, 0.0F, 1, 1, 4, 0.0F);
		this.shape1__2 = new TGModelPart(this, 0, 0);
		this.shape1__2.setPivot(0.0F, 8.66F, 0.0F);
		this.shape1__2.addCuboid(0.0F, -3.0F, -5.0F, 3, 3, 5, 0.0F);
		this.setRotation(shape1__2, -1.0471975511965976F, 0.0F, 0.0F);
		this.shape1_6 = new TGModelPart(this, 50, 0);
		this.shape1_6.setPivot(7.0F, 3.46F, 2.0F);
		this.shape1_6.addCuboid(0.0F, -1.0F, 0.0F, 3, 1, 4, 0.0F);
		this.setRotation(shape1_6, 1.0471975511965976F, 0.0F, 0.0F);
		this.shape83 = new TGModelPart(this, 17, 29);
		this.shape83.setPivot(0.0F, -12.0F, 0.0F);
		this.shape83.addCuboid(-1.0F, 0.0F, -1.0F, 2, 6, 2, 0.0F);
		this.setRotation(shape83, 0.0F, 0.7853981633974483F, 0.0F);
		this.shape1_3 = new TGModelPart(this, 48, 5);
		this.shape1_3.setPivot(7.0F, 3.46F, -2.0F);
		this.shape1_3.addCuboid(0.0F, -1.0F, 0.0F, 4, 1, 4, 0.0F);
		this.shape1__8 = new TGModelPart(this, 0, 0);
		this.shape1__8.setPivot(0.0F, 8.66F, 5.0F);
		this.shape1__8.addCuboid(0.0F, -3.0F, 0.0F, 3, 3, 5, 0.0F);
		this.setRotation(shape1__8, 1.0471975511965976F, 0.0F, 0.0F);
		this.shape58_2 = new TGModelPart(this, 18, 8);
		this.shape58_2.setPivot(-6.5F, 0.0F, 0.0F);
		this.shape58_2.addCuboid(-1.0F, -5.33F, -1.0F, 3, 2, 2, 0.0F);
		this.setRotation(shape58_2, -2.0943951023931953F, 0.0F, 0.0F);
		this.shape82 = new TGModelPart(this, 18, 12);
		this.shape82.setPivot(-0.5F, -6.0F, -0.5F);
		this.shape82.addCuboid(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
		this.shape14_7 = new TGModelPart(this, 20, 21);
		this.shape14_7.setPivot(-16.0F, -6.0F, -1.5F);
		this.shape14_7.addCuboid(0.0F, 0.0F, 0.0F, 10, 1, 3, 0.0F);
		this.shape1_5 = new TGModelPart(this, 52, 10);
		this.shape1_5.setPivot(4.0F, 2.6F, -1.5F);
		this.shape1_5.addCuboid(0.0F, -2.0F, -3.0F, 3, 2, 3, 0.0F);
		this.setRotation(shape1_5, -1.0471975511965976F, 0.0F, 0.0F);
		this.shape48 = new TGModelPart(this, 0, 49);
		this.shape48.setPivot(0.0F, 7.79F, -1.5F);
		this.shape48.addCuboid(0.0F, -3.0F, -3.0F, 8, 3, 3, 0.0F);
		this.setRotation(shape48, -1.0471975511965976F, 0.0F, 0.0F);
		this.shape48_2 = new TGModelPart(this, 0, 55);
		this.shape48_2.setPivot(0.0F, 7.79F, -1.5F);
		this.shape48_2.addCuboid(0.0F, -3.0F, 0.0F, 8, 3, 6, 0.0F);
		this.shape45_.addChild(this.shape45_1);
		this.sixside_1.addChild(this.shape1__9);
		this.sixside.addChild(this.shape1__4);
		this.sixside.addChild(this.shape1__3);
		this.sixside_1.addChild(this.shape1__5);
		this.sixside_1.addChild(this.shape1__6);
		this.shape58_1.addChild(this.shape59_1);
		this.sixside.addChild(this.shape1__1);
		this.shape58_2.addChild(this.shape59_2);
		this.shape58.addChild(this.shape59);
		this.shape58_1.addChild(this.shape60_1);
		this.shape58_2.addChild(this.shape60_2);
		this.sixside.addChild(this.shape1_);
		this.sixside_1.addChild(this.shape1__7);
		this.shape58.addChild(this.shape60);
		this.shape45_.addChild(this.shape45);
		this.shape45_.addChild(this.shape48_1);
		this.sixside.addChild(this.shape1__2);
		this.shape58_2.addChild(this.shape83);
		this.sixside_1.addChild(this.shape1__8);
		this.shape58_2.addChild(this.shape82);
		this.shape45_.addChild(this.shape48);
		this.shape45_.addChild(this.shape48_2);
	}

	@Override
	public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
					   float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
					   int overlay) {

		
		if (part == 0) {
			this.shape14_1.render(matrices, vertices, light, overlay);
			this.shape1_8.render(matrices, vertices, light, overlay);
			this.shape14_9.render(matrices, vertices, light, overlay);
			this.shape1_2.render(matrices, vertices, light, overlay);
			this.sixside.render(matrices, vertices, light, overlay);
			this.shape14_3.render(matrices, vertices, light, overlay);
			this.shape99.render(matrices, vertices, light, overlay);
			this.shape14_6.render(matrices, vertices, light, overlay);
			this.shape58.render(matrices, vertices, light, overlay);
			this.shape14_5.render(matrices, vertices, light, overlay);
			this.shape1_7.render(matrices, vertices, light, overlay);
			this.shape14_12.render(matrices, vertices, light, overlay);
			this.shape99_2.render(matrices, vertices, light, overlay);
			this.shape45_.render(matrices, vertices, light, overlay);
			this.shape14_10.render(matrices, vertices, light, overlay);
			this.shape58_1.render(matrices, vertices, light, overlay);
			this.shape84_6.render(matrices, vertices, light, overlay);
			this.shape84_7.render(matrices, vertices, light, overlay);
			this.shape1_11.render(matrices, vertices, light, overlay);
			this.shape14_8.render(matrices, vertices, light, overlay);
			this.shape14_4.render(matrices, vertices, light, overlay);
			this.shape84_2.render(matrices, vertices, light, overlay);
			this.shape1_1.render(matrices, vertices, light, overlay);
			this.shape99_1.render(matrices, vertices, light, overlay);
			this.shape15.render(matrices, vertices, light, overlay);
			this.sixside_1.render(matrices, vertices, light, overlay);
			this.shape84_5.render(matrices, vertices, light, overlay);
			this.shape14_11.render(matrices, vertices, light, overlay);
			this.shape84_3.render(matrices, vertices, light, overlay);
			this.shape1.render(matrices, vertices, light, overlay);
			this.shape1_9.render(matrices, vertices, light, overlay);
			this.shape14_2.render(matrices, vertices, light, overlay);
			this.shape84.render(matrices, vertices, light, overlay);
			this.shape1_4.render(matrices, vertices, light, overlay);
			this.shape1_10.render(matrices, vertices, light, overlay);
			this.shape84_1.render(matrices, vertices, light, overlay);
			this.shape84_4.render(matrices, vertices, light, overlay);
			this.shape14.render(matrices, vertices, light, overlay);
			this.shape1_6.render(matrices, vertices, light, overlay);
			this.shape1_3.render(matrices, vertices, light, overlay);
			this.shape58_2.render(matrices, vertices, light, overlay);
			this.shape14_7.render(matrices, vertices, light, overlay);
			this.shape1_5.render(matrices, vertices, light, overlay);

			this.GLOW.render(matrices, vertices, bright_light, overlay);
		}
		if (part == 1 && fireProgress > 0) {
			matrices.pushPose();
			this.transformPart(matrices, part, ammoLeft, reloadProgress, fireProgress);
			this.secondary.render(entityIn, matrices, vertices, ammoLeft, reloadProgress, transformType, part, fireProgress, chargeProgress, light, overlay);
			matrices.popPose();
		}
	}

	/** the glow rings pulse while firing; they are hidden otherwise (the geo version cannot skip a part) */
	@Override
	public void transformPart(PoseStack matrices, int part, int ammoLeft, float reloadProgress, float fireProgress) {
		if (part == 1) {
			float s = fireProgress > 0 ? (float) (0.90 + Math.sin(fireProgress * 2.0 * Math.PI) * 0.1) : 0.0001f;
			matrices.scale(s, s, s);
		}
	}

	protected static class GlowPart extends TGModel {

		TGModelPart Ring1;
		TGModelPart Ring2;
		TGModelPart Ring3;
		TGModelPart Ring4;
		TGModelPart Ring5;
		TGModelPart Ring6;
		TGModelPart Ring7;
		TGModelPart Ring8;

		public GlowPart() {
			super(RenderType::entityTranslucent);
			textureWidth = 64;
			textureHeight = 64;
			Ring1 = ring(25f);
			Ring2 = ring(63f);
			Ring3 = ring(117f);
			Ring4 = ring(155f);
			Ring5 = ring(-155f);
			Ring6 = ring(-117f);
			Ring7 = ring(-63f);
			Ring8 = ring(-25f);
		}

		/** one glow plane, rotated around the barrel axis (Fabric reused a single part and changed its pitch) */
		private TGModelPart ring(float degrees) {
			TGModelPart r = new TGModelPart(this, 48, 58);
			r.setPivot(0.0F, 0.0F, 0.0F);
			r.addCuboid(-4.0F, -6.0F, -0.5F, 7, 5, 1, 0.0F);
			setRotation(r, degrees * (float) (Math.PI / 180.0), 0.0F, 0.0F);
			return r;
		}

		@Override
		public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft,
				float reloadProgress, ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light,
				int overlay) {
			Ring1.render(matrices, vertices, bright_light, overlay);
			Ring2.render(matrices, vertices, bright_light, overlay);
			Ring3.render(matrices, vertices, bright_light, overlay);
			Ring4.render(matrices, vertices, bright_light, overlay);
			Ring5.render(matrices, vertices, bright_light, overlay);
			Ring6.render(matrices, vertices, bright_light, overlay);
			Ring7.render(matrices, vertices, bright_light, overlay);
			Ring8.render(matrices, vertices, bright_light, overlay);
		}
	}
}
