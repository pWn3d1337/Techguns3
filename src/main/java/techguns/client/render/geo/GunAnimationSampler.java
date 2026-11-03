package techguns.client.render.geo;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.keyframe.AnimationPoint;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.animation.keyframe.KeyframeStack;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.math.MolangQueries;
import software.bernie.geckolib.loading.math.value.Constant;
import software.bernie.geckolib.loading.object.BakedAnimations;
import techguns.TGIds;

/**
 * Samples gun animations at a given time instead of playing them through GeckoLib's animation controllers: a gun's pose
 * is a pure function of its timers, so animations stretch exactly to the stat durations, can run backwards (aiming) and
 * need no state per holder. Several animations add up (rotations and offsets add, scales multiply).
 * <p>
 * Values are computed like GeckoLib's {@code AnimationController} does (same keyframe lookup, easing and Molang
 * rotation conversion), so an animation looks the same as in Blockbench.
 */
// MIGRATION(26.x): GeckoLib 5 reworks Animation/Keyframe/EasingType; keep this class's contract.
final class GunAnimationSampler {
    /** Shared animations a gun uses when its own file lacks one of that name. */
    static final ResourceLocation COMMON = TGIds.id("animations/item/common.animation.json");

    private GunAnimationSampler() {
    }

    /** Accumulated change of one bone: rotation (radians) and offset add up, scale multiplies. */
    static final class BonePose {
        double rotX, rotY, rotZ;
        double posX, posY, posZ;
        double scaleX = 1, scaleY = 1, scaleZ = 1;
    }

    /** The animation {@code name} from the gun's own file, else from {@link #COMMON}; null if neither has it. */
    @Nullable
    static Animation find(ResourceLocation own, String name) {
        Map<ResourceLocation, BakedAnimations> all = GeckoLibCache.getBakedAnimations();
        BakedAnimations animations = all.get(own);
        Animation animation = animations != null ? animations.getAnimation(name) : null;
        if (animation == null && (animations = all.get(COMMON)) != null) {
            animation = animations.getAnimation(name);
        }
        return animation;
    }

    /** Adds {@code animation} sampled at {@code progress} (0..1 of its length) to {@code poses}, keyed by bone name. */
    static void sample(Animation animation, float progress, Map<String, BonePose> poses) {
        double tick = progress * animation.length();
        MathParser.setVariable(MolangQueries.ANIM_TIME, () -> tick / 20d);
        for (BoneAnimation bone : animation.boneAnimations()) {
            BonePose pose = poses.computeIfAbsent(bone.boneName(), k -> new BonePose());
            KeyframeStack<Keyframe<MathValue>> rotation = bone.rotationKeyFrames();
            if (!rotation.xKeyframes().isEmpty()) {
                pose.rotX += value(rotation.xKeyframes(), tick, true, true);
                pose.rotY += value(rotation.yKeyframes(), tick, true, true);
                pose.rotZ += value(rotation.zKeyframes(), tick, true, false);
            }
            KeyframeStack<Keyframe<MathValue>> position = bone.positionKeyFrames();
            if (!position.xKeyframes().isEmpty()) {
                pose.posX += value(position.xKeyframes(), tick, false, false);
                pose.posY += value(position.yKeyframes(), tick, false, false);
                pose.posZ += value(position.zKeyframes(), tick, false, false);
            }
            KeyframeStack<Keyframe<MathValue>> scale = bone.scaleKeyFrames();
            if (!scale.xKeyframes().isEmpty()) {
                pose.scaleX *= value(scale.xKeyframes(), tick, false, false);
                pose.scaleY *= value(scale.yKeyframes(), tick, false, false);
                pose.scaleZ *= value(scale.zKeyframes(), tick, false, false);
            }
        }
    }

    /**
     * One axis at {@code tick}, as GeckoLib's {@code AnimationController#getAnimationPointAtTick}: constant rotations are
     * converted to radians when loaded (x and y negated), Molang ones here.
     */
    private static double value(List<Keyframe<MathValue>> frames, double tick, boolean rotation, boolean negate) {
        Keyframe<MathValue> frame = frames.getLast();
        double frameTick = tick;
        double frameStart = 0;
        for (Keyframe<MathValue> f : frames) {
            if (frameStart + f.length() > tick) {
                frame = f;
                frameTick = tick - frameStart;
                break;
            }
            frameStart += f.length();
        }
        double start = frame.startValue().get();
        double end = frame.endValue().get();
        if (rotation) {
            if (!(frame.startValue() instanceof Constant)) {
                start = Math.toRadians(negate ? -start : start);
            }
            if (!(frame.endValue() instanceof Constant)) {
                end = Math.toRadians(negate ? -end : end);
            }
        }
        return EasingType.lerpWithOverride(new AnimationPoint(frame, frameTick, frame.length(), start, end), null);
    }
}
