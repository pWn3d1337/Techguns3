package techguns.world;

import java.util.Locale;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import techguns.TGConfig;
import techguns.registry.TGBiomeModifiers;
import techguns.registry.TGEntities;

/**
 * Techguns 2 {@code TGEntities.postInit}: adds the NPC spawn marker to the monster spawns of {@code biomes} (groups of
 * 1-3), weighted by the overworld or Nether config value and skipping the config's biome blacklist. Applied when the
 * server starts, so config changes take effect on the next start.
 */
public record NpcSpawnsBiomeModifier(HolderSet<Biome> biomes, Table table) implements BiomeModifier {
    public enum Table implements StringRepresentable {
        OVERWORLD, NETHER;

        public static final Codec<Table> CODEC = StringRepresentable.fromEnum(Table::values);

        int weight() {
            return this == NETHER ? TGConfig.SPAWN_WEIGHT_NETHER.get() : TGConfig.SPAWN_WEIGHT_OVERWORLD.get();
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final MapCodec<NpcSpawnsBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(NpcSpawnsBiomeModifier::biomes),
            Table.CODEC.fieldOf("table").forGetter(NpcSpawnsBiomeModifier::table)).apply(instance, NpcSpawnsBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        int weight = table.weight();
        if (phase != Phase.ADD || weight <= 0 || !biomes.contains(biome) || isBlacklisted(biome)) {
            return;
        }
        builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,
                new MobSpawnSettings.SpawnerData(TGEntities.NPC_SPAWN_MARKER.get(), weight, 1, 3));
    }

    private static boolean isBlacklisted(Holder<Biome> biome) {
        return biome.unwrapKey().map(ResourceKey::location).map(id -> TGConfig.SPAWN_BIOME_BLACKLIST.get().contains(id.toString())).orElse(false);
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return TGBiomeModifiers.NPC_SPAWNS.get();
    }
}
