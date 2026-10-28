package techguns.client.render.block;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import techguns.TGIds;
import techguns.client.render.TGRenderTypes;
import techguns.machine.ReactionChamberBlockEntity;

/**
 * Techguns 2 {@code RenderReactionChamber}: the formed chamber itself is the controller's OBJ block model; this draws
 * the item in the middle of the chamber, the heat ray (a camera-facing additive quad pulsing with the intensity)
 * while reacting, and the liquid column filling the glass.
 */
// MIGRATION(26.x): BlockEntityRenderer#render gets a render state and submit collector in 1.21.9+.
public class ReactionChamberRenderer implements BlockEntityRenderer<ReactionChamberBlockEntity> {
    private static final ResourceLocation HEAT_RAY = TGIds.id("textures/fx/heatray.png");
    /** Techguns 2: the column is 1 7/8 blocks wide, from half a block up to 2 15/16 blocks high. */
    private static final float HALF_WIDTH = 1.875f * 0.5f;
    private static final float BOTTOM = 0.5f;
    private static final float HEIGHT = 2.9375f;

    public ReactionChamberRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ReactionChamberBlockEntity chamber, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (!chamber.isFormed()) {
            return;
        }
        BlockPos center = chamber.centerPos();
        float dx = center.getX() - chamber.getBlockPos().getX() + 0.5f;
        float dz = center.getZ() - chamber.getBlockPos().getZ() + 0.5f;
        poseStack.pushPose();
        poseStack.translate(dx, 0.0f, dz);

        ItemStack item = chamber.getShownItem();
        if (!item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0f, 1.5f, 0.0f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f * chamber.chamberDirection().getOpposite().get2DDataValue()));
            Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, overlay, poseStack,
                    buffer, chamber.getLevel(), 0);
            poseStack.popPose();
        }
        FluidStack fluid = chamber.getTank().getFluid();
        if (!fluid.isEmpty()) {
            renderFluid(chamber, fluid, poseStack, buffer, light);
        }
        if (chamber.isReacting() && !chamber.getFocus().isEmpty()) {
            renderHeatRay(chamber, center, partialTick, poseStack, buffer);
        }
        poseStack.popPose();
    }

    /** Techguns 2 {@code renderBeamFocusEffect}: pulses every 10 ticks, turned to the camera around the vertical axis. */
    private void renderHeatRay(ReactionChamberBlockEntity chamber, BlockPos center, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double pulse = ((chamber.getLevel().getGameTime() % 10) + partialTick) / 10.0;
        pulse = 0.875 + 0.125 * (1.0 - Math.cos(Math.PI * pulse * 2.0));
        float w = (float) (HALF_WIDTH * pulse);
        // Techguns 2 computed a brightness from the intensity but drew the ray at full colour
        float alpha = 1.0f;
        float angle = (float) Math.atan2(center.getX() + 0.5 - camera.x, center.getZ() + 0.5 - camera.z);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotation(angle));
        Matrix4f mat = poseStack.last().pose();
        VertexConsumer c = buffer.getBuffer(TGRenderTypes.fxAdditive(HEAT_RAY));
        c.addVertex(mat, -w, BOTTOM, 0).setColor(1.0f, 1.0f, 1.0f, alpha).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
        c.addVertex(mat, -w, HEIGHT, 0).setColor(1.0f, 1.0f, 1.0f, alpha).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
        c.addVertex(mat, w, HEIGHT, 0).setColor(1.0f, 1.0f, 1.0f, alpha).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
        c.addVertex(mat, w, BOTTOM, 0).setColor(1.0f, 1.0f, 1.0f, alpha).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    /** The liquid as a translucent box in the glass, flowing texture while reacting (Techguns 2 {@code drawFluidCubeWithTesselator}). */
    private void renderFluid(ReactionChamberBlockEntity chamber, FluidStack fluid, PoseStack poseStack, MultiBufferSource buffer, int light) {
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation texture = chamber.isReacting() ? ext.getFlowingTexture(fluid) : ext.getStillTexture(fluid);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        int color = ext.getTintColor(fluid);
        float level = fluid.getAmount() / (float) ReactionChamberBlockEntity.TANK_CAPACITY;
        float top = BOTTOM + HEIGHT * level;
        // block light raised to the fluid's glow, sky light kept (a max of the packed values compares sky light first)
        int fluidLight = LightTexture.pack(Math.max(LightTexture.block(light), fluid.getFluid().getFluidType().getLightLevel(fluid)),
                LightTexture.sky(light));
        VertexConsumer c = buffer.getBuffer(RenderType.translucent());
        PoseStack.Pose pose = poseStack.last();
        float w = HALF_WIDTH;
        float v1 = sprite.getV(Math.min(1.0f, level * 2.0f));
        // four sides, then the top
        side(c, pose, sprite, color, fluidLight, -w, -w, w, -w, top, v1, 0, 0, -1);
        side(c, pose, sprite, color, fluidLight, w, w, -w, w, top, v1, 0, 0, 1);
        side(c, pose, sprite, color, fluidLight, -w, w, -w, -w, top, v1, -1, 0, 0);
        side(c, pose, sprite, color, fluidLight, w, -w, w, w, top, v1, 1, 0, 0);
        vertex(c, pose, -w, top, -w, color, sprite.getU0(), sprite.getV0(), fluidLight, 0, 1, 0);
        vertex(c, pose, -w, top, w, color, sprite.getU0(), sprite.getV1(), fluidLight, 0, 1, 0);
        vertex(c, pose, w, top, w, color, sprite.getU1(), sprite.getV1(), fluidLight, 0, 1, 0);
        vertex(c, pose, w, top, -w, color, sprite.getU1(), sprite.getV0(), fluidLight, 0, 1, 0);
        // the translucent layer culls back faces: draw the top from below as well
        vertex(c, pose, w, top, -w, color, sprite.getU1(), sprite.getV0(), fluidLight, 0, -1, 0);
        vertex(c, pose, w, top, w, color, sprite.getU1(), sprite.getV1(), fluidLight, 0, -1, 0);
        vertex(c, pose, -w, top, w, color, sprite.getU0(), sprite.getV1(), fluidLight, 0, -1, 0);
        vertex(c, pose, -w, top, -w, color, sprite.getU0(), sprite.getV0(), fluidLight, 0, -1, 0);
    }

    private static void side(VertexConsumer c, PoseStack.Pose pose, TextureAtlasSprite sprite, int color, int light, float x0, float z0,
                             float x1, float z1, float top, float vTop, float nx, float ny, float nz) {
        vertex(c, pose, x0, BOTTOM, z0, color, sprite.getU0(), sprite.getV0(), light, nx, ny, nz);
        vertex(c, pose, x1, BOTTOM, z1, color, sprite.getU1(), sprite.getV0(), light, nx, ny, nz);
        vertex(c, pose, x1, top, z1, color, sprite.getU1(), vTop, light, nx, ny, nz);
        vertex(c, pose, x0, top, z0, color, sprite.getU0(), vTop, light, nx, ny, nz);
        // and from the inside
        vertex(c, pose, x0, top, z0, color, sprite.getU0(), vTop, light, -nx, -ny, -nz);
        vertex(c, pose, x1, top, z1, color, sprite.getU1(), vTop, light, -nx, -ny, -nz);
        vertex(c, pose, x1, BOTTOM, z1, color, sprite.getU1(), sprite.getV0(), light, -nx, -ny, -nz);
        vertex(c, pose, x0, BOTTOM, z0, color, sprite.getU0(), sprite.getV0(), light, -nx, -ny, -nz);
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose pose, float x, float y, float z, int color, float u, float v, int light,
                               float nx, float ny, float nz) {
        c.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setLight(light).setNormal(pose, nx, ny, nz);
    }

    /** The chamber reaches up to four blocks above and two around the controller. */
    @Override
    public AABB getRenderBoundingBox(ReactionChamberBlockEntity chamber) {
        return new AABB(chamber.getBlockPos()).inflate(3.0).expandTowards(0, 2, 0);
    }
}
