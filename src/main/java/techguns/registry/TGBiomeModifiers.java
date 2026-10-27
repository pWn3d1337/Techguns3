package techguns.registry;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import techguns.TGIds;
import techguns.Techguns;
import techguns.world.NpcSpawnsBiomeModifier;

/** Biome modifier types and the generated biome modifiers (natural NPC spawns). */
public final class TGBiomeModifiers {
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Techguns.MODID);

    public static final Supplier<MapCodec<NpcSpawnsBiomeModifier>> NPC_SPAWNS = SERIALIZERS.register("npc_spawns", () -> NpcSpawnsBiomeModifier.CODEC);

    public static final ResourceKey<BiomeModifier> OVERWORLD_NPC_SPAWNS = key("overworld_npc_spawns");
    public static final ResourceKey<BiomeModifier> NETHER_NPC_SPAWNS = key("nether_npc_spawns");

    private TGBiomeModifiers() {
    }

    private static ResourceKey<BiomeModifier> key(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, TGIds.id(name));
    }

    /** Techguns 2: all overworld biomes (neither Nether nor End) and all Nether biomes. */
    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(OVERWORLD_NPC_SPAWNS,
                new NpcSpawnsBiomeModifier(biomes.getOrThrow(Tags.Biomes.IS_OVERWORLD), NpcSpawnsBiomeModifier.Table.OVERWORLD));
        context.register(NETHER_NPC_SPAWNS, new NpcSpawnsBiomeModifier(biomes.getOrThrow(Tags.Biomes.IS_NETHER), NpcSpawnsBiomeModifier.Table.NETHER));
    }
}
