package techguns.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;
import techguns.TGIds;
import techguns.Techguns;
import techguns.slot.TGSlotType;
import techguns.registry.TGAmmo;
import techguns.registry.TGArmors;
import techguns.registry.TGItems;
import techguns.registry.TGSlotItems;
import techguns.registry.TGTags;

public class TGItemTagsProvider extends ItemTagsProvider {
    public TGItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Techguns.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // everything an Ammo Bag accepts: all ammo items, full and empty
        TGAmmo.flatAmmo().forEach(item -> tag(TGTags.Items.AMMO).add(item.get()));
        TGAmmo.modelAmmo().forEach(item -> tag(TGTags.Items.AMMO).add(item.get()));
        // Ammo Bench inputs (Fabric tags)
        tag(TGTags.Items.BULLET_CORE).addTag(TGTags.Items.ingot("steel")).addTag(TGTags.Items.ingot("lead"));
        tag(TGTags.Items.BULLET_CASING).addTag(Tags.Items.INGOTS_COPPER).addTag(Tags.Items.INGOTS_IRON)
                .addTag(TGTags.Items.ingot("tin")).addTag(TGTags.Items.ingot("bronze"));
        tag(TGTags.Items.BULLET_POWDER).addTag(Tags.Items.GUNPOWDERS);
        // Techguns armor takes armor enchantments like Techguns 2 GenericArmor (books, Upgrade Bench)
        TGArmors.armors().forEach(item -> tag(switch (item.get().getType()) {
            case HELMET -> ItemTags.HEAD_ARMOR_ENCHANTABLE;
            case CHESTPLATE -> ItemTags.CHEST_ARMOR_ENCHANTABLE;
            case LEGGINGS -> ItemTags.LEG_ARMOR_ENCHANTABLE;
            default -> ItemTags.FOOT_ARMOR_ENCHANTABLE;
        }).add(item.get()));
        // Curios slots of the extra slot gear (head = face items)
        TGSlotItems.items().forEach(item -> tag(curiosSlot(item.get().slot())).add(item.get()));
        // Ore blocks: mirror the block tags
        for (String material : new String[] {"tin", "lead", "titanium", "uranium"}) {
            copy(TGTags.Blocks.ore(material), TGTags.Items.ore(material));
        }
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(Tags.Blocks.ORES_IN_GROUND_STONE, Tags.Items.ORES_IN_GROUND_STONE);
        copy(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Tags.Items.ORES_IN_GROUND_DEEPSLATE);

        // Raw materials
        rawMaterial("tin", TGItems.RAW_TIN);
        rawMaterial("lead", TGItems.RAW_LEAD);
        rawMaterial("titanium", TGItems.RAW_TITANIUM);
        rawMaterial("uranium", TGItems.RAW_URANIUM);

        // Ingots
        ingot("tin", TGItems.TIN_INGOT);
        ingot("lead", TGItems.LEAD_INGOT);
        ingot("bronze", TGItems.BRONZE_INGOT);
        ingot("steel", TGItems.STEEL_INGOT);
        ingot("obsidian_steel", TGItems.OBSIDIAN_STEEL_INGOT);
        ingot("titanium", TGItems.TITANIUM_INGOT);

        // Nuggets
        nugget("copper", TGItems.COPPER_NUGGET);
        nugget("lead", TGItems.LEAD_NUGGET);
        nugget("steel", TGItems.STEEL_NUGGET);

        // Plates
        plate("iron", TGItems.IRON_PLATE);
        plate("copper", TGItems.COPPER_PLATE);
        plate("tin", TGItems.TIN_PLATE);
        plate("bronze", TGItems.BRONZE_PLATE);
        plate("steel", TGItems.STEEL_PLATE);
        plate("obsidian_steel", TGItems.OBSIDIAN_STEEL_PLATE);
        plate("lead", TGItems.LEAD_PLATE);
        plate("carbon", TGItems.CARBON_PLATE);
        plate("titanium", TGItems.TITANIUM_PLATE);
    }

    private static TagKey<Item> curiosSlot(TGSlotType slot) {
        return TagKey.create(Registries.ITEM, TGIds.of("curios", slot.curiosId()));
    }

    private void rawMaterial(String material, DeferredItem<Item> item) {
        tag(TGTags.Items.rawMaterial(material)).add(item.get());
        tag(Tags.Items.RAW_MATERIALS).addTag(TGTags.Items.rawMaterial(material));
    }

    private void ingot(String material, DeferredItem<Item> item) {
        tag(TGTags.Items.ingot(material)).add(item.get());
        tag(Tags.Items.INGOTS).addTag(TGTags.Items.ingot(material));
    }

    private void nugget(String material, DeferredItem<Item> item) {
        tag(TGTags.Items.nugget(material)).add(item.get());
        tag(Tags.Items.NUGGETS).addTag(TGTags.Items.nugget(material));
    }

    private void plate(String material, DeferredItem<Item> item) {
        tag(TGTags.Items.plate(material)).add(item.get());
        tag(TGTags.Items.PLATES).addTag(TGTags.Items.plate(material));
    }
}
