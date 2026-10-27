package techguns.client.render.entity;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import techguns.client.model.turret.TurretModel;
import techguns.client.render.block.TurretBaseRenderer;
import techguns.entity.npc.TurretEntity;
import techguns.machine.TurretBlockEntity;
import techguns.registry.TGGuns;

/**
 * Techguns 2 {@code RenderNPCTurret}: the swivel turned by the head yaw, the mount and the gun also by the pitch,
 * mirrored for a hanging turret; the gun sits at its Techguns 2 turret offset.
 */
// MIGRATION(26.x): EntityRenderer#render gets a render state and submit collector in 1.21.2+ / 1.21.9+.
public class TurretRenderer extends EntityRenderer<TurretEntity> {
    private static final Supplier<TurretModel> MODEL = Suppliers.memoize(TurretModel::bake);
    /** Techguns 2 / Fabric {@code setTurretPosOffset} per gun. */
    private static final Supplier<Map<Item, Vec3>> GUN_OFFSETS = Suppliers.memoize(() -> Map.ofEntries(
            Map.entry(TGGuns.M4.get(), new Vec3(0, 0, 0.08)), Map.entry(TGGuns.M4_INFILTRATOR.get(), new Vec3(0, 0, 0.08)),
            Map.entry(TGGuns.AK47.get(), new Vec3(0, 0, 0.08)), Map.entry(TGGuns.SCAR.get(), new Vec3(0, 0.02, 0.09)),
            Map.entry(TGGuns.THOMPSON.get(), new Vec3(0, 0, 0.04)), Map.entry(TGGuns.BOLT_ACTION.get(), new Vec3(0, 0, 0.14)),
            Map.entry(TGGuns.MAC10.get(), new Vec3(0, 0, -0.07)), Map.entry(TGGuns.VECTOR.get(), new Vec3(0, -0.1, 0.15)),
            Map.entry(TGGuns.AS50.get(), new Vec3(0, 0.03, 0.13)), Map.entry(TGGuns.MINIGUN.get(), new Vec3(0, -0.49, -0.14)),
            Map.entry(TGGuns.GUIDED_MISSILE_LAUNCHER.get(), new Vec3(0, 0.01, -0.12)), Map.entry(TGGuns.GRIM_REAPER.get(), new Vec3(0, 0.11, -0.16)),
            Map.entry(TGGuns.ROCKET_LAUNCHER.get(), new Vec3(0, 0, -0.1)), Map.entry(TGGuns.CHAINSAW.get(), new Vec3(0, -0.47, -0.08)),
            Map.entry(TGGuns.NUCLEAR_DEATHRAY.get(), new Vec3(0, 0.04, -0.19)), Map.entry(TGGuns.GAUSS_RIFLE.get(), new Vec3(0, -0.02, 0.12)),
            Map.entry(TGGuns.FLAMETHROWER.get(), new Vec3(0, 0, 0.1)), Map.entry(TGGuns.ALIEN_BLASTER.get(), new Vec3(0, -0.03, -0.04)),
            Map.entry(TGGuns.MIB_GUN.get(), new Vec3(0, -0.04, 0)), Map.entry(TGGuns.NETHER_BLASTER.get(), new Vec3(0, -0.16, 0.12)),
            Map.entry(TGGuns.PULSE_RIFLE.get(), new Vec3(0, 0, -0.09)), Map.entry(TGGuns.MINING_DRILL.get(), new Vec3(0, -0.47, -0.08)),
            Map.entry(TGGuns.LASERGUN.get(), new Vec3(0, 0.01, 0.11)), Map.entry(TGGuns.MAGIC_RIFLE.get(), new Vec3(0, 0, 0.14))));
    /** Techguns 2: the gun sits this high above the mount origin; the mount turns around this height. */
    private static final float GUN_HEIGHT = 0.85f;
    private static final float PITCH_PIVOT = 0.9f;

    public TurretRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(TurretEntity turret) {
        TurretBlockEntity base = turret.base();
        return TurretBaseRenderer.texture(base != null ? base.getTier() : 0);
    }

    @Override
    public void render(TurretEntity turret, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        boolean hanging = turret.isHanging();
        float mirror = hanging ? -1.0f : 1.0f;
        float yaw = Mth.rotLerp(partialTick, turret.yHeadRotO, turret.yHeadRot);
        float pitch = Mth.lerp(partialTick, turret.xRotO, turret.getXRot());
        int overlay = LivingEntityRenderer.getOverlayCoords(turret, 0.0f);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(turret)));
        poseStack.pushPose();
        if (hanging) {
            poseStack.translate(0.0f, -0.6f, 0.0f);
        } else {
            poseStack.translate(0.0f, 1.5f, 0.0f);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        }
        // the model's front is +z; the standing turret's flipped frame turns it to -z, so it needs another half turn
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw * mirror + (hanging ? 0.0f : 180.0f)));
        MODEL.get().renderSwivel(poseStack, consumer, light, overlay);
        poseStack.translate(0.0f, PITCH_PIVOT, 0.0f);
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch * mirror));
        poseStack.translate(0.0f, -PITCH_PIVOT, 0.0f);
        MODEL.get().renderMount(poseStack, consumer, light, overlay);
        ItemStack gun = turret.getWeapon();
        if (!gun.isEmpty()) {
            Vec3 offset = GUN_OFFSETS.get().getOrDefault(gun.getItem(), Vec3.ZERO);
            poseStack.pushPose();
            poseStack.translate(offset.x, offset.y + GUN_HEIGHT, offset.z);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            Minecraft.getInstance().getItemRenderer().renderStatic(turret, gun, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack,
                    buffer, turret.level(), light, overlay, turret.getId());
            poseStack.popPose();
        }
        poseStack.popPose();
        super.render(turret, entityYaw, partialTick, poseStack, buffer, light);
    }
}
