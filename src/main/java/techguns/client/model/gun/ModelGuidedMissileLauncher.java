package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.gun.LockOnTracker;
import techguns.client.model.GeoBones;
import techguns.client.model.TGGeoExporter;
import techguns.client.model.TGModel;
import techguns.client.model.TGModelPart;
import techguns.gun.LockOnGunItem;

/**
 * Guided missile launcher (Fabric {@code ModelGuidedMissileLauncher}): part 0 is the launcher, part 1 the glowing
 * holo sight, which shows a second texture area while the lock-on is complete (first person only).
 */
public class ModelGuidedMissileLauncher extends TGModel {
	private final HoloPart secondary = new HoloPart();

	/** Render part 1 is the nested holo sight. */
	@Override
	public TGModel partModel(int part) {
		return part == 1 ? secondary : this;
	}

	@Override
	public RenderType getRenderType(ResourceLocation texture, int part) {
		return part == 0 ? this.renderType(texture) : RenderType.entityCutout(texture);
	}

	TGModelPart Alpha01;
	TGModelPart Alpha02;
	TGModelPart Alpha03;
	TGModelPart Alpha04;
	TGModelPart Barrel01;
	TGModelPart Barrel02;
	TGModelPart Center01;
	TGModelPart Center02;
	TGModelPart End01;
	TGModelPart End02;
	TGModelPart End03;
	TGModelPart Extra01;
	TGModelPart Extra02;
	TGModelPart Extra03;
	TGModelPart Extra04;
	TGModelPart Extra05;
	TGModelPart Extra06;
	TGModelPart Front01;
	TGModelPart Front02;
	TGModelPart Front03;
	TGModelPart Grip01;
	TGModelPart Grip02;
	TGModelPart Grip03;
	TGModelPart Grip04;
	TGModelPart Optics01;
	TGModelPart Optics02;
	TGModelPart Optics03;
	TGModelPart Optics04;
	TGModelPart Optics05;
	TGModelPart Optics06;
	TGModelPart Optics07;

	public ModelGuidedMissileLauncher() {
		super(RenderType::entitySolid);
		this.textureWidth = 128;
		this.textureHeight = 64;
		this.Optics07 = new TGModelPart(this, 68, 9);
		this.Optics07.setPivot(23.0F, -2.0F, 4.0F);
		this.Optics07.addCuboid(0.0F, 0.0F, 0.0F, 1, 3, 1, 0.0F);
		this.End02 = new TGModelPart(this, 94, 4);
		this.End02.setPivot(-27.0F, -0.5F, -3.5F);
		this.End02.addCuboid(0.0F, 0.0F, 0.0F, 2, 6, 6, 0.0F);
		this.End03 = new TGModelPart(this, 107, 0);
		this.End03.setPivot(-26.9F, -1.0F, -2.0F);
		this.End03.addCuboid(0.0F, 0.0F, 0.0F, 2, 7, 3, 0.0F);
		this.Optics03 = new TGModelPart(this, 35, 17);
		this.Optics03.setPivot(17.0F, -1.0F, -1.5F);
		this.Optics03.addCuboid(0.0F, 0.0F, 0.0F, 8, 1, 2, 0.0F);
		this.Alpha01 = new TGModelPart(this, 0, 57);
		this.Alpha01.setPivot(16.0F, -0.5F, 2.5F);
		this.Alpha01.addCuboid(0.0F, 0.0F, 0.0F, 10, 6, 0, 0.0F);
		this.Extra01 = new TGModelPart(this, 45, 57);
		this.Extra01.setPivot(37.5F, -1.0F, -3.5F);
		this.Extra01.addCuboid(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
		this.Center02 = new TGModelPart(this, 0, 38);
		this.Center02.setPivot(26.0F, -0.5F, -3.5F);
		this.Center02.addCuboid(0.0F, 0.0F, 0.0F, 1, 6, 6, 0.0F);
		this.Front03 = new TGModelPart(this, 0, 0);
		this.Front03.setPivot(26.9F, -0.5F, -2.0F);
		this.Front03.addCuboid(0.0F, 0.0F, 0.0F, 12, 6, 3, 0.0F);
		this.Center01 = new TGModelPart(this, 0, 38);
		this.Center01.setPivot(15.0F, -0.5F, -3.5F);
		this.Center01.addCuboid(0.0F, 0.0F, 0.0F, 1, 6, 6, 0.0F);
		this.Optics05 = new TGModelPart(this, 61, 11);
		this.Optics05.setPivot(23.0F, 3.5F, 3.0F);
		this.Optics05.addCuboid(0.0F, -2.0F, 0.0F, 1, 2, 3, 0.0F);
		this.setRotation(Optics05, 0.5235987755982988F, 0.0F, 0.0F);
		this.Front02 = new TGModelPart(this, 0, 10);
		this.Front02.setPivot(27.0F, 0.0F, -3.0F);
		this.Front02.addCuboid(0.0F, 0.0F, 0.0F, 12, 5, 5, 0.0F);
		this.End01 = new TGModelPart(this, 110, 5);
		this.End01.setPivot(-26.9F, 1.0F, -4.0F);
		this.End01.addCuboid(0.0F, 0.0F, 0.0F, 2, 3, 7, 0.0F);
		this.Optics01 = new TGModelPart(this, 32, 10);
		this.Optics01.setPivot(18.0F, -3.0F, -0.5F);
		this.Optics01.addCuboid(0.0F, -1.0F, -1.0F, 7, 2, 2, 0.0F);
		this.setRotation(Optics01, 0.7853981633974483F, 0.0F, 0.0F);
		this.Grip04 = new TGModelPart(this, 60, 44);
		this.Grip04.setPivot(28.0F, 5.5F, -2.0F);
		this.Grip04.addCuboid(0.0F, 0.0F, 0.0F, 8, 5, 3, 0.0F);
		this.Alpha04 = new TGModelPart(this, 0, 57);
		this.Alpha04.setPivot(16.0F, -0.5F, -3.5F);
		this.Alpha04.addCuboid(0.0F, 0.0F, 0.0F, 10, 6, 0, 0.0F);
		this.Extra06 = new TGModelPart(this, 45, 57);
		this.Extra06.setPivot(27.5F, -1.0F, 1.5F);
		this.Extra06.addCuboid(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
		this.Optics04 = new TGModelPart(this, 51, 10);
		this.Optics04.setPivot(20.0F, 1.5F, 2.0F);
		this.Optics04.addCuboid(0.0F, 0.0F, 0.0F, 5, 2, 1, 0.0F);
		this.Grip01 = new TGModelPart(this, 38, 38);
		this.Grip01.setPivot(16.0F, 5.5F, -1.5F);
		this.Grip01.addCuboid(0.0F, 0.0F, 0.0F, 2, 6, 2, 0.0F);
		this.setRotation(Grip01, 0.0F, 0.0F, 0.3490658503988659F);
		this.Front01 = new TGModelPart(this, 31, 0);
		this.Front01.setPivot(26.9F, 1.0F, -3.5F);
		this.Front01.addCuboid(0.0F, 0.0F, 0.0F, 12, 3, 6, 0.0F);
		this.Grip02 = new TGModelPart(this, 47, 38);
		this.Grip02.setPivot(16.0F, 5.0F, -2.0F);
		this.Grip02.addCuboid(0.0F, 0.0F, 0.0F, 12, 2, 3, 0.0F);
		this.Extra03 = new TGModelPart(this, 45, 57);
		this.Extra03.setPivot(27.5F, -1.0F, -3.5F);
		this.Extra03.addCuboid(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
		this.Extra05 = new TGModelPart(this, 45, 61);
		this.Extra05.setPivot(27.5F, -2.0F, 1.5F);
		this.Extra05.addCuboid(0.0F, 0.0F, 0.0F, 11, 1, 1, 0.0F);
		this.Grip03 = new TGModelPart(this, 43, 44);
		this.Grip03.setPivot(24.0F, 5.5F, -2.5F);
		this.Grip03.addCuboid(0.0F, 0.0F, 0.0F, 4, 5, 4, 0.0F);
		this.Alpha03 = new TGModelPart(this, -6, 51);
		this.Alpha03.setPivot(16.0F, -0.5F, -3.5F);
		this.Alpha03.addCuboid(0.0F, 0.0F, 0.0F, 10, 0, 6, 0.0F);
		this.Extra02 = new TGModelPart(this, 45, 61);
		this.Extra02.setPivot(27.5F, -2.0F, -3.5F);
		this.Extra02.addCuboid(0.0F, 0.0F, 0.0F, 11, 1, 1, 0.0F);
		this.Barrel01 = new TGModelPart(this, 0, 21);
		this.Barrel01.setPivot(-25.0F, 1.0F, -3.0F);
		this.Barrel01.addCuboid(0.0F, 0.0F, 0.0F, 51, 3, 5, 0.0F);
		this.Optics02 = new TGModelPart(this, 35, 14);
		this.Optics02.setPivot(19.0F, -2.0F, -1.5F);
		this.Optics02.addCuboid(0.0F, 0.0F, 0.0F, 5, 1, 2, 0.0F);
		this.Alpha02 = new TGModelPart(this, -6, 51);
		this.Alpha02.setPivot(16.0F, 5.5F, -3.5F);
		this.Alpha02.addCuboid(0.0F, 0.0F, 0.0F, 10, 0, 6, 0.0F);
		this.Barrel02 = new TGModelPart(this, 0, 29);
		this.Barrel02.setPivot(-25.0F, 0.0F, -2.0F);
		this.Barrel02.addCuboid(0.0F, 0.0F, 0.0F, 51, 5, 3, 0.0F);
		this.Optics06 = new TGModelPart(this, 67, 14);
		this.Optics06.setPivot(23.0F, 1.0F, 5.0F);
		this.Optics06.addCuboid(0.0F, 0.0F, 0.0F, 1, 1, 3, 0.0F);
		this.Extra04 = new TGModelPart(this, 45, 57);
		this.Extra04.setPivot(37.5F, -1.0F, 1.5F);
		this.Extra04.addCuboid(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
	}

	@Override
	public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft, float reloadProgress,
			ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
		if (part == 0) {
			Optics07.render(matrices, vertices, light, overlay);
			End02.render(matrices, vertices, light, overlay);
			End03.render(matrices, vertices, light, overlay);
			Optics03.render(matrices, vertices, light, overlay);
			Alpha01.render(matrices, vertices, light, overlay);
			Extra01.render(matrices, vertices, light, overlay);
			Center02.render(matrices, vertices, light, overlay);
			Front03.render(matrices, vertices, light, overlay);
			Center01.render(matrices, vertices, light, overlay);
			Optics05.render(matrices, vertices, light, overlay);
			Front02.render(matrices, vertices, light, overlay);
			End01.render(matrices, vertices, light, overlay);
			Optics01.render(matrices, vertices, light, overlay);
			Grip04.render(matrices, vertices, light, overlay);
			Alpha04.render(matrices, vertices, light, overlay);
			Extra06.render(matrices, vertices, light, overlay);
			Optics04.render(matrices, vertices, light, overlay);
			Grip01.render(matrices, vertices, light, overlay);
			Front01.render(matrices, vertices, light, overlay);
			Grip02.render(matrices, vertices, light, overlay);
			Extra03.render(matrices, vertices, light, overlay);
			Extra05.render(matrices, vertices, light, overlay);
			Grip03.render(matrices, vertices, light, overlay);
			Alpha03.render(matrices, vertices, light, overlay);
			Extra02.render(matrices, vertices, light, overlay);
			Barrel01.render(matrices, vertices, light, overlay);
			Optics02.render(matrices, vertices, light, overlay);
			Alpha02.render(matrices, vertices, light, overlay);
			Barrel02.render(matrices, vertices, light, overlay);
			Optics06.render(matrices, vertices, light, overlay);
			Extra04.render(matrices, vertices, light, overlay);
		} else if (part == 1) {
			matrices.pushPose();
			this.transformPart(matrices, part, ammoLeft, reloadProgress, fireProgress);
			this.secondary.render(entityIn, matrices, vertices, ammoLeft, reloadProgress, transformType, part, fireProgress, chargeProgress, light, overlay);
			matrices.popPose();
		}
	}

	/** the holo sight is drawn at half size around its pivot (applies to the geo version too) */
	@Override
	public void transformPart(PoseStack matrices, int part, int ammoLeft, float reloadProgress, float fireProgress) {
		if (part == 1) {
			TGModelPart h = secondary.Holo2;
			matrices.translate(h.pivotX * scale, h.pivotY * scale, h.pivotZ * scale);
			matrices.scale(0.5F, 0.5F, 0.5F);
			matrices.translate(-h.pivotX * scale, -h.pivotY * scale, -h.pivotZ * scale);
		}
	}

	/** The second holo texture is shown in first person once the lock-on of the held launcher is complete. */
	private static boolean isLocked(ItemDisplayContext context) {
		if (!context.firstPerson()) {
			return false;
		}
		var player = Minecraft.getInstance().player;
		return player != null && player.getMainHandItem().getItem() instanceof LockOnGunItem gun && LockOnTracker.isLocked(gun);
	}

	protected static class HoloPart extends TGModel {

		TGModelPart Holo2;
		TGModelPart Holo2_1;

		public HoloPart() {
			super(RenderType::entityCutout);
			this.textureWidth = 128;
			this.textureHeight = 64;
			this.Holo2 = new TGModelPart(this, 78, 0);
			this.Holo2.setPivot(23.5F, -3.0F, 5.0F);
			this.Holo2.addCuboid(0.0F, 0.0F, 0.0F, 0, 8, 8, 0.0F);
			this.Holo2_1 = new TGModelPart(this, 78, -8);
			this.Holo2_1.setPivot(23.5F, -3.0F, 5.0F);
			this.Holo2_1.addCuboid(0.0F, 0.0F, 0.0F, 0, 8, 8, 0.0F);
		}

		@Override
		public void render(@Nullable LivingEntity entityIn, PoseStack matrices, VertexConsumer vertices, int ammoLeft, float reloadProgress,
				ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
			if (isLocked(transformType)) {
				this.Holo2_1.render(matrices, vertices, bright_light, overlay);
			} else {
				this.Holo2.render(matrices, vertices, bright_light, overlay);
			}
		}

		@Override
		public void animateGeoBones(GeoBones bones, int part, int ammoLeft, float reloadProgress, float fireProgress,
				ItemDisplayContext context) {
			super.animateGeoBones(bones, part, ammoLeft, reloadProgress, fireProgress, context);
			boolean locked = isLocked(context);
			// glow parts export with the glow suffix
			bones.setHidden("Holo2" + TGGeoExporter.GLOW_SUFFIX, locked);
			bones.setHidden("Holo2_1" + TGGeoExporter.GLOW_SUFFIX, !locked);
		}
	}
}
