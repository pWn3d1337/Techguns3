package techguns.client.render.item;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import techguns.TGIds;
import techguns.Techguns;
import techguns.client.model.GeoGunModel;
import techguns.client.model.TGGeoBridge;
import techguns.client.render.fx.IScreenEffect;
import techguns.client.render.fx.ScreenEffects;
import techguns.gun.GunItem;

/**
 * Checks the data files of every gun and the screen effect files ({@link ScreenEffects#check}) at resource reload and logs one warning per problem, so a broken resource pack or
 * addon gun shows up in the log instead of drawing nothing: missing geo, animation, item model, texture or OBJ files,
 * display entries ({@code bone_textures}, {@code meshes}, effects) naming bones the geo file lacks, unknown screen
 * effects, and motion animations
 * neither the gun's file nor {@code common.animation.json} has. Guns drawn by data alone ({@link RenderGunBase#dataDriven})
 * are also checked for a display file and for mesh bones nothing draws; guns with a Java model behind them may leave
 * those to code.
 */
final class GunDisplayCheck {
    private static final ResourceLocation COMMON_ANIMATIONS = TGIds.id("animations/item/common.animation.json");
    /** A split part ({@code <gun>_1}) shares the gun's animation file, as in the GeckoLib renderer. */
    private static final Pattern PART_SUFFIX = Pattern.compile("_\\d+$");

    private final ResourceManager resources;
    private final Map<ResourceLocation, Optional<JsonObject>> json = new HashMap<>();
    private final List<String> problems = new ArrayList<>();

    private GunDisplayCheck(ResourceManager resources) {
        this.resources = resources;
    }

    /** Checks every gun with a renderer against {@code displays} and logs what is wrong. */
    static void run(Map<ResourceLocation, GunDisplay> displays, ResourceManager resources) {
        GunDisplayCheck check = new GunDisplayCheck(resources);
        Set<ResourceLocation> guns = new HashSet<>();
        for (Item item : TGItemRenderers.items()) {
            if (item instanceof GunItem && TGItemRenderers.get(item) instanceof RenderGunBase renderer
                    && renderer.model instanceof GeoGunModel geo) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                guns.add(id);
                check.gun(id, geo, displays.get(id));
            }
        }
        ScreenEffects.check(resources, check.problems::add);
        displays.keySet().stream().filter(id -> !guns.contains(id)).sorted()
                .forEach(id -> check.problems.add("display file " + id + " names no gun"));
        check.problems.forEach(problem -> Techguns.LOGGER.warn("Gun data check: {}", problem));
        Techguns.LOGGER.info("Gun data check: {} guns, {} display files, {} screen effects, {} problem(s)", guns.size(), displays.size(),
                ScreenEffects.count(), check.problems.size());
    }

    private void gun(ResourceLocation gun, GeoGunModel geo, @Nullable GunDisplay display) {
        boolean dataOnly = geo.dataOnly();
        Set<String> bones = new HashSet<>();
        Set<String> animations = new HashSet<>();
        for (String geoId : geo.geoIds()) {
            ResourceLocation model = resource(geoId, "geo/item/", ".geo.json");
            Optional<JsonObject> file = read(model);
            if (file.isEmpty()) {
                // a gun with a Java model draws that one instead
                if (dataOnly) {
                    problems.add(gun + ": geo file " + model + " missing, the gun draws nothing");
                }
                continue;
            }
            bones.addAll(geoBones(file.get()));
            ResourceLocation animation = resource(PART_SUFFIX.matcher(geoId).replaceFirst(""), "animations/item/", ".animation.json");
            read(animation).ifPresentOrElse(a -> animations.addAll(animationNames(a)),
                    () -> problems.add(gun + ": animation file " + animation + " missing"));
        }
        ResourceLocation itemModel = TGIds.of(gun.getNamespace(), "models/item/" + gun.getPath() + ".json");
        if (resources.getResource(itemModel).isEmpty()) {
            problems.add(gun + ": item model " + itemModel + " missing (display transforms)");
        }
        if (display == null) {
            if (dataOnly) {
                problems.add(gun + ": no display file " + TGIds.of(gun.getNamespace(), GunDisplays.DIRECTORY + "/" + gun.getPath() + ".json")
                        + ", the gun has no texture");
            }
            return;
        }
        if (bones.isEmpty()) {
            // geo files missing (reported above) or a Java-only gun: nothing to check the bones against
            animations.clear();
        }
        texture(gun, "texture", display.texture());
        for (int i = 0; i < display.parts().size(); i++) {
            part(gun, "part " + i, display.parts().get(i));
        }
        if (display.parts().size() > geo.geoIds().size()) {
            problems.add(gun + ": " + display.parts().size() + " parts but " + geo.geoIds().size() + " geo files");
        }
        display.boneTextures().forEach((bone, part) -> {
            bone(gun, bones, "bone_textures", bone);
            part(gun, "bone_textures " + bone, part);
        });
        display.meshes().forEach((bone, mesh) -> {
            if (!bone.startsWith(TGGeoBridge.MESH)) {
                problems.add(gun + ": meshes " + bone + " is not a mesh bone (" + TGGeoBridge.MESH + "*)");
            }
            bone(gun, bones, "meshes", bone);
            if (resources.getResource(mesh.model()).isEmpty()) {
                problems.add(gun + ": meshes " + bone + ": OBJ file " + mesh.model() + " missing");
            }
        });
        if (dataOnly) {
            bones.stream().filter(b -> b.startsWith(TGGeoBridge.MESH) && !display.meshes().containsKey(b)).sorted()
                    .forEach(b -> problems.add(gun + ": mesh bone " + b + " has no meshes entry, nothing is drawn there"));
        }
        display.muzzleFlash().ifPresent(e -> {
            marker(gun, bones, "muzzle_flash", TGGeoBridge.MUZZLE);
            effect(gun, "muzzle_flash", e.effect());
        });
        display.chargeEffect().ifPresent(e -> {
            marker(gun, bones, "charge_effect", "charge");
            effect(gun, "charge_effect", e.effect());
        });
        display.idleEffect().ifPresent(e -> {
            marker(gun, bones, "idle_effect", "idle_fx");
            effect(gun, "idle_effect", e.effect());
        });
        display.scope().ifPresent(s -> effect(gun, "scope", s.effect()));
        display.recoil().ifPresent(m -> motion(gun, animations, "recoil", m));
        display.reload().ifPresent(m -> motion(gun, animations, "reload", m));
        display.charge().ifPresent(m -> motion(gun, animations, "charge", m));
        display.scope().flatMap(GunDisplay.Scope::recoil).ifPresent(s -> animation(gun, animations, "scope recoil", s.animation()));
    }

    private void effect(ResourceLocation gun, String where, IScreenEffect effect) {
        if (effect instanceof ScreenEffects.Ref ref && !ref.exists()) {
            problems.add(gun + ": " + where + ": unknown screen effect " + ref.id() + " (" + ScreenEffects.DIRECTORY + "/)");
        }
    }

    private void part(ResourceLocation gun, String where, GunDisplay.Part part) {
        for (int level = 0; level < part.headLevelTextures().size(); level++) {
            texture(gun, where + " head level " + level, part.headLevelTextures().get(level));
        }
        part.ammoTextures().forEach((ammo, texture) -> texture(gun, where + " ammo " + ammo, texture));
    }

    private void texture(ResourceLocation gun, String where, ResourceLocation texture) {
        if (resources.getResource(texture).isEmpty()) {
            problems.add(gun + ": " + where + ": texture " + texture + " missing");
        }
    }

    private void bone(ResourceLocation gun, Set<String> bones, String where, String bone) {
        if (!bones.isEmpty() && !bones.contains(bone)) {
            problems.add(gun + ": " + where + " names bone " + bone + ", which the geo file lacks");
        }
    }

    /** Effects are drawn at the first bone whose name starts with the marker name. */
    private void marker(ResourceLocation gun, Set<String> bones, String where, String marker) {
        if (!bones.isEmpty() && bones.stream().noneMatch(b -> b.startsWith(marker))) {
            problems.add(gun + ": " + where + " needs a " + marker + " bone, the geo file has none");
        }
    }

    private void motion(ResourceLocation gun, Set<String> animations, String where, GunDisplay.Motion motion) {
        animation(gun, animations, where + " first_person", motion.firstPerson().animation());
        motion.aiming().ifPresent(s -> animation(gun, animations, where + " aiming", s.animation()));
        animation(gun, animations, where + " third_person", motion.thirdPerson().animation());
    }

    private void animation(ResourceLocation gun, Set<String> animations, String where, String name) {
        if (animations.contains(name)) {
            return;
        }
        if (read(COMMON_ANIMATIONS).map(GunDisplayCheck::animationNames).orElse(Set.of()).contains(name)) {
            return;
        }
        problems.add(gun + ": " + where + ": animation " + name + " is in neither the gun's animation file nor common");
    }

    private Optional<JsonObject> read(ResourceLocation location) {
        return json.computeIfAbsent(location, l -> {
            Optional<Resource> resource = resources.getResource(l);
            if (resource.isEmpty()) {
                return Optional.empty();
            }
            try (Reader reader = resource.get().openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                return element.isJsonObject() ? Optional.of(element.getAsJsonObject()) : Optional.empty();
            } catch (Exception e) {
                problems.add(l + " unreadable: " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    /** Bone names of all geometries in a Bedrock geo file. */
    private static Set<String> geoBones(JsonObject geo) {
        Set<String> names = new HashSet<>();
        if (geo.get("minecraft:geometry") instanceof com.google.gson.JsonArray geometries) {
            for (JsonElement geometry : geometries) {
                if (geometry.isJsonObject() && geometry.getAsJsonObject().get("bones") instanceof com.google.gson.JsonArray list) {
                    for (JsonElement bone : list) {
                        if (bone.isJsonObject() && bone.getAsJsonObject().has("name")) {
                            names.add(bone.getAsJsonObject().get("name").getAsString());
                        }
                    }
                }
            }
        }
        return names;
    }

    private static Set<String> animationNames(JsonObject file) {
        return file.get("animations") instanceof JsonObject animations ? animations.keySet() : Set.of();
    }

    /** As GeckoLibGunRenderer: {@code <path>} is techguns, {@code <ns>:<path>} that namespace. */
    private static ResourceLocation resource(String id, String folder, String suffix) {
        ResourceLocation base = TGIds.orTechguns(id);
        return TGIds.of(base.getNamespace(), folder + base.getPath() + suffix);
    }
}
