package techguns;

import net.minecraft.resources.ResourceLocation;

/**
 * Single place to create identifiers. Never construct {@link ResourceLocation}s elsewhere.
 */
// MIGRATION(26.x): ResourceLocation is renamed Identifier in newer Mojang mappings; only this class should need changes.
public final class TGIds {
    private TGIds() {
    }

    /** Identifier in the techguns namespace. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Techguns.MODID, path);
    }

    /** Identifier in the minecraft namespace. */
    public static ResourceLocation vanilla(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    /** Identifier in any namespace. */
    public static ResourceLocation of(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
