package techguns.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import techguns.Techguns;
import techguns.registry.TGNpcs;

/** Entity type tags: the Techguns 2 {@code GenericNPCUndead} NPCs are undead (smite, healing/harming potions). */
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
    }
}
