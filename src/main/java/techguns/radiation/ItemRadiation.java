package techguns.radiation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import techguns.TGIds;

/**
 * Radioactive items (Techguns 2 {@code ItemRadiationRegistry}): carrying one in the main inventory gives the radiation
 * effect of its level (the strongest item counts). An item data map, so datapacks can add items.
 */
public record ItemRadiation(int level) {
    public static final Codec<ItemRadiation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 255).fieldOf("level").forGetter(ItemRadiation::level)
    ).apply(instance, ItemRadiation::new));

    /** {@code techguns:radiation}, synced for the tooltip. */
    public static final DataMapType<Item, ItemRadiation> DATA_MAP = DataMapType.builder(TGIds.id("radiation"), Registries.ITEM, CODEC)
            .synced(CODEC, false).build();

    /** The radiation level of a stack, 0 if it isn't radioactive. */
    public static int of(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        ItemRadiation data = stack.getItemHolder().getData(DATA_MAP);
        return data == null ? 0 : data.level();
    }
}
