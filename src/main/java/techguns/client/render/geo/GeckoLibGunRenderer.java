package techguns.client.render.geo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import techguns.TGIds;
import techguns.Techguns;
import techguns.client.model.GeoAnimInput;
import techguns.client.model.GeoBones;
import techguns.client.model.TGGeoBridge;
import techguns.client.model.TGGeoExporter;

/**
 * GeckoLib implementation of {@link TGGeoBridge}: draws gun geo models inside the Techguns gun renderer.
 * <p>
 * Models: {@code assets/techguns/geo/item/<id>.geo.json} (exported from the Java models by {@link TGGeoExporter}),
 * animations (optional): {@code assets/techguns/animations/item/<id>.animation.json}, falling back to
 * {@code common.animation.json} per animation name. Animations are sampled from the gun's timers by
 * {@link GunAnimationSampler}, not played by GeckoLib's controllers. Bones whose name contains
 * {@link TGGeoExporter#GLOW_SUFFIX} are drawn full bright.
 */
// MIGRATION(26.x): GeckoLib 5 moves to render states; GeoObjectRenderer/GeoModel signatures change.
public final class GeckoLibGunRenderer implements TGGeoBridge {
    private static final int FULL_BRIGHT = 0xF000F0;

    private static final String ROOT = "root";

    private final Map<String, GunRenderer> renderers = new HashMap<>();

    @Override
    public boolean render(String id, PoseStack poseStack, MultiBufferSource buffers, VertexConsumer buffer, int light, int overlay,
                          GeoAnimInput anim, Consumer<GeoBones> bones, BiConsumer<String, Matrix4f> markers) {
        GunRenderer renderer = renderers.computeIfAbsent(id, GunRenderer::new);
        if (!GeckoLibCache.getBakedModels().containsKey(renderer.gunModel.model)) {
            if (!renderer.warned) {
                renderer.warned = true;
                Techguns.LOGGER.warn("Gun geo model {} not loaded, using the Java model", renderer.gunModel.model);
            }
            return false;
        }
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        GunAnimatable animatable = renderer.animatable;
        animatable.anim = anim;
        animatable.bones = bones;
        renderer.markers = markers;

        poseStack.pushPose();
        // Java model space (y down) from GeckoLib space (y up), see TGGeoExporter
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        // render type null: GeckoLib draws into the given buffer, whose render type the gun renderer chose for this part
        renderer.render(poseStack, animatable, buffers, null, buffer, light, partialTick);
        poseStack.popPose();
        renderer.markers = null;
        return true;
    }

    /** Input of the current draw; a gun's pose depends only on it, so one animatable serves every holder. */
    private static final class GunAnimatable implements GeoAnimatable {
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
        GeoAnimInput anim = GeoAnimInput.NONE;
        /** Code-driven bone changes for the current frame. */
        Consumer<GeoBones> bones = b -> {
        };

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return cache;
        }

        @Override
        public double getTick(Object object) {
            return 0;
        }
    }

    // GeckoLib 4 deprecates the one-argument resource getters but still declares them abstract
    @SuppressWarnings("deprecation")
    private static final class GunModel extends GeoModel<GunAnimatable> {
        final ResourceLocation model;
        final ResourceLocation animation;
        private final Map<String, GunAnimationSampler.BonePose> poses = new HashMap<>();
        /** Only the aim layer: the pose of the marker bones at rest ({@link TGGeoBridge#REST}). */
        final Map<String, GunAnimationSampler.BonePose> aimPoses = new HashMap<>();

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

        /**
         * Poses every bone from scratch for this draw instead of GeckoLib's controller processing: rest pose, plus the
         * sampled animations (aim, reload and its whole-gun motion, fire and recoil, charge), then the code-driven bone
         * changes.
         */
        @Override
        public void handleAnimations(GunAnimatable animatable, long instanceId, AnimationState<GunAnimatable> animationState,
                                     float partialTick) {
            GeoAnimInput anim = animatable.anim;
            aimPoses.clear();
            sample("aim", anim.aim(), 1f, 1f, aimPoses);
            poses.clear();
            sample("aim", anim.aim(), 1f, 1f, poses);
            sample("reload", anim.reload(), 1f, 1f, poses);
            sample(anim.reloadMotion(), anim.reload());
            if (anim.empty() && GunAnimationSampler.find(animation, "fire_last") != null) {
                // the shot that emptied the gun; its end pose stays while the gun is empty (slide locked back)
                sample("fire_last", anim.fire() > 0f ? anim.fire() : 1f, 1f, 1f, poses);
            } else {
                sample("fire", anim.fire(), 1f, 1f, poses);
            }
            sample(anim.recoil(), anim.fire());
            sample("charge", anim.charge(), 1f, 1f, poses);
            if (anim.holdPitch() != 0f || anim.holdYaw() != 0f) {
                // turns about the hand: the root pivot is the hand origin and the model axes are the hand axes
                GunAnimationSampler.BonePose root = poses.computeIfAbsent(ROOT, k -> new GunAnimationSampler.BonePose());
                root.rotX += Math.toRadians(anim.holdPitch());
                root.rotY += Math.toRadians(anim.holdYaw());
            }
            if (anim.mirror()) {
                mirrorRoot(poses);
                mirrorRoot(aimPoses);
            }

            for (GeoBone bone : getAnimationProcessor().getRegisteredBones()) {
                BoneSnapshot rest = bone.getInitialSnapshot();
                GunAnimationSampler.BonePose pose = poses.get(bone.getName());
                bone.setHidden(false);
                if (pose == null) {
                    bone.updateRotation(rest.getRotX(), rest.getRotY(), rest.getRotZ());
                    bone.updatePosition(rest.getOffsetX(), rest.getOffsetY(), rest.getOffsetZ());
                    bone.updateScale(rest.getScaleX(), rest.getScaleY(), rest.getScaleZ());
                } else {
                    bone.updateRotation(rest.getRotX() + (float) pose.rotX, rest.getRotY() + (float) pose.rotY, rest.getRotZ() + (float) pose.rotZ);
                    bone.updatePosition(rest.getOffsetX() + (float) pose.posX, rest.getOffsetY() + (float) pose.posY,
                            rest.getOffsetZ() + (float) pose.posZ);
                    bone.updateScale(rest.getScaleX() * (float) pose.scaleX, rest.getScaleY() * (float) pose.scaleY,
                            rest.getScaleZ() * (float) pose.scaleZ);
                }
            }
            applyCodeBones(animatable);
        }

        private void sample(String name, float progress, float move, float turn, Map<String, GunAnimationSampler.BonePose> into) {
            if (progress > 0f) {
                Animation found = GunAnimationSampler.find(animation, name);
                if (found != null) {
                    GunAnimationSampler.sample(found, progress, move, turn, into);
                }
            }
        }

        private void sample(@Nullable GeoAnimInput.Layer layer, float progress) {
            if (layer != null) {
                sample(layer.animation(), progress, layer.move(), layer.turn(), poses);
            }
        }

        /** Left hand: the whole-gun motion is mirrored, the parts move as authored. */
        private static void mirrorRoot(Map<String, GunAnimationSampler.BonePose> poses) {
            GunAnimationSampler.BonePose root = poses.get(ROOT);
            if (root != null) {
                root.mirror();
            }
        }

        /** After the keyframe animations: code-driven bone changes (belts, static part scales). */
        private void applyCodeBones(GunAnimatable animatable) {
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
        final GunAnimatable animatable = new GunAnimatable();
        /** Receives the marker bone poses during a draw. */
        BiConsumer<String, Matrix4f> markers;
        /** Missing model logged once. */
        boolean warned;

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
            this.objectRenderTranslations = new Matrix4f(poseStack.last().pose());
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

        /**
         * Called with the bone's full transform on the stack: reports marker bones at their pivot, animated and at rest
         * (only the aim layer applied, {@link TGGeoBridge#REST}).
         */
        @Override
        public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay,
                                      int colour) {
            if (markers != null && TGGeoBridge.isMarker(bone.getName())) {
                markers.accept(bone.getName(), new Matrix4f(poseStack.last().pose())
                        .translate(bone.getPivotX() / 16f, bone.getPivotY() / 16f, bone.getPivotZ() / 16f));
                markers.accept(bone.getName() + TGGeoBridge.REST, restPose(bone).translate(
                        bone.getPivotX() / 16f, bone.getPivotY() / 16f, bone.getPivotZ() / 16f));
            }
            super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, colour);
        }

        /**
         * The pose of {@code bone}'s space like RenderUtil#prepMatrixForBone (offset, turn about the pivot), with the rest
         * rotations plus only the aim layer.
         */
        private Matrix4f restPose(GeoBone bone) {
            Matrix4f pose = bone.getParent() != null ? restPose(bone.getParent()) : new Matrix4f(modelRenderTranslations);
            GunAnimationSampler.BonePose aim = gunModel.aimPoses.get(bone.getName());
            BoneSnapshot rest = bone.getInitialSnapshot();
            float rotX = rest.getRotX();
            float rotY = rest.getRotY();
            float rotZ = rest.getRotZ();
            if (aim != null) {
                pose.translate((float) -aim.posX / 16f, (float) aim.posY / 16f, (float) aim.posZ / 16f);
                rotX += (float) aim.rotX;
                rotY += (float) aim.rotY;
                rotZ += (float) aim.rotZ;
            }
            float px = bone.getPivotX() / 16f;
            float py = bone.getPivotY() / 16f;
            float pz = bone.getPivotZ() / 16f;
            return pose.translate(px, py, pz).rotateZYX(rotZ, rotY, rotX).translate(-px, -py, -pz);
        }
    }
}
