package techguns.client.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Gun model drawn from Bedrock geo files through {@link TGGeoBridge} (Q3 decided: guns use GeckoLib). One geo file
 * per render part, because parts may use different textures (e.g. the rocket in the launcher); a gun whose parts were
 * merged into one geo file lists fewer geo ids than render parts, and the remaining parts are not drawn. The ported Java
 * model stays as the export source ({@code /tgdebug exportgeo}) and as fallback while a geo file is missing.
 * Keyframe animations run in hand (holder known); transforms, recoil and the muzzle flash stay in the gun renderer, which
 * reads the marker bones ({@link #marker}) of the last draw.
 */
public class GeoGunModel extends TGModel {
    /** Whether a render part is drawn, e.g. the loaded rocket. */
    @FunctionalInterface
    public interface PartVisibility {
        boolean isVisible(int part, int ammoLeft, float reloadProgress);
    }

    private final TGModel fallback;
    private final String[] geoIds;
    private PartVisibility visibility = (part, ammoLeft, reloadProgress) -> true;
    @Nullable
    private GeoAnimInput animInput;
    /** Marker bone poses of the last draw (all parts). */
    private final Map<String, Matrix4f> markers = new HashMap<>();
    /** See {@link #turned()}. */
    private boolean turned;
    /** See {@link #motionInFiles()}. */
    private boolean motionInFiles;
    private static final Quaternionf JAVA_TURN = Axis.YP.rotationDegrees(90f);
    private static final Quaternionf JAVA_TURN_BACK = Axis.YP.rotationDegrees(-90f);

    /**
     * @param geoIds geo model id per render part ({@code assets/techguns/geo/item/<id>.geo.json})
     */
    public GeoGunModel(TGModel fallback, String... geoIds) {
        super(RenderType::entitySolid);
        this.fallback = fallback;
        this.geoIds = geoIds;
    }

    public GeoGunModel setVisibility(PartVisibility visibility) {
        this.visibility = visibility;
        return this;
    }

    /**
     * The geo files are turned 90 degrees about Y against the Java model ({@code tools/geo_turn.py}, the guns of
     * {@code RenderGunBase90}): Java model space = {@link #JAVA_TURN} x geo frame. The Java part transforms, code bone
     * offsets and scales and the Java fallback are turned to match.
     */
    public GeoGunModel turned() {
        this.turned = true;
        return this;
    }

    /**
     * The geo files and animations hold all part motion (docs/gun_animation_plan.md, phase 5): the Java model's
     * {@code transformPart} is only used by the Java fallback, not for the geo draw.
     */
    public GeoGunModel motionInFiles() {
        this.motionInFiles = true;
        return this;
    }

    /** Code bones of a turned model: Java offsets and scales turned by Ry(90), (x, y, z) -> (z, y, -x). */
    private record TurnedBones(GeoBones bones) implements GeoBones {
        @Override
        public void setHidden(String bone, boolean hidden) {
            bones.setHidden(bone, hidden);
        }

        @Override
        public void setOffset(String bone, float x, float y, float z) {
            bones.setOffset(bone, z, y, -x);
        }

        @Override
        public void setScale(String bone, float x, float y, float z) {
            bones.setScale(bone, z, y, x);
        }
    }

    @Override
    public void render(@Nullable LivingEntity entity, PoseStack poseStack, VertexConsumer vertices, int ammoLeft, float reloadProgress,
                       ItemDisplayContext context, int part, float fireProgress, float chargeProgress, int light, int overlay) {
        if (part == 0) {
            markers.clear();
        }
        if (TGGeoBridge.enabled()) {
            if (part >= geoIds.length) {
                return;
            }
            if (!visibility.isVisible(part, ammoLeft, reloadProgress) || !fallback.isPartVisible(entity, part)) {
                return;
            }
            boolean inHand = entity != null && (context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
            GeoAnimInput anim = !inHand ? GeoAnimInput.still(context == ItemDisplayContext.GUI, ammoLeft) : animInput != null ? animInput
                    // without the gun renderer's input the magazine size is unknown: full or empty
                    : new GeoAnimInput(fireProgress, reloadProgress, 0f, chargeProgress, ammoLeft > 0 ? 1f : 0f, ammoLeft, ammoLeft <= 0,
                    false, 0f, 0f, null, null, null, null, false, context.firstPerson(), true, 0);
            poseStack.pushPose();
            if (turned) {
                poseStack.mulPose(JAVA_TURN);
            }
            if (!motionInFiles) {
                fallback.transformPart(poseStack, entity, context, part, ammoLeft, reloadProgress, fireProgress);
            }
            if (turned) {
                poseStack.mulPose(JAVA_TURN_BACK);
            }
            boolean drawn = TGGeoBridge.get().render(geoIds[part], poseStack, Minecraft.getInstance().renderBuffers().bufferSource(),
                    vertices, light, overlay, anim,
                    bones -> partModel(part).animateGeoBones(turned ? new TurnedBones(bones) : bones, part, ammoLeft, reloadProgress,
                            fireProgress, chargeProgress, context),
                    markers::put);
            poseStack.popPose();
            if (drawn) {
                return;
            }
            if (turned) {
                // geo file missing: the renderer placed the geo frame
                poseStack.pushPose();
                poseStack.mulPose(JAVA_TURN);
                fallback.render(entity, poseStack, vertices, ammoLeft, reloadProgress, context, part, fireProgress, chargeProgress, light, overlay);
                poseStack.popPose();
                return;
            }
        }
        fallback.render(entity, poseStack, vertices, ammoLeft, reloadProgress, context, part, fireProgress, chargeProgress, light, overlay);
    }

    /** The geo model id of each render part. */
    public List<String> geoIds() {
        return List.of(geoIds);
    }

    @Override
    public void setAnimInput(@Nullable GeoAnimInput input) {
        this.animInput = input;
    }

    @Override
    @Nullable
    public Matrix4f marker(String name) {
        return markers.get(name);
    }

    /** The Java model a geo part was exported from (part models are nested in the gun model, see TGGeoExporter). */
    @Override
    public TGModel partModel(int part) {
        return part == 0 ? fallback : fallback.partModel(part);
    }

    @Override
    public RenderType getRenderType(ResourceLocation texture, int part) {
        return fallback.getRenderType(texture, part);
    }

    @Override
    public RenderType getRenderType(ResourceLocation texture, int part, String ammoVariant) {
        return fallback.getRenderType(texture, part, ammoVariant);
    }
}
