package techguns.client.render.geo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import techguns.TGIds;
import techguns.Techguns;
import techguns.client.model.GeoBones;
import techguns.client.model.TGGeoBridge;
import techguns.client.model.TGGeoExporter;

/**
 * GeckoLib implementation of {@link TGGeoBridge}: draws gun geo models inside the Techguns gun renderer.
 * <p>
 * Models: {@code assets/techguns/geo/item/<id>.geo.json} (exported from the Java models by {@link TGGeoExporter}),
 * animations (optional): {@code assets/techguns/animations/item/<id>.animation.json} with a {@code reload} animation.
 * Bones whose name contains {@link TGGeoExporter#GLOW_SUFFIX} are drawn full bright.
 */
// MIGRATION(26.x): GeckoLib 5 moves to render states; GeoObjectRenderer/AnimationController signatures change.
public final class GeckoLibGunRenderer implements TGGeoBridge {
    private static final int FULL_BRIGHT = 0xF000F0;

    private final Map<String, GunRenderer> renderers = new HashMap<>();

    @Override
    public boolean render(String id, long animationKey, PoseStack poseStack, MultiBufferSource buffers, VertexConsumer buffer,
                          int light, int overlay, float reloadProgress, float fireProgress, Consumer<GeoBones> bones) {
        GunRenderer renderer = renderers.computeIfAbsent(id, GunRenderer::new);
        if (!GeckoLibCache.getBakedModels().containsKey(renderer.gunModel.model)) {
            if (!renderer.warned) {
                renderer.warned = true;
                Techguns.LOGGER.warn("Gun geo model {} not loaded, using the Java model", renderer.gunModel.model);
            }
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(false);
        // one animation state per holder and hand; static copies (GUI, ground) never animate
        GunAnimatable animatable = renderer.animatables.computeIfAbsent(animationKey, k -> new GunAnimatable());
        long gameTime = mc.level != null ? mc.level.getGameTime() : 0;
        animatable.lastSeen = gameTime;
        renderer.prune(gameTime);
        animatable.reloading = animationKey >= 0 && reloadProgress > 0f
                && GeckoLibCache.getBakedAnimations().containsKey(renderer.gunModel.animation);
        animatable.tick = gameTime + partialTick;
        animatable.bones = bones;

        poseStack.pushPose();
        // Java model space (y down) from GeckoLib space (y up), see TGGeoExporter
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        // render type null: GeckoLib draws into the given buffer, whose render type the gun renderer chose for this part
        renderer.render(poseStack, animatable, buffers, null, buffer, light, partialTick);
        poseStack.popPose();
        return true;
    }

    /** Animation state of one rendered copy of a gun. */
    private static final class GunAnimatable implements GeoAnimatable {
        private static final RawAnimation RELOAD = RawAnimation.begin().thenPlay("reload");

        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
        boolean reloading;
        double tick;
        /** Game time of the last draw; holders not drawn for a while are dropped. */
        long lastSeen;
        /** Code-driven bone changes for the current frame. */
        Consumer<GeoBones> bones = b -> {
        };

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            controllers.add(new AnimationController<>(this, "main", 0, state -> {
                if (reloading) {
                    return state.setAndContinue(RELOAD);
                }
                state.getController().forceAnimationReset();
                return PlayState.STOP;
            }));
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return cache;
        }

        @Override
        public double getTick(Object object) {
            return tick;
        }
    }

    // GeckoLib 4 deprecates the one-argument resource getters but still declares them abstract
    @SuppressWarnings("deprecation")
    private static final class GunModel extends GeoModel<GunAnimatable> {
        final ResourceLocation model;
        final ResourceLocation animation;

        GunModel(String id) {
            this.model = TGIds.id("geo/item/" + id + ".geo.json");
            this.animation = TGIds.id("animations/item/" + id + ".animation.json");
        }

        @Override
        public ResourceLocation getModelResource(GunAnimatable animatable) {
            return model;
        }

        @Override
        public ResourceLocation getTextureResource(GunAnimatable animatable) {
            // unused: the gun renderer passes the buffer of the part's render type, which carries the texture
            return MissingTextureAtlasSprite.getLocation();
        }

        @Override
        public ResourceLocation getAnimationResource(GunAnimatable animatable) {
            return animation;
        }

        /** Runs after the keyframe animations: code-driven bone changes (belts, static part scales). */
        @Override
        public void setCustomAnimations(GunAnimatable animatable, long instanceId, AnimationState<GunAnimatable> state) {
            animatable.bones.accept(new GeoBones() {
                @Override
                public void setHidden(String bone, boolean hidden) {
                    getBone(bone).ifPresent(b -> b.setHidden(hidden));
                }

                // GeckoLib translates by (-posX, posY, posZ) / 16 and the model is turned about Z, see TGGeoExporter
                @Override
                public void setOffset(String bone, float x, float y, float z) {
                    getBone(bone).ifPresent(b -> {
                        b.setPosX(x);
                        b.setPosY(-y);
                        b.setPosZ(z);
                    });
                }

                @Override
                public void setScale(String bone, float x, float y, float z) {
                    getBone(bone).ifPresent(b -> {
                        b.setScaleX(x);
                        b.setScaleY(y);
                        b.setScaleZ(z);
                    });
                }
            });
        }
    }

    private static final class GunRenderer extends GeoObjectRenderer<GunAnimatable> {
        final GunModel gunModel;
        /** Keyed by holder and hand; holders not drawn for {@link #STALE_TICKS} are dropped (entity ids are never reused). */
        final Map<Long, GunAnimatable> animatables = new HashMap<>();
        private long lastPrune;
        /** Missing model logged once. */
        boolean warned;

        private static final int STALE_TICKS = 200;

        /** Drops the animation states of holders not drawn recently; at most once every {@link #STALE_TICKS}. */
        void prune(long gameTime) {
            if (Math.abs(gameTime - lastPrune) < STALE_TICKS) {
                return;
            }
            lastPrune = gameTime;
            animatables.values().removeIf(a -> Math.abs(gameTime - a.lastSeen) > STALE_TICKS);
        }

        GunRenderer(String id) {
            this(new GunModel(id));
        }

        private GunRenderer(GunModel model) {
            super(model);
            this.gunModel = model;
        }

        /** No GeckoLib item offset (0.5, 0.51, 0.5): the gun renderer has already placed the model. */
        @Override
        public void preRender(PoseStack poseStack, GunAnimatable animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                              VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
            this.objectRenderTranslations = new org.joml.Matrix4f(poseStack.last().pose());
        }

        /** Glow bones (red dots, energy cells) are drawn full bright like in the Java models. */
        @Override
        public void renderRecursively(PoseStack poseStack, GunAnimatable animatable, GeoBone bone, RenderType renderType,
                                      MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                                      int packedLight, int packedOverlay, int colour) {
            int light = bone.getName().contains(TGGeoExporter.GLOW_SUFFIX) ? FULL_BRIGHT : packedLight;
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, light,
                    packedOverlay, colour);
        }
    }
}
