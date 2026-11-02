package techguns.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import techguns.Techguns;
import techguns.registry.TGNpcs;
import techguns.registry.TGTags;

/**
 * Entity type tags: the Techguns 2 {@code GenericNPCUndead} NPCs are undead (smite, healing/harming potions), the
 * alien bug is an arthropod (bane of arthropods), creatures with flesh or bones are gored.
 */
public class TGEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public TGEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper fileHelper) {
        super(output, lookup, Techguns.MODID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (TGNpcs.Npc npc : TGNpcs.undead()) {
            tag(EntityTypeTags.UNDEAD).add(npc.type().get());
            tag(EntityTypeTags.SENSITIVE_TO_SMITE).add(npc.type().get());
        }
        // Techguns 2 AlienBug: EnumCreatureAttribute.ARTHROPOD
        tag(EntityTypeTags.ARTHROPOD).add(TGNpcs.ALIEN_BUG.type().get());
        tag(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS).add(TGNpcs.ALIEN_BUG.type().get());
        // Techguns 2 EntityDeathUtils.goreMap plus the newer vanilla creatures; machines and constructs (attack helicopter,
        // turret, golems, blaze, breeze, wither, ender dragon, armor stand) and other mods' entities are left out
        for (TGNpcs.Npc npc : TGNpcs.npcs()) {
            if (npc != TGNpcs.ATTACK_HELICOPTER) {
                tag(TGTags.EntityTypes.GORE).add(npc.type().get());
            }
        }
        tag(TGTags.EntityTypes.GORE).add(
                // humans and illagers
                EntityType.PLAYER, EntityType.VILLAGER, EntityType.WANDERING_TRADER, EntityType.WITCH, EntityType.EVOKER, EntityType.VINDICATOR,
                EntityType.PILLAGER, EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX,
                // undead
                EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER, EntityType.GIANT, EntityType.ZOMBIFIED_PIGLIN,
                EntityType.SKELETON, EntityType.STRAY, EntityType.BOGGED, EntityType.WITHER_SKELETON, EntityType.ZOMBIE_HORSE,
                EntityType.SKELETON_HORSE, EntityType.ZOGLIN, EntityType.PHANTOM,
                // other monsters
                EntityType.CREEPER, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.SILVERFISH,
                EntityType.SLIME, EntityType.MAGMA_CUBE, EntityType.GHAST, EntityType.SHULKER, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN,
                EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.HOGLIN, EntityType.WARDEN,
                // animals
                EntityType.COW, EntityType.MOOSHROOM, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.HORSE, EntityType.DONKEY,
                EntityType.MULE, EntityType.LLAMA, EntityType.TRADER_LLAMA, EntityType.CAMEL, EntityType.WOLF, EntityType.CAT, EntityType.OCELOT,
                EntityType.FOX, EntityType.RABBIT, EntityType.PARROT, EntityType.BAT, EntityType.POLAR_BEAR, EntityType.PANDA, EntityType.GOAT,
                EntityType.BEE, EntityType.TURTLE, EntityType.STRIDER, EntityType.SNIFFER, EntityType.ARMADILLO, EntityType.FROG, EntityType.TADPOLE,
                EntityType.AXOLOTL, EntityType.ALLAY,
                // water creatures
                EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.DOLPHIN, EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH,
                EntityType.TROPICAL_FISH);
    }
}
