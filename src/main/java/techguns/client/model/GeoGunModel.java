package techguns.client.model;

import java.util.ArrayList;
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
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Gun model drawn from Bedrock geo files through {@link TGGeoBridge} (Q3 decided: guns use GeckoLib). Usually one geo
 * file; bones with their own texture (the rocket in the launcher, mining heads) are drawn in extra passes of the same
 * model ({@link #setBoneTypes}). A gun whose parts were merged into one geo file lists fewer geo ids than render parts,
 * and the remaining parts are not drawn. The ported Java
 * model stays as the export source ({@code /tgdebug exportgeo}) and as fallback while a geo file is missing.
 * Keyframe animations run in hand (holder known); transforms, recoil and the muzzle flash stay in the gun renderer, which
 * reads the marker bones ({@link #marker}) of the last draw.
 */
public class GeoGunModel extends TGModel {
    private final TGModel fallback;
    private final String[] geoIds;
    @Nullable
    private GeoAnimInput animInput;
    @Nullable
    private ResourceLocation partTexture;
    private Map<String, RenderType> boneTypes = Map.of();
    private Map<String, GeoMesh> meshes = Map.of();
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

    /**
     * A geo model with no Java model behind it (guns set up from data only, {@code RenderGunBase#dataDriven}): all motion
     * comes from the model files, and nothing is drawn while geo models are off or the geo file is missing.
     */
    public static GeoGunModel dataOnly(String geoId) {
        return new GeoGunModel(new NoJavaModel(), geoId).motionInFiles();
    }

    /** Whether this model has no Java model behind it ({@link #dataOnly(String)}). */
    public boolean dataOnly() {
        return fallback instanceof NoJavaModel;
    }

    /** Stands in for the Java model of a data-only gun: draws nothing, moves nothing. */
    private static final class NoJavaModel extends TGModel {
        NoJavaModel() {
            super(RenderType::entitySolid);
        }

        @Override
        public void render(@Nullable LivingEntity entity, PoseStack poseStack, VertexConsumer vertices, int ammoLeft, float reloadProgress,
                           ItemDisplayContext context, int part, float fireProgress, float chargeProgress, int light, int overlay) {
        }
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
            if (!fallback.isPartVisible(entity, part)) {
                return;
            }
            boolean inHand = entity != null && (context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
            GeoAnimInput anim = !inHand ? GeoAnimInput.still(context == ItemDisplayContext.GUI, ammoLeft) : animInput != null ? animInput
                    // without the gun renderer's input the magazine size is unknown: full or empty
                    : new GeoAnimInput(fireProgress, reloadProgress, 0f, chargeProgress, ammoLeft > 0 ? 1f : 0f, ammoLeft, ammoLeft <= 0,
                    false, 0f, 0f, null, null, null, null, false, context.firstPerson(), true, 0, false);
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
            List<String> meshBones = new ArrayList<>();
            boolean drawn = TGGeoBridge.get().render(geoIds[part], poseStack, Minecraft.getInstance().renderBuffers().bufferSource(),
                    vertices, partTexture, boneTypes, light, overlay, anim,
                    bones -> partModel(part).animateGeoBones(turned ? new TurnedBones(bones) : bones, part, ammoLeft, reloadProgress,
                            fireProgress, chargeProgress, context),
                    (name, pose) -> {
                        markers.put(name, pose);
                        if (name.startsWith(TGGeoBridge.MESH) && !name.endsWith(TGGeoBridge.REST)) {
                            meshBones.add(name);
                        }
                    });
            poseStack.popPose();
            if (drawn) {
                for (String mesh : meshBones) {
                    GeoMesh dataMesh = this.meshes.get(mesh);
                    if (dataMesh != null) {
                        dataMesh.render(meshPose(markers.get(mesh)), vertices, light, overlay);
                    } else {
                        partModel(part).renderMesh(mesh, meshPose(markers.get(mesh)), vertices, light, overlay);
                    }
                }
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

    /**
     * A pose stack at a mesh bone's absolute pose. The normal matrix is the inverse transpose scaled like
     * {@code PoseStack#scale} does, keeping the sign of a mirroring pose (the GUI's negative y scale).
     */
    private static PoseStack meshPose(Matrix4f pose) {
        PoseStack stack = new PoseStack();
        stack.last().pose().set(pose);
        Matrix3f normal = new Matrix3f(pose).normal();
        stack.last().normal().set(normal.scale(1f / (float) Math.cbrt(Math.abs(normal.determinant()))));
        return stack;
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
    public void setPartTexture(@Nullable ResourceLocation texture) {
        this.partTexture = texture;
    }

    @Override
    public void setBoneTypes(Map<String, RenderType> boneTypes) {
        this.boneTypes = boneTypes;
    }

    @Override
    public void setMeshes(Map<String, GeoMesh> meshes) {
        this.meshes = meshes;
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
