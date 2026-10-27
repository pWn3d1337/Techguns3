package techguns.recipe;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import techguns.registry.TGRecipes;

/**
 * Reaction Chamber recipe (Techguns 2 {@code ReactionChamberRecipe}): an input item (slot 0) and a beam focus (slot 1)
 * react in exactly {@code liquidLevel} buckets of {@code fluid} at {@code intensity}. The reaction lasts up to
 * {@code ticks} reaction ticks (60 game ticks each, {@code powerPerTick} FE each); every tick at the required intensity
 * adds one completion, {@code requiredCompletion} succeeds. {@code instability} is the chance per tick that the
 * required intensity moves by up to {@code intensityMargin}; running out of time fails with {@code risk}.
 */
public record ReactionChamberRecipe(Ingredient input, Ingredient focus, FluidIngredient fluid, List<ItemStack> outputs, int ticks,
                                    int requiredCompletion, int intensity, int intensityMargin, int liquidLevel, int liquidConsumption,
                                    float instability, Risk risk, int powerPerTick) implements MachineRecipe {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FOCUS = 1;
    public static final int MAX_LEVEL = 10;
    public static final int BUCKET = 1000;

    /** What happens when the reaction fails (Techguns 2 {@code RiskType}). */
    public enum Risk implements StringRepresentable {
        BREAK_ITEM, EXPLOSION_LOW, EXPLOSION_MEDIUM;

        public static final Codec<Risk> CODEC = StringRepresentable.fromEnum(Risk::values);
        public static final StreamCodec<ByteBuf, Risk> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], Risk::ordinal);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public String translationKey() {
            return "techguns.reactionchamber.risk." + getSerializedName();
        }
    }

    /** Input and focus only (used for slot rules and recipe viewers). */
    @Override
    public boolean matches(MachineInput machineInput, Level level) {
        return input.test(machineInput.getItem(SLOT_INPUT)) && focus.test(machineInput.getItem(SLOT_FOCUS));
    }

    /** Techguns 2 {@code matches}: the tank holds exactly the recipe's buckets, set as liquid level, at its intensity. */
    public boolean canStart(ItemStack inputStack, ItemStack focusStack, FluidStack tank, int tankLevel, int currentIntensity) {
        return !inputStack.isEmpty() && input.test(inputStack) && focus.test(focusStack) && fluid.test(tank)
                && tankLevel == liquidLevel && tank.getAmount() == liquidLevel * BUCKET && currentIntensity == intensity;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.getFirst();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.REACTION_CHAMBER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TGRecipes.REACTION_CHAMBER.get();
    }

    public static class Serializer implements RecipeSerializer<ReactionChamberRecipe> {
        private static final Codec<Integer> LEVEL = Codec.intRange(0, MAX_LEVEL);
        private static final MapCodec<ReactionChamberRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(ReactionChamberRecipe::input),
                Ingredient.CODEC_NONEMPTY.fieldOf("focus").forGetter(ReactionChamberRecipe::focus),
                FluidIngredient.CODEC_NON_EMPTY.fieldOf("fluid").forGetter(ReactionChamberRecipe::fluid),
                ItemStack.STRICT_CODEC.listOf(1, 4).fieldOf("outputs").forGetter(ReactionChamberRecipe::outputs),
                Codec.intRange(1, 100).fieldOf("ticks").forGetter(ReactionChamberRecipe::ticks),
                Codec.intRange(1, 100).fieldOf("required_completion").forGetter(ReactionChamberRecipe::requiredCompletion),
                LEVEL.fieldOf("intensity").forGetter(ReactionChamberRecipe::intensity),
                LEVEL.optionalFieldOf("intensity_margin", 0).forGetter(ReactionChamberRecipe::intensityMargin),
                LEVEL.fieldOf("liquid_level").forGetter(ReactionChamberRecipe::liquidLevel),
                Codec.intRange(0, MAX_LEVEL * BUCKET).fieldOf("liquid_consumption").forGetter(ReactionChamberRecipe::liquidConsumption),
                Codec.floatRange(0.0f, 1.0f).optionalFieldOf("instability", 0.0f).forGetter(ReactionChamberRecipe::instability),
                Risk.CODEC.fieldOf("risk").forGetter(ReactionChamberRecipe::risk),
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("power_per_tick").forGetter(ReactionChamberRecipe::powerPerTick)
        ).apply(i, ReactionChamberRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ReactionChamberRecipe> STREAM_CODEC = StreamCodec.of(Serializer::write, Serializer::read);

        private static void write(RegistryFriendlyByteBuf buf, ReactionChamberRecipe r) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.input());
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.focus());
            FluidIngredient.STREAM_CODEC.encode(buf, r.fluid());
            ItemStack.LIST_STREAM_CODEC.encode(buf, r.outputs());
            buf.writeVarInt(r.ticks());
            buf.writeVarInt(r.requiredCompletion());
            buf.writeVarInt(r.intensity());
            buf.writeVarInt(r.intensityMargin());
            buf.writeVarInt(r.liquidLevel());
            buf.writeVarInt(r.liquidConsumption());
            buf.writeFloat(r.instability());
            Risk.STREAM_CODEC.encode(buf, r.risk());
            buf.writeVarInt(r.powerPerTick());
        }

        private static ReactionChamberRecipe read(RegistryFriendlyByteBuf buf) {
            return new ReactionChamberRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buf), Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                    FluidIngredient.STREAM_CODEC.decode(buf), ItemStack.LIST_STREAM_CODEC.decode(buf), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readFloat(), Risk.STREAM_CODEC.decode(buf),
                    buf.readVarInt());
        }

        @Override
        public MapCodec<ReactionChamberRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ReactionChamberRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
