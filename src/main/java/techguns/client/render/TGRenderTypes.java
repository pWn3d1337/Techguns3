package techguns.client.render;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import techguns.TGIds;

/**
 * All custom render types and shaders (Techguns 2 / Fabric {@code TGRenderHelper}). The only place that
 * defines render state; every FX/projectile renderer draws through these with
 * {@link DefaultVertexFormat#POSITION_COLOR_TEX_LIGHTMAP} vertices (position, color, uv, light).
 */
// MIGRATION(26.x): render types become RenderPipelines (1.21.5+); shader JSON/GLSL and this class are the only places to change.
public final class TGRenderTypes {
    /**
     * Fabric "techguns_particle_alpha": texture * vertex color without alpha discard or fog, so soft additive
     * flashes don't get cut off at low alpha (vanilla particle shaders discard alpha < 0.1).
     */
    @Nullable
    private static ShaderInstance particleAlphaShader;

    private static final RenderStateShard.ShaderStateShard PARTICLE_ALPHA_SHADER = new RenderStateShard.ShaderStateShard(() -> particleAlphaShader);

    private TGRenderTypes() {
    }

    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), TGIds.id("particle_alpha"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                    shader -> particleAlphaShader = shader);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load Techguns shaders", e);
        }
    }

    /**
     * Muzzle flashes, beams, glowing FX: additive blending, custom shader, no culling, no depth write. Additive blending
     * without depth writes does not depend on the draw order, so the quads are not sorted on upload.
     */
    private static final Function<ResourceLocation, RenderType> FX_ADDITIVE = Util.memoize(texture -> RenderType.create(
            "techguns_fx_additive", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(PARTICLE_ALPHA_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, true, true))
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setOutputState(RenderStateShard.PARTICLES_TARGET)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)));

    /** Smoke and other alpha-blended FX. */
    private static final Function<ResourceLocation, RenderType> FX_ALPHA = Util.memoize(texture -> RenderType.create(
            "techguns_fx_alpha", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, true, true))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.WEATHER_TARGET)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)));

    /** Scope overlays: drawn on top of everything. */
    private static final Function<ResourceLocation, RenderType> SCOPE = Util.memoize(texture -> RenderType.create(
            "techguns_scope", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.MAIN_TARGET)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .createCompositeState(false)));

    /** Bullet tracers: additive, writes depth. */
    private static final Function<ResourceLocation, RenderType> PROJECTILE_ADDITIVE = Util.memoize(texture -> RenderType.create(
            "techguns_projectile_additive", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setOutputState(RenderStateShard.PARTICLES_TARGET)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(true)));

    /** Sprite projectiles (stone ball): opaque with alpha cutout, lit by the world light. */
    private static final Function<ResourceLocation, RenderType> PROJECTILE_CUTOUT = Util.memoize(texture -> RenderType.create(
            "techguns_projectile_cutout", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(true)));

    public static RenderType fxAdditive(ResourceLocation texture) {
        return FX_ADDITIVE.apply(texture);
    }

    public static RenderType fxAlpha(ResourceLocation texture) {
        return FX_ALPHA.apply(texture);
    }

    public static RenderType scope(ResourceLocation texture) {
        return SCOPE.apply(texture);
    }

    public static RenderType projectileAdditive(ResourceLocation texture) {
        return PROJECTILE_ADDITIVE.apply(texture);
    }

    public static RenderType projectileCutout(ResourceLocation texture) {
        return PROJECTILE_CUTOUT.apply(texture);
    }
}
