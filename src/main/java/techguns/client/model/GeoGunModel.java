package techguns.client.model;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Gun model drawn from Bedrock geo files through {@link TGGeoBridge} (Q3 decided: guns use GeckoLib). One geo file
 * per render part, because parts may use different textures (e.g. the rocket in the launcher). The ported Java model
 * stays as the export source ({@code /tgdebug exportgeo}) and as fallback while a geo file is missing.
 * Everything else (transforms, recoil, muzzle flash) stays in the gun renderer.
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

    @Override
    public void render(@Nullable LivingEntity entity, PoseStack poseStack, VertexConsumer vertices, int ammoLeft, float reloadProgress,
                       ItemDisplayContext context, int part, float fireProgress, float chargeProgress, int light, int overlay) {
        if (TGGeoBridge.enabled() && part < geoIds.length) {
            if (!visibility.isVisible(part, ammoLeft, reloadProgress)) {
                return;
            }
            boolean inHand = entity != null && (context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
            boolean left = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            long animationKey = inHand ? entity.getId() * 2L + (left ? 1 : 0) : -1L;
            if (TGGeoBridge.get().render(geoIds[part], animationKey, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(),
                    vertices, light, overlay, reloadProgress, fireProgress)) {
                return;
            }
        }
        fallback.render(entity, poseStack, vertices, ammoLeft, reloadProgress, context, part, fireProgress, chargeProgress, light, overlay);
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
