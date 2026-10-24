package techguns.client.particle;

import java.util.Comparator;
import java.util.Iterator;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import techguns.client.particle.ITGParticle.BillboardAxes;
import techguns.client.particle.ParticleList.ParticleListIterator;

/**
 * Owns, ticks and renders all Techguns particles (the Fabric version was {@code ClientProxy.particleManager}).
 * Particles are sorted back to front with a few bubble sort passes per tick.
 */
public class TGParticleManager {
    private static final TGParticleManager INSTANCE = new TGParticleManager();

    /** Bubble sort passes per tick (Fabric config {@code cl_sortPassesPerTick}, default 10). */
    private static final int SORT_PASSES_PER_TICK = 10;

    protected ParticleList<TGParticleSystem> listSystems = new ParticleList<>();
    protected ParticleList<ITGParticle> list = new ParticleList<>();
    protected ParticleList<ITGParticle> listNoSort = new ParticleList<>();
    protected ComparatorParticleDepth compare = new ComparatorParticleDepth();

    public static TGParticleManager get() {
        return INSTANCE;
    }

    public void addEffect(ITGParticle effect) {
        if (effect == null) {
            return;
        }
        if (effect instanceof TGParticleSystem system) {
            listSystems.add(system);
        } else if (effect.doNotSort()) {
            listNoSort.add(effect);
        } else {
            list.add(effect);
        }
    }

    /** Dev testing only (/tgdebug fxstats). */
    public String debugStats() {
        return "systems=" + listSystems.getSize() + " particles=" + list.getSize() + " unsorted=" + listNoSort.getSize();
    }

    public void clear() {
        this.listSystems.clear();
        this.listNoSort.clear();
        this.list.clear();
    }

    public void tickParticles() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) {
            return;
        }

        Entity viewEnt = mc.getCameraEntity();

        Iterator<TGParticleSystem> sysIt = listSystems.iterator();
        while (sysIt.hasNext()) {
            TGParticleSystem p = sysIt.next();

            p.updateTick();
            if (p.shouldRemove()) {
                sysIt.remove();
            }
        }

        ParticleListIterator<ITGParticle> it = list.iterator();
        while (it.hasNext()) {
            ITGParticle p = it.next();

            p.updateTick();
            if (p.shouldRemove()) {
                it.remove();
            } else if (viewEnt != null) {
                p.setDepth(this.distanceToPlane(viewEnt, p.getPos()));
            }
        }

        Iterator<ITGParticle> it2 = listNoSort.iterator();
        while (it2.hasNext()) {
            ITGParticle p = it2.next();

            p.updateTick();
            if (p.shouldRemove()) {
                it2.remove();
            }
        }

        this.list.doBubbleSort(SORT_PASSES_PER_TICK, compare);
    }

    /**
     * Draws all particles. The buffer source is flushed at the end; consecutive particles of the same render type
     * share a batch, so the depth sorted order is kept.
     */
    public void renderParticles(MultiBufferSource.BufferSource buffers, Camera camera, float partialTick, PoseStack poseStack) {
        float yawRad = camera.getYRot() * 0.017453292F;
        float pitchRad = camera.getXRot() * 0.017453292F;
        float f1 = Mth.cos(yawRad);
        float f2 = Mth.sin(yawRad);
        float f3 = -f2 * Mth.sin(pitchRad);
        float f4 = f1 * Mth.sin(pitchRad);
        float f5 = Mth.cos(pitchRad);
        BillboardAxes axes = new BillboardAxes(f1, f5, f2, f3, f4);

        for (ITGParticle p : this.list) {
            p.doRender(buffers, camera, partialTick, axes, poseStack);
        }
        for (ITGParticle p : this.listNoSort) {
            p.doRender(buffers, camera, partialTick, axes, poseStack);
        }
        buffers.endLastBatch();
    }

    /** Squared distance of a point to the view plane's normal axis (Techguns 2 depth value used for sorting). */
    public double distanceToPlane(Entity viewEntity, Vec3 pos) {
        // Formula from: http://geomalgorithms.com/a04-_planes.html
        Vec3 n = viewEntity.getLookAngle();
        double dot1 = -n.dot(pos.subtract(viewEntity.position()));
        double dot2 = n.dot(n);
        double f = dot1 / dot2;

        Vec3 pos2 = pos.add(n.scale(f));
        return pos.distanceToSqr(pos2);
    }

    /** Sorts far particles first. */
    public static class ComparatorParticleDepth implements Comparator<ITGParticle> {
        @Override
        public int compare(ITGParticle p1, ITGParticle p2) {
            if (p1.doNotSort() && p2.doNotSort()) {
                return 0;
            }
            double dist1 = p1.getDepth();
            double dist2 = p2.getDepth();

            if (dist1 < dist2) {
                return 1;
            } else if (dist1 > dist2) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
