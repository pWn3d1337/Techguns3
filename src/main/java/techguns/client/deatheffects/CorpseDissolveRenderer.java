package techguns.client.deatheffects;

import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import techguns.TGIds;
import techguns.core.damage.DeathType;

/**
 * Draws the corpse of an entity killed by a laser or bio weapon: the model burns/rots and fades out under a pulsing
 * glow overlay (Fabric {@code LivingEntityRendererMixin.renderDead}, without a mixin: the vanilla render is canceled
 * in {@code RenderLivingEvent.Pre} and the renderer's model is drawn here).
 */
// MIGRATION(26.x): entity models and renderers move to render states; redo against the new model/state API.
final class CorpseDissolveRenderer {
    static final ResourceLocation BIO_TEXTURE = TGIds.id("textures/fx/bio.png");
    static final ResourceLocation LASER_TEXTURE = TGIds.id("textures/fx/laserdeath.png");
    /** Vanilla removes a dying entity after 20 ticks. */
    private static final float MAX_DEATH_TIME = 20.0f;
    private static final int FULL_BRIGHT = 0xF000F0;

    private CorpseDissolveRenderer() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void render(LivingEntity entity, LivingEntityRenderer<?, ?> renderer, DeathType type, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int light) {
        EntityModel model = renderer.getModel();
        ResourceLocation texture = ((LivingEntityRenderer) renderer).getTextureLocation(entity);
        float prog = Mth.clamp((entity.deathTime + partialTick) / MAX_DEATH_TIME, 0f, 1f);
        float mainColor = 1.0f - prog * prog;
        float mainAlpha = (1.0f - prog) * (1.0f - prog);
        float overlay = (float) (0.5 + Math.sin((Math.sqrt(prog) + 0.75) * 2.0 * Math.PI) / 2.0);

        poseStack.pushPose();
        // the parts of LivingEntityRenderer.setupRotations/render that matter for an upright corpse
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - bodyYaw));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0, -1.501, 0.0);

        float ageInTicks = entity.tickCount + partialTick;
        model.young = entity.isBaby();
        model.prepareMobModel(entity, 0f, 0f, partialTick);
        model.setupAnim(entity, 0f, 0f, ageInTicks, headYaw - bodyYaw, pitch);

        // bio: green tint, every part swells by a random amount; laser: burns to black
        boolean bio = type == DeathType.BIO;
        int mainArgb = bio ? FastColor.ARGB32.colorFromFloat(mainAlpha, mainColor, 1.0f, mainColor)
                : FastColor.ARGB32.colorFromFloat(mainAlpha, 1.0f, mainColor, mainColor);
        int fxArgb = FastColor.ARGB32.colorFromFloat(overlay, 1.0f, 1.0f, 1.0f);
        List<ModelPart> parts = bio ? DeathEffects.modelParts(model) : null;

        // one pass per render type: requesting another buffer from the source ends the previous batch
        drawPass(model, parts, entity, prog, poseStack, buffers.getBuffer(RenderType.entityTranslucent(texture)), light, mainArgb);
        drawPass(model, parts, entity, prog, poseStack,
                buffers.getBuffer(RenderType.itemEntityTranslucentCull(bio ? BIO_TEXTURE : LASER_TEXTURE)), FULL_BRIGHT, fxArgb);
        poseStack.popPose();
    }

    /** Draws the whole model, or each part with its random swelling (same seed in every pass). */
    private static void drawPass(EntityModel<?> model, List<ModelPart> parts, LivingEntity entity, float prog,
                                 PoseStack poseStack, VertexConsumer consumer, int light, int argb) {
        if (parts == null) {
            model.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, argb);
            return;
        }
        Random rand = new Random(entity.getId());
        for (ModelPart part : parts) {
            poseStack.pushPose();
            poseStack.scale(1.0f + rand.nextFloat() * prog, 1.0f + rand.nextFloat() * prog, 1.0f + rand.nextFloat() * prog);
            part.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, argb);
            poseStack.popPose();
        }
    }
}
