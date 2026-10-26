package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;

public class ModelShishkebap extends TGModel {

	TGModelPart Grip3;
	TGModelPart igniter;
	TGModelPart Grip2;
	TGModelPart bladetop1;
	TGModelPart valve;
	TGModelPart bladetop2;
	TGModelPart tank1;
	TGModelPart guard;
	TGModelPart tank0;
	TGModelPart tube;
	TGModelPart tube1;
	TGModelPart bladetop0;
	TGModelPart bladetop3;
	TGModelPart tube0;
	TGModelPart Grip1;
	TGModelPart tube3;
	TGModelPart blade2;
	TGModelPart Handle;
	TGModelPart blade1;
	TGModelPart blade0;
	TGModelPart tube2;
	TGModelPart tube4;

	public ModelShishkebap() {
		super(RenderType::entitySolid);
		textureWidth = 64;
		textureHeight = 64;
		this.Grip3 = new TGModelPart(this, 18, 34);
		this.Grip3.setPivot(0.0F, -3.0F, 0.0F);
		this.Grip3.addCuboid(-1.5F, 0.0F, -3.0F, 3, 2, 5, 0.0F);
		this.igniter = new TGModelPart(this, 15, 7);
		this.igniter.setPivot(0.0F, -5.3F, -2.0F);
		this.igniter.addCuboid(-0.5F, -5.3F, -0.5F, 1, 7, 1, 0.0F);
		this.setRotation(igniter, 0.17453292519943295F, 0.7853981633974483F, 0.12217304763960307F);
		this.Grip2 = new TGModelPart(this, 0, 14);
		this.Grip2.setPivot(0.0F, 9.0F, 0.0F);
		this.Grip2.addCuboid(-1.0F, 0.0F, -1.0F, 2, 3, 2, 0.0F);
		this.setRotation(Grip2, 0.0F, 0.7853981633974483F, 0.0F);
		this.bladetop1 = new TGModelPart(this, 52, 15);
		this.bladetop1.setPivot(0.0F, -45.5F, 0.6F);
		this.bladetop1.addCuboid(-0.5F, 0.0F, 0.0F, 1, 6, 1, 0.0F);
		this.setRotation(bladetop1, -0.3141592653589793F, 0.0F, 0.0F);
		this.valve = new TGModelPart(this, 2, 27);
		this.valve.setPivot(4.0F, -9.0F, 4.0F);
		this.valve.addCuboid(-1.5F, -1.5F, -1.5F, 1, 3, 3, 0.0F);
		this.setRotation(valve, 0.7853981633974483F, 0.0F, 0.0F);
		this.bladetop2 = new TGModelPart(this, 39, 22);
		this.bladetop2.setPivot(0.0F, -39.7F, -1.4F);
		this.bladetop2.addCuboid(-0.5F, -7.0F, -0.5F, 1, 7, 1, 0.0F);
		this.setRotation(bladetop2, -0.6632251157578453F, 0.7853981633974483F, -0.5061454830783556F);
		this.tank1 = new TGModelPart(this, 39, 15);
		this.tank1.setPivot(0.0F, -13.0F, 4.0F);
		this.tank1.addCuboid(-1.5F, 0.0F, -1.5F, 3, 3, 3, 0.0F);
		this.setRotation(tank1, 0.0F, 0.7853981633974483F, 0.0F);
		this.guard = new TGModelPart(this, 0, 35);
		this.guard.setPivot(0.0F, -4.0F, 1.0F);
		this.guard.addCuboid(-2.5F, 0.0F, -5.0F, 5, 1, 7, 0.0F);
		this.tank0 = new TGModelPart(this, 39, 0);
		this.tank0.setPivot(0.0F, -10.0F, 4.0F);
		this.tank0.addCuboid(-2.0F, 0.0F, -2.0F, 4, 10, 4, 0.0F);
		this.setRotation(tank0, 0.0F, 0.7853981633974483F, 0.0F);
		this.tube = new TGModelPart(this, 20, 0);
		this.tube.setPivot(0.0F, -39.7F, 4.0F);
		this.tube.addCuboid(-0.5F, 0.0F, -0.5F, 1, 27, 1, 0.0F);
		this.setRotation(tube, 0.0F, 0.7853981633974483F, 0.0F);
		this.tube1 = new TGModelPart(this, 11, 27);
		this.tube1.setPivot(0.0F, -21.0F, -0.3F);
		this.tube1.addCuboid(-1.0F, -1.0F, 0.0F, 2, 2, 4, 0.0F);
		this.setRotation(tube1, 0.0F, 0.0F, 0.7853981633974483F);
		this.bladetop0 = new TGModelPart(this, 44, 22);
		this.bladetop0.setPivot(0.0F, -46.0F, 0.6F);
		this.bladetop0.addCuboid(-0.5F, 0.0F, 0.0F, 1, 6, 1, 0.0F);
		this.bladetop3 = new TGModelPart(this, 49, 23);
		this.bladetop3.setPivot(0.0F, -42.5F, -0.4F);
		this.bladetop3.addCuboid(-0.5F, 0.0F, 0.0F, 1, 3, 1, 0.0F);
		this.tube0 = new TGModelPart(this, 11, 27);
		this.tube0.setPivot(0.0F, -15.0F, -0.3F);
		this.tube0.addCuboid(-1.0F, -1.0F, 0.0F, 2, 2, 4, 0.0F);
		this.setRotation(tube0, 0.0F, 0.0F, 0.7853981633974483F);
		this.Grip1 = new TGModelPart(this, 0, 0);
		this.Grip1.setPivot(0.0F, -1.0F, 0.0F);
		this.Grip1.addCuboid(-1.5F, 0.0F, -1.5F, 3, 10, 3, 0.0F);
		this.setRotation(Grip1, 0.0F, 0.7853981633974483F, 0.0F);
		this.tube3 = new TGModelPart(this, 11, 27);
		this.tube3.setPivot(0.0F, -33.0F, -0.3F);
		this.tube3.addCuboid(-1.0F, -1.0F, 0.0F, 2, 2, 4, 0.0F);
		this.setRotation(tube3, 0.0F, 0.0F, 0.7853981633974483F);
		this.blade2 = new TGModelPart(this, 34, 0);
		this.blade2.setPivot(0.0F, -40.0F, -1.4F);
		this.blade2.addCuboid(-0.5F, 0.0F, -0.5F, 1, 30, 1, 0.0F);
		this.setRotation(blade2, 0.0F, 0.7853981633974483F, 0.0F);
		this.Handle = new TGModelPart(this, 0, 21);
		this.Handle.setPivot(0.0F, -1.5F, -3.0F);
		this.Handle.addCuboid(-0.5F, 0.0F, 0.0F, 1, 7, 1, 0.0F);
		this.setRotation(Handle, -0.2181661564992912F, 0.0F, 0.0F);
		this.blade1 = new TGModelPart(this, 25, 0);
		this.blade1.setPivot(0.0F, -40.0F, -1.4F);
		this.blade1.addCuboid(-0.5F, 0.0F, 0.0F, 1, 30, 3, 0.0F);
		this.blade0 = new TGModelPart(this, 7, 16);
		this.blade0.setPivot(0.0F, -10.0F, 0.0F);
		this.blade0.addCuboid(-1.0F, 0.0F, -2.0F, 2, 6, 4, 0.0F);
		this.tube2 = new TGModelPart(this, 11, 27);
		this.tube2.setPivot(0.0F, -27.0F, -0.3F);
		this.tube2.addCuboid(-1.0F, -1.0F, 0.0F, 2, 2, 4, 0.0F);
		this.setRotation(tube2, 0.0F, 0.0F, 0.7853981633974483F);
		this.tube4 = new TGModelPart(this, 11, 27);
		this.tube4.setPivot(0.0F, -39.0F, -0.3F);
		this.tube4.addCuboid(-1.0F, -1.0F, 0.0F, 2, 2, 4, 0.0F);
		this.setRotation(tube4, 0.0F, 0.0F, 0.7853981633974483F);
		// Tabula scaled these blade parts (formerly pose stack code in render)
		this.bladetop1.setRenderScale(0.9f, 1.0f, 1.0f);
		this.bladetop2.setRenderScale(0.7f, 1.0f, 0.7f);
		this.bladetop3.setRenderScale(0.95f, 1.0f, 1.0f);
		this.blade2.setRenderScale(0.7f, 1.0f, 0.7f);
	}

	@Override
	public void render(@Nullable LivingEntity entity, PoseStack matrices, VertexConsumer vertices, int ammoLeft, float reloadProgress,
			ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
		Grip3.render(matrices, vertices, light, overlay);
		igniter.render(matrices, vertices, light, overlay);
		Grip2.render(matrices, vertices, light, overlay);
		bladetop1.render(matrices, vertices, light, overlay);
		valve.render(matrices, vertices, light, overlay);
		bladetop2.render(matrices, vertices, light, overlay);
		tank1.render(matrices, vertices, light, overlay);
		guard.render(matrices, vertices, light, overlay);
		tank0.render(matrices, vertices, light, overlay);
		tube.render(matrices, vertices, light, overlay);
		tube1.render(matrices, vertices, light, overlay);
		bladetop0.render(matrices, vertices, light, overlay);
		bladetop3.render(matrices, vertices, light, overlay);
		tube0.render(matrices, vertices, light, overlay);
		Grip1.render(matrices, vertices, light, overlay);
		tube3.render(matrices, vertices, light, overlay);
		blade2.render(matrices, vertices, light, overlay);
		Handle.render(matrices, vertices, light, overlay);
		blade1.render(matrices, vertices, light, overlay);
		blade0.render(matrices, vertices, light, overlay);
		tube2.render(matrices, vertices, light, overlay);
		tube4.render(matrices, vertices, light, overlay);
	}
}
