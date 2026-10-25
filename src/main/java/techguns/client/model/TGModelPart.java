package techguns.client.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import techguns.client.render.TGMatrixOps;

/**
 * Techguns 2 style box part (Tabula/Techne exports): pivot in 1/16 units, rotation in radians, optional children.
 * Wraps one vanilla {@link ModelPart.Cube}.
 */
public class TGModelPart {
    /** 1.0/16.0 */
    protected static final double SCALE = 0.0625;
    protected static final float RAD2DEG = 180.0F / (float) Math.PI;

    TGModel parent;
    float x;
    float y;
    float z;
    float width;
    float height;
    float length;

    float expansionX = 0.0f;
    float expansionY = 0.0f;
    float expansionZ = 0.0f;

    public float pivotX;
    public float pivotY;
    public float pivotZ;

    int textureWidth;
    int textureHeight;

    public boolean mirror;

    public float pitch;
    public float yaw;
    public float roll;

    int u;
    int v;

    /** Static scale of this part (Tabula exports scaled parts in render code); 1 = none. */
    float scaleX = 1.0f;
    float scaleY = 1.0f;
    float scaleZ = 1.0f;

    private ModelPart.Cube cuboid = null;

    private final List<TGModelPart> children = new ArrayList<>();

    public TGModelPart(TGModel parent, int u, int v) {
        this.parent = parent;
        this.textureWidth = parent.textureWidth;
        this.textureHeight = parent.textureHeight;
        this.u = u;
        this.v = v;
    }

    public void addCuboid(float x, float y, float z, float w, float h, float l) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = w;
        this.height = h;
        this.length = l;
    }

    public void addCuboid(float x, float y, float z, float w, float h, float l, float expansionX, float expansionY, float expansionZ) {
        this.addCuboid(x, y, z, w, h, l);
        this.expansionX = expansionX;
        this.expansionY = expansionY;
        this.expansionZ = expansionZ;
    }

    public void addCuboid(float x, float y, float z, float w, float h, float l, float scale) {
        this.addCuboid(x, y, z, w, h, l);
    }

    public void setPivot(float px, float py, float pz) {
        this.pivotX = px;
        this.pivotY = py;
        this.pivotZ = pz;
    }

    /** Used by some Tabula exports. */
    public TGModelPart setTextureOffset(float u, float v) {
        this.u += u;
        this.v += v;
        return this;
    }

    public void setTextureSize(int texW, int texH) {
        this.textureWidth = texW;
        this.textureHeight = texH;
    }

    public void render(PoseStack poseStack, VertexConsumer vertices, int light, int overlay) {
        if (!hasRenderScale()) {
            this.render(poseStack, vertices, light, overlay, 0f, 0f, 0f);
            return;
        }
        // scaled around the Tabula scale origin, see scaleOrigin()
        poseStack.pushPose();
        poseStack.translate(scaleOriginX() * SCALE, scaleOriginY() * SCALE, scaleOriginZ() * SCALE);
        poseStack.scale(scaleX, scaleY, scaleZ);
        poseStack.translate(-scaleOriginX() * SCALE, -scaleOriginY() * SCALE, -scaleOriginZ() * SCALE);
        this.render(poseStack, vertices, light, overlay, 0f, 0f, 0f);
        poseStack.popPose();
    }

    /**
     * Scales this part (Tabula "scale" export, formerly written out as pose stack code in each model). The scale must
     * commute with the part's rotation (uniform, no rotation, or rotation about the axis whose two other scales are
     * equal) so the geo model can reproduce it with a bone scale.
     */
    public TGModelPart setRenderScale(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
        return this;
    }

    public boolean hasRenderScale() {
        return scaleX != 1.0f || scaleY != 1.0f || scaleZ != 1.0f;
    }

    // Tabula scales around pivot + rotation (radians read as pixels); kept as is so the models look unchanged
    float scaleOriginX() {
        return pivotX + pitch;
    }

    float scaleOriginY() {
        return pivotY + yaw;
    }

    float scaleOriginZ() {
        return pivotZ + roll;
    }

    public void render(PoseStack poseStack, VertexConsumer vertices, int light, int overlay, float rx, float ry, float rz) {
        if (cuboid == null) {
            this.cuboid = new ModelPart.Cube(u, v, x, y, z, width, height, length, 0, 0, 0, mirror, textureWidth, textureHeight, EnumSet.allOf(Direction.class));
        }
        poseStack.pushPose();

        poseStack.translate(pivotX * SCALE, pivotY * SCALE, pivotZ * SCALE);

        poseStack.pushPose();
        if (this.pitch != 0.0 || rx != 0.0f) {
            TGMatrixOps.rotate(poseStack, this.pitch * RAD2DEG + rx, 1F, 0F, 0F);
        }
        if (this.yaw != 0.0 || ry != 0.0f) {
            TGMatrixOps.rotate(poseStack, this.yaw * RAD2DEG + ry, 0F, 1F, 0F);
        }
        if (this.roll != 0.0 || rz != 0.0f) {
            TGMatrixOps.rotate(poseStack, this.roll * RAD2DEG + rz, 0F, 0F, 1F);
        }

        // Expansion
        if (this.expansionX != 0 || this.expansionY != 0.0f || this.expansionZ != 0.0f) {
            float scaleX = (this.width + (this.expansionX * 2.0f)) / this.width;
            float scaleY = (this.height + (this.expansionY * 2.0f)) / this.height;
            float scaleZ = (this.length + (this.expansionZ * 2.0f)) / this.length;
            poseStack.scale(scaleX, scaleY, scaleZ);
        }

        this.cuboid.compile(poseStack.last(), vertices, light, overlay, -1);
        poseStack.popPose();
        for (TGModelPart child : this.children) {
            child.render(poseStack, vertices, light, overlay, rx, ry, rz);
        }
        poseStack.popPose();
    }

    public void addChild(TGModelPart child) {
        this.children.add(child);
    }

    List<TGModelPart> children() {
        return children;
    }
}
