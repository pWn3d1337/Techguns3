package techguns.camo;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * One camo of one item (datapack registry {@code techguns:camo}, Fabric {@code TGCamos} design with Techguns 2
 * textures and names). The item's camos are its entries sorted by {@code order}; order 0 is the default look. Items
 * store the camo's {@code name} in the {@code techguns:camo} component.
 *
 * @param item    the camo-able item
 * @param name    id of the camo within the item (e.g. {@code woodland}); stored on the stack
 * @param order   position when cycling; 0 = default
 * @param display shown name
 * @param texture full texture path (guns, single-texture armor models)
 * @param layer   armor layer texture base: {@code textures/models/armor/<path>_layer_1/_2.png}
 * @param tints   RGB tint of each item icon layer (Fabric layered armor icons); -1 or a missing entry leaves a layer untinted
 */
public record TGCamo(Holder<Item> item, String name, int order, Component display, Optional<ResourceLocation> texture,
                     Optional<ResourceLocation> layer, List<Integer> tints) {
    public static final Codec<TGCamo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(TGCamo::item),
            Codec.STRING.fieldOf("name").forGetter(TGCamo::name),
            Codec.INT.optionalFieldOf("order", 0).forGetter(TGCamo::order),
            ComponentSerialization.CODEC.fieldOf("display").forGetter(TGCamo::display),
            ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(TGCamo::texture),
            ResourceLocation.CODEC.optionalFieldOf("layer").forGetter(TGCamo::layer),
            Codec.INT.listOf().optionalFieldOf("tints", List.of()).forGetter(TGCamo::tints)
    ).apply(instance, TGCamo::new));

    /** The ARGB tint of an item icon layer ({@code tintindex}); -1 (opaque white) for an untinted layer. */
    public int tint(int layer) {
        int rgb = layer >= 0 && layer < tints.size() ? tints.get(layer) : -1;
        return rgb == -1 ? -1 : 0xFF000000 | rgb;
    }
}
