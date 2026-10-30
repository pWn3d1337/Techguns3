package techguns.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import techguns.Techguns;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGDamageTypeTags;
import techguns.damage.TGDamageTypes;

public class TGDamageTypeTagsProvider extends TagsProvider<DamageType> {
    public TGDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, Techguns.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Techguns categories
        tag(TGDamageTypeTags.category(TGDamageCategory.PHYSICAL)).add(TGDamageTypes.PHYSICAL);
        tag(TGDamageTypeTags.category(TGDamageCategory.PROJECTILE)).add(TGDamageTypes.BULLET);
        tag(TGDamageTypeTags.category(TGDamageCategory.EXPLOSION)).add(TGDamageTypes.EXPLOSION);
        tag(TGDamageTypeTags.category(TGDamageCategory.FIRE)).add(TGDamageTypes.FIRE);
        tag(TGDamageTypeTags.category(TGDamageCategory.ENERGY)).add(TGDamageTypes.ENERGY);
        tag(TGDamageTypeTags.category(TGDamageCategory.POISON)).add(TGDamageTypes.POISON, TGDamageTypes.ACID);
        tag(TGDamageTypeTags.category(TGDamageCategory.ICE)).add(TGDamageTypes.ICE);
        tag(TGDamageTypeTags.category(TGDamageCategory.LIGHTNING)).add(TGDamageTypes.LIGHTNING);
        tag(TGDamageTypeTags.category(TGDamageCategory.RADIATION)).add(TGDamageTypes.RADIATION);
        tag(TGDamageTypeTags.category(TGDamageCategory.DARK)).add(TGDamageTypes.DARK);
        tag(TGDamageTypeTags.category(TGDamageCategory.UNRESISTABLE)).add(TGDamageTypes.RADIATION_POISONING);

        // Vanilla behaviour
        tag(DamageTypeTags.IS_PROJECTILE).add(TGDamageTypes.BULLET);
        tag(DamageTypeTags.IS_EXPLOSION).add(TGDamageTypes.EXPLOSION);
        tag(DamageTypeTags.IS_LIGHTNING).add(TGDamageTypes.LIGHTNING);
        tag(DamageTypeTags.IS_FREEZING).add(TGDamageTypes.ICE);
        // Not is_fire on purpose: fire resistance would make entities immune to flamethrowers.
        tag(DamageTypeTags.BYPASSES_ARMOR).add(TGDamageTypes.RADIATION_POISONING);
        tag(DamageTypeTags.BYPASSES_SHIELD).add(TGDamageTypes.RADIATION_POISONING, TGDamageTypes.RADIATION, TGDamageTypes.POISON, TGDamageTypes.ACID);
        // Rapid-fire weapons must not be swallowed by the hurt cooldown (acid keeps it, as in Techguns 2).
        tag(DamageTypeTags.BYPASSES_COOLDOWN).add(
                TGDamageTypes.BULLET, TGDamageTypes.FIRE, TGDamageTypes.ENERGY, TGDamageTypes.POISON, TGDamageTypes.ICE,
                TGDamageTypes.LIGHTNING, TGDamageTypes.RADIATION, TGDamageTypes.RADIATION_POISONING, TGDamageTypes.DARK);
        // poison knocks back like Fabric's bio gun; acid and bio blobs have no direct entity, so they push nothing
        tag(DamageTypeTags.NO_KNOCKBACK).add(TGDamageTypes.RADIATION, TGDamageTypes.RADIATION_POISONING, TGDamageTypes.ACID);

        // Newer vanilla tags (1.20.5+). Animals panic only from listed types: hurt by guns they flee like in Techguns 2.
        tag(DamageTypeTags.PANIC_CAUSES).add(
                TGDamageTypes.PHYSICAL, TGDamageTypes.BULLET, TGDamageTypes.EXPLOSION, TGDamageTypes.FIRE, TGDamageTypes.ENERGY,
                TGDamageTypes.POISON, TGDamageTypes.ACID, TGDamageTypes.ICE, TGDamageTypes.LIGHTNING, TGDamageTypes.RADIATION,
                TGDamageTypes.RADIATION_POISONING, TGDamageTypes.DARK);
        // shots knock armor stands over like arrows; melee tools break them like a punch
        tag(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS).add(TGDamageTypes.BULLET, TGDamageTypes.ENERGY);
        tag(DamageTypeTags.CAN_BREAK_ARMOR_STAND).add(TGDamageTypes.PHYSICAL);
        // wolf armor soaks up attacks, but not sickness (vanilla lets magic and wither through)
        tag(DamageTypeTags.BYPASSES_WOLF_ARMOR).add(TGDamageTypes.RADIATION, TGDamageTypes.RADIATION_POISONING);
    }
}
