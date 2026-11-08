package techguns.client.render.fx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import techguns.TGIds;
import techguns.Techguns;

/**
 * The screen effects of the resource packs ({@code assets/<namespace>/techguns/screen_effect/<name>.json}), named by
 * file id: {@code techguns:rifle} or just {@code rifle} in the techguns namespace. A file has a {@code type}:
 * {@code sprite} ({@link ScreenEffect}), {@code layers} ({@link MultiScreenEffect}) or {@code by_ammo}
 * ({@link VariableScreenEffect}); where an effect goes inside another, a name or an inline effect may be given.
 * <p>
 * Gun display files and code hold {@link Ref references}, looked up at draw time, so effect and display files load in
 * any order and a reload replaces the effects everywhere. An unknown name draws nothing; the gun data check reports it.
 */
public final class ScreenEffects {
    public static final String DIRECTORY = "techguns/screen_effect";
    private static final Gson GSON = new Gson();
    private static Map<ResourceLocation, IScreenEffect> effects = Map.of();

    /** A named effect, either a {@link Ref} (string) or an inline effect (object). */
    private static final Codec<IScreenEffect> EFFECT = Codec.recursive("screen_effect", self -> {
        Codec<IScreenEffect> inline = Codec.STRING.partialDispatch("type", e -> DataResult.success(typeOf(e)), type -> mapCodec(type, self));
        return Codec.either(Ref.CODEC, inline).xmap(e -> e.map(r -> r, i -> i),
                e -> e instanceof Ref ref ? Either.left(ref) : Either.right(e));
    });

    /** A screen effect by name (gun display files). */
    public static final Codec<IScreenEffect> CODEC = Ref.CODEC.xmap(r -> r, e -> (Ref) e);

    private ScreenEffects() {
    }

    private static String typeOf(IScreenEffect effect) {
        return switch (effect) {
            case ScreenEffect e -> "sprite";
            case MultiScreenEffect e -> "layers";
            case VariableScreenEffect e -> "by_ammo";
            default -> throw new IllegalArgumentException("Screen effect without a type: " + effect);
        };
    }

    @SuppressWarnings("unchecked")
    private static DataResult<MapCodec<IScreenEffect>> mapCodec(String type, Codec<IScreenEffect> effect) {
        MapCodec<? extends IScreenEffect> codec = switch (type) {
            case "sprite" -> ScreenEffect.CODEC;
            case "layers" -> MultiScreenEffect.codec(effect);
            case "by_ammo" -> VariableScreenEffect.codec(effect);
            default -> null;
        };
        // each codec only encodes effects of its own type (typeOf picked it)
        return codec != null ? DataResult.success((MapCodec<IScreenEffect>) codec) : DataResult.error(() -> "Unknown screen effect type: " + type);
    }

    /** Lower-case enum names ({@code additive}, {@code rise_smooth}). */
    static <E extends Enum<E>> Codec<E> enumCodec(Class<E> type) {
        return Codec.STRING.comapFlatMap(name -> {
            try {
                return DataResult.success(Enum.valueOf(type, name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                return DataResult.error(() -> "Unknown " + type.getSimpleName() + ": " + name);
            }
        }, value -> value.name().toLowerCase(Locale.ROOT));
    }

    /** A reference to effect {@code id} ({@code <name>} in the techguns namespace or {@code <namespace>:<name>}). */
    public static Ref ref(String id) {
        return new Ref(TGIds.orTechguns(id));
    }

    /** The number of loaded effects. */
    public static int count() {
        return effects.size();
    }

    @Nullable
    public static IScreenEffect get(ResourceLocation id) {
        return effects.get(id);
    }

    /**
     * Loads the effect files of {@code resources}; called by the gun display loader before it loads the display files.
     * Broken files are logged and skipped.
     */
    public static void reload(ResourceManager resources) {
        Map<ResourceLocation, JsonElement> files = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(resources, DIRECTORY, GSON, files);
        Map<ResourceLocation, IScreenEffect> result = new HashMap<>();
        files.forEach((id, json) -> EFFECT.parse(JsonOps.INSTANCE, json)
                .ifSuccess(effect -> result.put(id, effect))
                .ifError(error -> Techguns.LOGGER.warn("Screen effect {} skipped: {}", id, error.message())));
        effects = Map.copyOf(result);
    }

    /** Reports missing textures, unknown names and reference cycles of the loaded effects. */
    public static void check(ResourceManager resources, Consumer<String> problems) {
        effects.keySet().stream().sorted().forEach(id -> walk(id, effects.get(id), resources, problems, new HashSet<>(List.of(id))));
    }

    /**
     * Checks {@code effect} inside effect file {@code owner}; {@code path} holds the files on the way (cycles). The
     * effects behind a reference are followed for cycles only ({@code resources} null): their textures are reported
     * for their own file.
     */
    private static void walk(ResourceLocation owner, IScreenEffect effect, @Nullable ResourceManager resources, Consumer<String> problems,
                             Set<ResourceLocation> path) {
        switch (effect) {
            case Ref ref -> {
                IScreenEffect target = effects.get(ref.id());
                if (target == null) {
                    if (resources != null) {
                        problems.accept("screen effect " + owner + " uses unknown effect " + ref.id());
                    }
                } else if (!path.add(ref.id())) {
                    problems.accept("screen effect " + owner + " refers to itself through " + ref.id());
                } else {
                    walk(owner, target, null, problems, path);
                    path.remove(ref.id());
                }
            }
            case ScreenEffect sprite -> {
                if (resources != null && resources.getResource(sprite.texture()).isEmpty()) {
                    problems.accept("screen effect " + owner + ": texture " + sprite.texture() + " missing");
                }
            }
            case MultiScreenEffect layers -> layers.layers().forEach(l -> walk(owner, l.effect(), resources, problems, path));
            case VariableScreenEffect byAmmo -> {
                byAmmo.ammo().values().forEach(e -> walk(owner, e, resources, problems, path));
                byAmmo.fallback().ifPresent(e -> walk(owner, e, resources, problems, path));
            }
            default -> {
            }
        }
    }

    /**
     * A screen effect by name, looked up at draw time; draws nothing while no such effect is loaded, or deeper than
     * {@link #MAX_DEPTH} references (a cycle between effect files).
     */
    public record Ref(ResourceLocation id) implements IScreenEffect {
        private static final int MAX_DEPTH = 8;
        private static int depth;

        static final Codec<Ref> CODEC = Codec.STRING.comapFlatMap(id -> {
                    try {
                        return DataResult.success(ref(id));
                    } catch (RuntimeException e) {
                        return DataResult.error(() -> "Invalid screen effect name " + id + ": " + e.getMessage());
                    }
                },
                ref -> ref.id.getNamespace().equals(Techguns.MODID) ? ref.id.getPath() : ref.id.toString());

        /** Whether the effect is loaded. */
        public boolean exists() {
            return effects.containsKey(id);
        }

        @Override
        public void doRender(PoseStack poseStack, MultiBufferSource buffers, float progress, float offsetX, float offsetY, float offsetZ,
                             float scale, float rotX, float rotY, float rotZ, boolean is3p, String ammoVariant) {
            IScreenEffect effect = effects.get(id);
            if (effect == null || depth >= MAX_DEPTH) {
                return;
            }
            depth++;
            try {
                effect.doRender(poseStack, buffers, progress, offsetX, offsetY, offsetZ, scale, rotX, rotY, rotZ, is3p, ammoVariant);
            } finally {
                depth--;
            }
        }
    }
}
