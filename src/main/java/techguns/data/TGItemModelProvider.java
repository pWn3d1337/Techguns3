package techguns.data;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import techguns.Techguns;
import techguns.registry.TGItems;

public class TGItemModelProvider extends ItemModelProvider {
    public TGItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Techguns.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        TGItems.simpleItems().forEach(item -> basicItem(item.get()));
    }
}
