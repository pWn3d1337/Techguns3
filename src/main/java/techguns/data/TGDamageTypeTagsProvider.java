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
        tag(TGDamageTypeTags.category(TGDamageCategory.POISON)).add(TGDamageTypes.POISON);
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
        tag(DamageTypeTags.BYPASSES_SHIELD).add(TGDamageTypes.RADIATION_POISONING, TGDamageTypes.RADIATION, TGDamageTypes.POISON);
        // Rapid-fire weapons must not be swallowed by the hurt cooldown.
        tag(DamageTypeTags.BYPASSES_COOLDOWN).add(
                TGDamageTypes.BULLET, TGDamageTypes.FIRE, TGDamageTypes.ENERGY, TGDamageTypes.POISON, TGDamageTypes.ICE,
                TGDamageTypes.LIGHTNING, TGDamageTypes.RADIATION, TGDamageTypes.RADIATION_POISONING, TGDamageTypes.DARK);
        tag(DamageTypeTags.NO_KNOCKBACK).add(TGDamageTypes.RADIATION, TGDamageTypes.RADIATION_POISONING, TGDamageTypes.POISON);
    }
}
