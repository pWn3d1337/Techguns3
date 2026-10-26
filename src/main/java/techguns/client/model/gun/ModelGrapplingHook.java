package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;
import techguns.client.render.TGMatrixOps;
import techguns.entity.projectile.GrapplingHookProjectile;

public class ModelGrapplingHook extends TGModel {
	private final HookPart hookPart = new HookPart();
	private final WheelPart wheelPart = new WheelPart();

	/**
	 * Grappling state of the holder, from its hook entity in the client level: 0 = hook in the gun, 1 = hook launched
	 * (wheel unrolls), -1 = hook attached or pulling (wheel rolls back).
	 */
	private static int status(LivingEntity holder) {
		if (holder.level() instanceof ClientLevel level) {
			for (Entity entity : level.entitiesForRendering()) {
				if (entity instanceof GrapplingHookProjectile hook && hook.getOwner() == holder && hook.isAlive()) {
					return switch (hook.status()) {
						case NONE -> 0;
						case LAUNCHING -> 1;
						default -> -1;
					};
				}
			}
		}
		return 0;
	}

	/** Render part 1 is the hook, part 2 the wheel. */
	@Override
	public TGModel partModel(int part) {
		return part == 1 ? hookPart : part == 2 ? wheelPart : this;
	}

	/** The hook is gone while it is launched. */
	@Override
	public boolean isPartVisible(@Nullable LivingEntity entity, int part) {
		return part != 1 || entity == null || status(entity) == 0;
	}

	/** The wheel spins while the hook is out. */
	@Override
	public void transformPart(PoseStack matrices, @Nullable LivingEntity entity, ItemDisplayContext context, int part, int ammoLeft,
			float reloadProgress, float fireProgress) {
		if (part == 2 && entity != null) {
			int status = status(entity);
			if (status != 0) {
				float t = entity.level().getGameTime() + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
				float spin = ((t * 90.0f) % 360.0f) * Math.signum(status);
				// turns around the wheel's own pivot
				matrices.translate(wheelPart.wheel1.pivotX * scale, wheelPart.wheel1.pivotY * scale, wheelPart.wheel1.pivotZ * scale);
				TGMatrixOps.rotate(matrices, spin, 0f, 0f, 1f);
				matrices.translate(-wheelPart.wheel1.pivotX * scale, -wheelPart.wheel1.pivotY * scale, -wheelPart.wheel1.pivotZ * scale);
			}
		}
	}

	TGModelPart grip05;
	TGModelPart main03;
	TGModelPart grip01;
	TGModelPart tank01;
	TGModelPart main07;
	TGModelPart main08;
	TGModelPart main11;
	TGModelPart tank05;
	TGModelPart grip04;
	TGModelPart tank03;
	TGModelPart main05;
	TGModelPart main10;
	TGModelPart grip02;
	TGModelPart main02;
	TGModelPart tank08;
	TGModelPart main04;
	TGModelPart main09;
	TGModelPart main01;
	TGModelPart grip06;
	TGModelPart tank06;
	TGModelPart main12;
	TGModelPart grip03;
	TGModelPart main06;
	TGModelPart tank02;
	TGModelPart tank04;
	TGModelPart tank07;

	public ModelGrapplingHook() {
		super(RenderType::entitySolid);
		textureWidth = 64;
		textureHeight = 32;
		this.grip05 = new TGModelPart(this, 46, 15);
		this.grip05.setPivot(0.0F, 6.5F, 0.0F);
		this.grip05.addCuboid(-0.5F, -0.5F, -0.5F, 6.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F);
		setRotation(grip05, 0.7853981633974483F, 0.0F, 0.0F);
		this.main03 = new TGModelPart(this, 20, 8);
		this.main03.setPivot(12.5F, 0.0F, 0.0F);
		this.main03.addCuboid(-4.0F, -1.5F, -1.5F, 2.0F, 3.0F, 3.0F, 0.0F, 0.0F, 0.0F);
		setRotation(main03, 0.7853981633974483F, 0.0F, 0.0F);
		this.grip01 = new TGModelPart(this, 36, 10);
		this.grip01.setPivot(0.0F, 2.0F, 0.0F);
		this.grip01.addCuboid(0.0F, 0.0F, -0.5F, 2.0F, 4.0F, 1.0F, 0.0F, 0.0F, 0.0F);
		this.tank01 = new TGModelPart(this, 32, 0);
		this.tank01.setPivot(2.0F, -4.0F, 0.0F);
		this.tank01.addCuboid(0.0F, -1.0F, -1.0F, 7.0F, 3.0F, 2.0F, 0.0F, 0.0F, 0.0F);
		this.main07 = new TGModelPart(this, 0, 14);
		this.main07.setPivot(0.0F, 2.0F, 0.5F);
		this.main07.addCuboid(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 1.0F, 0.0F, 0.0F, 0.0F);
		setRotation(main07, 0.0F, 0.0F, 0.7853981633974483F);
		this.main08 = new TGModelPart(this, 0, 18);
		this.main08.setPivot(-2.6F, -2.0F, 0.0F);
		this.main08.addCuboid(-0.5F, -0.5F, 1.0F, 1.0F, 2.5F, 0.5F, 0.0F, 0.0F, 0.0F);
		this.main11 = new TGModelPart(this, 0, 18);
		this.main11.setPivot(-2.6F, -2.0F, 0.0F);
		this.main11.addCuboid(-0.5F, -0.5F, -1.5F, 1.0F, 2.5F, 0.5F, 0.0F, 0.0F, 0.0F);
		this.tank05 = new TGModelPart(this, 58, 4);
		this.tank05.setPivot(9.0F, -2.0F, 0.0F);
		this.tank05.addCuboid(0.0F, -1.5F, -1.0F, 1.0F, 3.0F, 2.0F, 0.0F, 0.0F, 0.0F);
		this.grip04 = new TGModelPart(this, 36, 15);
		this.grip04.setPivot(0.0F, 6.0F, 0.0F);
		this.grip04.addCuboid(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F);
		this.tank03 = new TGModelPart(this, 58, 0);
		this.tank03.setPivot(9.5F, -3.5F, 0.0F);
		this.tank03.addCuboid(0.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, 0.0F, -0.3F, -0.3F);
		setRotation(tank03, 0.7853981633974483F, 0.0F, 0.0F);
		this.main05 = new TGModelPart(this, 21, 14);
		this.main05.setPivot(0.0F, 0.0F, 0.0F);
		this.main05.addCuboid(0.0F, 0.0F, -1.0F, 5.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F);
		this.main10 = new TGModelPart(this, 4, 18);
		this.main10.setPivot(-3.1F, 0.0F, 0.0F);
		this.main10.addCuboid(0.0F, 0.0F, 1.0F, 1.0F, 3.0F, 0.5F, 0.0F, 0.0F, 0.0F);
		setRotation(main10, 0.0F, 0.0F, -0.7853981633974483F);
		this.grip02 = new TGModelPart(this, 42, 10);
		this.grip02.setPivot(5.0F, 0.5F, 0.0F);
		this.grip02.addCuboid(0.0F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, 0.0F, 0.0F, 0.0F);
		this.main02 = new TGModelPart(this, 0, 10);
		this.main02.setPivot(1.0F, 0.0F, 0.0F);
		this.main02.addCuboid(0.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F);
		setRotation(main02, 0.7853981633974483F, 0.0F, 0.0F);
		this.tank08 = new TGModelPart(this, 58, 0);
		this.tank08.setPivot(-0.5F, -3.5F, 0.0F);
		this.tank08.addCuboid(0.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, 0.0F, -0.3F, -0.3F);
		setRotation(tank08, 0.7853981633974483F, 0.0F, 0.0F);
		this.main04 = new TGModelPart(this, 0, 4);
		this.main04.setPivot(-0.5F, 0.0F, 0.0F);
		this.main04.addCuboid(0.0F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, 0.0F, 0.0F);
		setRotation(main04, 0.7853981633974483F, 0.0F, 0.0F);
		this.main09 = new TGModelPart(this, 4, 18);
		this.main09.setPivot(-3.1F, 0.0F, 0.0F);
		this.main09.addCuboid(0.0F, 0.0F, -1.5F, 1.0F, 3.0F, 0.5F, 0.0F, 0.0F, 0.0F);
		setRotation(main09, 0.0F, 0.0F, -0.7853981633974483F);
		this.main01 = new TGModelPart(this, 50, 0);
		this.main01.setPivot(10.0F, 0.0F, 0.0F);
		this.main01.addCuboid(0.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F);
		setRotation(main01, 0.7853981633974483F, 0.0F, 0.0F);
		this.grip06 = new TGModelPart(this, 32, 10);
		this.grip06.setPivot(2.0F, 2.0F, 0.0F);
		this.grip06.addCuboid(0.0F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, 0.0F, 0.0F);
		setRotation(grip06, 0.0F, 0.0F, -0.39269908169872414F);
		this.tank06 = new TGModelPart(this, 52, 7);
		this.tank06.setPivot(0.0F, -3.5F, 0.0F);
		this.tank06.addCuboid(0.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F);
		setRotation(tank06, 0.7853981633974483F, 0.0F, 0.0F);
		this.main12 = new TGModelPart(this, 8, 20);
		this.main12.setPivot(-2.7F, -1.0F, 0.0F);
		this.main12.addCuboid(0.0F, 0.0F, -0.5F, 3.0F, 0.5F, 1.0F, 0.0F, 0.0F, 0.0F);
		setRotation(main12, 0.0F, 0.0F, 0.3127630032889644F);
		this.grip03 = new TGModelPart(this, 54, 11);
		this.grip03.setPivot(6.0F, 4.5F, 0.0F);
		this.grip03.addCuboid(-1.0F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, 0.0F, 0.0F);
		setRotation(grip03, 0.0F, 0.0F, 0.7853981633974483F);
		this.main06 = new TGModelPart(this, 13, 14);
		this.main06.setPivot(-0.9F, 1.5F, 0.0F);
		this.main06.addCuboid(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F);
		setRotation(main06, 0.0F, 0.0F, 0.7853981633974483F);
		this.tank02 = new TGModelPart(this, 31, 5);
		this.tank02.setPivot(2.0F, -3.5F, 0.0F);
		this.tank02.addCuboid(0.0F, -1.0F, -1.5F, 7.0F, 2.0F, 3.0F, 0.0F, 0.0F, 0.0F);
		this.tank04 = new TGModelPart(this, 48, 4);
		this.tank04.setPivot(9.0F, -3.5F, 0.0F);
		this.tank04.addCuboid(0.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F);
		setRotation(tank04, 0.7853981633974483F, 0.0F, 0.0F);
		this.tank07 = new TGModelPart(this, 46, 10);
		this.tank07.setPivot(0.0F, -2.0F, 0.0F);
		this.tank07.addCuboid(0.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, 0.0F, 0.0F, 0.0F);
	}

	@Override
	public void render(@Nullable LivingEntity entity, PoseStack matrices, VertexConsumer vertices, int ammoLeft, float reloadProgress,
			ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
		if (!isPartVisible(entity, part)) {
			return;
		}
		if (part == 0) {
			grip05.render(matrices, vertices, light, overlay);
			main03.render(matrices, vertices, light, overlay);
			grip01.render(matrices, vertices, light, overlay);
			tank01.render(matrices, vertices, light, overlay);
			main07.render(matrices, vertices, light, overlay);
			main08.render(matrices, vertices, light, overlay);
			main11.render(matrices, vertices, light, overlay);
			tank05.render(matrices, vertices, light, overlay);
			grip04.render(matrices, vertices, light, overlay);
			tank03.render(matrices, vertices, light, overlay);
			main05.render(matrices, vertices, light, overlay);
			main10.render(matrices, vertices, light, overlay);
			grip02.render(matrices, vertices, light, overlay);
			main02.render(matrices, vertices, light, overlay);
			tank08.render(matrices, vertices, light, overlay);
			main04.render(matrices, vertices, light, overlay);
			main09.render(matrices, vertices, light, overlay);
			main01.render(matrices, vertices, light, overlay);
			grip06.render(matrices, vertices, light, overlay);
			tank06.render(matrices, vertices, light, overlay);
			main12.render(matrices, vertices, light, overlay);
			grip03.render(matrices, vertices, light, overlay);
			main06.render(matrices, vertices, light, overlay);
			tank02.render(matrices, vertices, light, overlay);
			tank04.render(matrices, vertices, light, overlay);
			tank07.render(matrices, vertices, light, overlay);
		} else {
			matrices.pushPose();
			this.transformPart(matrices, entity, transformType, part, ammoLeft, reloadProgress, fireProgress);
			this.partModel(part).render(entity, matrices, vertices, ammoLeft, reloadProgress, transformType, part, fireProgress, chargeProgress, light, overlay);
			matrices.popPose();
		}
	}


	protected static class HookPart extends TGModel {

		TGModelPart hook04;
		TGModelPart hook01_1;
		TGModelPart hook03;
		TGModelPart hook01;
		TGModelPart hook05;
		TGModelPart hook06;
		TGModelPart hook02;

		public HookPart() {
			super(RenderType::entitySolid);
			textureWidth = 64;
			textureHeight = 32;
			this.hook04 = new TGModelPart(this, 0, 0);
			this.hook04.setPivot(13.4F, 0.0F, 0.0F);
			this.hook04.addCuboid(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, -0.2F, -0.2F, -0.2F);
			setRotation(hook04, 0.0F, 0.7853981633974483F, 0.0F);
			this.hook01_1 = new TGModelPart(this, 19, 20);
			this.hook01_1.setPivot(15.0F, 0.0F, 0.0F);
			this.hook01_1.addCuboid(0.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F);
			setRotation(hook01_1, 0.7853981633974483F, 0.0F, 0.0F);
			this.hook03 = new TGModelPart(this, 0, 0);
			this.hook03.setPivot(13.4F, 0.0F, 0.0F);
			this.hook03.addCuboid(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, -0.2F, -0.2F, -0.2F);
			setRotation(hook03, 0.0F, 0.0F, -0.7853981633974483F);
			this.hook01 = new TGModelPart(this, 31, 19);
			this.hook01.setPivot(12.0F, 0.0F, 0.0F);
			this.hook01.addCuboid(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, 0.0F, -0.3F, -0.3F);
			setRotation(hook01, 0.7853981633974483F, 0.0F, 0.0F);
			this.hook05 = new TGModelPart(this, 0, 0);
			this.hook05.setPivot(13.4F, 0.0F, 0.0F);
			this.hook05.addCuboid(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, -0.2F, -0.2F, -0.2F);
			setRotation(hook05, 0.0F, 0.0F, 0.7853981633974483F);
			this.hook06 = new TGModelPart(this, 0, 0);
			this.hook06.setPivot(13.4F, 0.0F, 0.0F);
			this.hook06.addCuboid(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, -0.2F, -0.2F, -0.2F);
			setRotation(hook06, 0.0F, -0.7853981633974483F, 0.0F);
			this.hook02 = new TGModelPart(this, 24, 19);
			this.hook02.setPivot(13.0F, 0.0F, 0.0F);
			this.hook02.addCuboid(0.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F);
			setRotation(hook02, 0.7853981633974483F, 0.0F, 0.0F);
		}

		@Override
		public void render(@Nullable LivingEntity entity, PoseStack matrices, VertexConsumer vertices, int ammoLeft, float reloadProgress,
				ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
			hook04.render(matrices, vertices, light, overlay);
			hook01_1.render(matrices, vertices, light, overlay);
			hook03.render(matrices, vertices, light, overlay);
			hook01.render(matrices, vertices, light, overlay);
			hook05.render(matrices, vertices, light, overlay);
			hook06.render(matrices, vertices, light, overlay);
			hook02.render(matrices, vertices, light, overlay);
		}

	}

	protected static class WheelPart extends TGModel {

		TGModelPart wheel1;
		TGModelPart wheel2;

		public WheelPart() {
			super(RenderType::entitySolid);
			textureWidth = 64;
			textureHeight = 32;
			this.wheel1 = new TGModelPart(this, 22, 0);
			this.wheel1.setPivot(-2.6F, -2.0F, 0.0F);
			this.wheel1.addCuboid(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F);
			this.wheel2 = new TGModelPart(this, 22, 4);
			this.wheel2.setPivot(0.0F, 0.0F, 0.0F);
			this.wheel2.addCuboid(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F);
			setRotation(wheel2, 0.0F, 0.0F, 1.5707963267948966F);
			this.wheel1.addChild(this.wheel2);
		}

		@Override
		public void render(@Nullable LivingEntity entity, PoseStack matrices, VertexConsumer vertices, int ammoLeft, float reloadProgress,
				ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
			wheel1.render(matrices, vertices, light, overlay);
		}

	}
}
