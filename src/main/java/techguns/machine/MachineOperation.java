package techguns.machine;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

/**
 * A running machine job (Techguns 2 {@code MachineOperation}): the inputs are taken when it starts, the outputs (per
 * operation, times {@code multiplier}) are delivered when it finishes.
 */
public record MachineOperation(List<ItemStack> outputs, int multiplier, int time) {
    public static final Codec<MachineOperation> CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("outputs").forGetter(MachineOperation::outputs),
            Codec.INT.fieldOf("multiplier").forGetter(MachineOperation::multiplier),
            Codec.INT.fieldOf("time").forGetter(MachineOperation::time)
    ).apply(i, MachineOperation::new));

    public MachineOperation {
        outputs = outputs.stream().map(ItemStack::copy).toList();
    }

    /** Output {@code index} with the multiplier applied, or empty. */
    public ItemStack output(int index) {
        if (index >= outputs.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack out = outputs.get(index);
        return out.copyWithCount(out.getCount() * multiplier);
    }
}
