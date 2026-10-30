package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageTypes;
import techguns.core.damage.DeathType;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.GunItem;
import techguns.gun.ammo.AmmoType;
import techguns.registry.TGGuns;
import techguns.registry.TGStructures;

/**
 * Newer vanilla features Techguns 2 (1.12) did not know: damage tags of 1.20.5+, breezes deflecting projectiles and the
 * vanilla surface structures the Techguns structure sets keep away from.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGVanillaIntegrationTests {
    private static final String ARENA = "empty_8x4x8";

    private static boolean tagged(GameTestHelper helper, ResourceKey<DamageType> type, TagKey<DamageType> tag) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type).is(tag);
    }

    @GameTest(template = ARENA)
    public static void damageTypesFollowNewerVanillaTags(GameTestHelper helper) {
        Registry<DamageType> types = helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        long checked = types.holders().filter(holder -> holder.key().location().getNamespace().equals(Techguns.MODID)).peek(holder ->
                helper.assertTrue(holder.is(DamageTypeTags.PANIC_CAUSES), holder.key().location() + " should make animals panic")).count();
        helper.assertTrue(checked == 12, "All 12 Techguns damage types checked, got " + checked);
        helper.assertTrue(tagged(helper, TGDamageTypes.RADIATION, DamageTypeTags.BYPASSES_WOLF_ARMOR), "Radiation passes wolf armor");
        helper.assertTrue(!tagged(helper, TGDamageTypes.BULLET, DamageTypeTags.BYPASSES_WOLF_ARMOR), "Bullets hit wolf armor");
        helper.assertTrue(!tagged(helper, TGDamageTypes.FIRE, DamageTypeTags.IS_FIRE), "Fire stays out of is_fire (fire resistance)");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void bulletKnocksOverArmorStand(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(4, 1, 4));
        stand.hurt(TGDamage.bullet(helper.getLevel(), null, player, DeathType.DEFAULT), 5.0f);
        helper.assertTrue(stand.isRemoved(), "A bullet knocks an armor stand over like an arrow");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void breezeDeflectsBullets(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 pos = helper.absoluteVec(new Vec3(0.5, 1, 4.5));
        player.moveTo(pos.x, pos.y, pos.z, -90.0f, 0.0f);
        player.setYHeadRot(-90.0f);
        ItemStack gun = new ItemStack(TGGuns.M4.get());
        GunData.setAmmo(gun, new GunAmmo(30, AmmoType.DEFAULT));
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        Breeze breeze = helper.spawnWithNoFreeWill(EntityType.BREEZE, new BlockPos(5, 1, 4));
        float health = breeze.getHealth();

        ((GunItem) gun.getItem()).shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.assertTrue(GunData.loaded(gun) == 29, "The shot should be fired");
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(breeze.getHealth() == health, "The breeze deflects the bullet, health " + breeze.getHealth());
            helper.succeed();
        });
    }

    /**
     * The big Techguns set keeps its distance from villages that really generate: in a world of plains a village fits
     * everywhere, in mushroom fields none does (there the big set keeps every one of its positions).
     */
    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void bigStructuresKeepAwayFromVillages(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        Registry<StructureSet> sets = registries.registryOrThrow(Registries.STRUCTURE_SET);
        StructureSet big = sets.getOrThrow(TGStructures.Size.BIG.set);
        RandomSpreadStructurePlacement placement = (RandomSpreadStructurePlacement) big.placement();
        StructureSet villages = sets.getOrThrow(BuiltinStructureSets.VILLAGES);
        long seed = 1234567L;
        RandomState randomState = RandomState.create(registries.registryOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD),
                registries.lookupOrThrow(Registries.NOISE), seed);
        Holder<Biome> plains = registries.registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
        Holder<Biome> mushroom = registries.registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.MUSHROOM_FIELDS);
        ChunkGeneratorStructureState plainsWorld = ChunkGeneratorStructureState.createForNormal(randomState, seed, new FixedBiomeSource(plains),
                registries.lookupOrThrow(Registries.STRUCTURE_SET));
        ChunkGeneratorStructureState mushroomWorld = ChunkGeneratorStructureState.createForNormal(randomState, seed, new FixedBiomeSource(mushroom),
                registries.lookupOrThrow(Registries.STRUCTURE_SET));

        int cells = 12;
        int keptInMushrooms = 0;
        int keptInPlains = 0;
        int range = TGStructures.Size.BIG.radius + 5;
        for (int cx = 0; cx < cells; cx++) {
            for (int cz = 0; cz < cells; cz++) {
                ChunkPos start = placement.getPotentialStructureChunk(seed, cx * placement.spacing(), cz * placement.spacing());
                if (placement.isStructureChunk(mushroomWorld, start.x, start.z)) {
                    keptInMushrooms++;
                }
                if (placement.isStructureChunk(plainsWorld, start.x, start.z)) {
                    keptInPlains++;
                    for (int x = start.x - range; x <= start.x + range; x++) {
                        for (int z = start.z - range; z <= start.z + range; z++) {
                            if (villages.placement().isStructureChunk(plainsWorld, x, z)) {
                                helper.fail("Big Techguns structure at " + start + " next to a village at " + x + ", " + z);
                            }
                        }
                    }
                }
            }
        }
        helper.assertTrue(keptInMushrooms == cells * cells, "No village fits mushroom fields: all positions stay, kept " + keptInMushrooms);
        helper.assertTrue(keptInPlains > 0 && keptInPlains < cells * cells, "Plains lose the positions next to villages, kept " + keptInPlains);
        helper.succeed();
    }
}
