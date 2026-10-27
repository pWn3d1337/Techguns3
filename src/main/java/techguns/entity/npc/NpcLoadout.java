package techguns.entity.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.HolderLookup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import techguns.camo.CamoData;
import techguns.camo.TGCamo;

/**
 * Techguns 2 {@code addRandomArmor}: armor pieces, each with a chance (or a choice of two items), one camo for all of
 * them (fixed, or random like {@code GenericArmorMultiCamo.getRandomCamoIndexFor}), and a weighted weapon.
 */
public final class NpcLoadout {
    /** No camo set (the items' default look). */
    public static final int DEFAULT_CAMO = 0;
    /** A random camo of the first armor piece, shared by all pieces. */
    public static final int RANDOM_CAMO = -1;

    public static final NpcLoadout NONE = builder().build();

    private record Piece(EquipmentSlot slot, Supplier<? extends Item> item, Supplier<? extends Item> alternative, double chance) {
    }

    private record Weapon(Supplier<? extends Item> item, int weight) {
    }

    private final List<Piece> pieces;
    private final List<Weapon> weapons;
    private final int camo;
    /** Each piece rolls its own camo in this range (inclusive), when {@link #camoPerPieceMax} >= 0. */
    private final int camoPerPieceMin;
    private final int camoPerPieceMax;

    private NpcLoadout(List<Piece> pieces, List<Weapon> weapons, int camo, int camoPerPieceMin, int camoPerPieceMax) {
        this.pieces = pieces;
        this.weapons = weapons;
        this.camo = camo;
        this.camoPerPieceMin = camoPerPieceMin;
        this.camoPerPieceMax = camoPerPieceMax;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Equips {@code mob}; {@code registries} give the camos. */
    public void apply(Mob mob, RandomSource random, HolderLookup.Provider registries) {
        int camoIndex = camo;
        if (camoIndex == RANDOM_CAMO && !pieces.isEmpty()) {
            List<TGCamo> camos = CamoData.camos(registries, new ItemStack(pieces.getFirst().item().get()));
            camoIndex = camos.isEmpty() ? DEFAULT_CAMO : random.nextInt(camos.size());
        }
        for (Piece piece : pieces) {
            if (camoPerPieceMax >= 0) {
                camoIndex = camoPerPieceMin + random.nextInt(camoPerPieceMax - camoPerPieceMin + 1);
            }
            if (random.nextDouble() > piece.chance()) {
                if (piece.alternative() == null) {
                    continue;
                }
                mob.setItemSlot(piece.slot(), withCamo(new ItemStack(piece.alternative().get()), camoIndex, registries));
            } else {
                mob.setItemSlot(piece.slot(), withCamo(new ItemStack(piece.item().get()), camoIndex, registries));
            }
        }
        int total = weapons.stream().mapToInt(Weapon::weight).sum();
        if (total > 0) {
            int roll = random.nextInt(total);
            for (Weapon weapon : weapons) {
                roll -= weapon.weight();
                if (roll < 0) {
                    mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon.item().get()));
                    break;
                }
            }
        }
    }

    private static ItemStack withCamo(ItemStack stack, int index, HolderLookup.Provider registries) {
        if (index > DEFAULT_CAMO) {
            List<TGCamo> camos = CamoData.camos(registries, stack);
            if (!camos.isEmpty()) {
                CamoData.set(stack, camos.get(Math.min(index, camos.size() - 1)));
            }
        }
        return stack;
    }

    public static final class Builder {
        private final List<Piece> pieces = new ArrayList<>();
        private final List<Weapon> weapons = new ArrayList<>();
        private int camo = DEFAULT_CAMO;
        private int camoPerPieceMin;
        private int camoPerPieceMax = -1;

        private Builder() {
        }

        /** An armor piece worn with {@code chance}. */
        public Builder armor(EquipmentSlot slot, Supplier<? extends Item> item, double chance) {
            pieces.add(new Piece(slot, item, null, chance));
            return this;
        }

        /** One of two pieces, the first with {@code chance}. */
        public Builder armorChoice(EquipmentSlot slot, Supplier<? extends Item> item, Supplier<? extends Item> alternative, double chance) {
            pieces.add(new Piece(slot, item, alternative, chance));
            return this;
        }

        /** Camo index (cycling order) of all pieces, or {@link #RANDOM_CAMO}. */
        public Builder camo(int index) {
            this.camo = index;
            return this;
        }

        /** Every piece gets its own random camo index in {@code min..max} (Techguns 2 Outcast). */
        public Builder camoPerPiece(int min, int max) {
            this.camoPerPieceMin = min;
            this.camoPerPieceMax = max;
            return this;
        }

        public Builder weapon(Supplier<? extends Item> item, int weight) {
            weapons.add(new Weapon(item, weight));
            return this;
        }

        public NpcLoadout build() {
            return new NpcLoadout(List.copyOf(pieces), List.copyOf(weapons), camo, camoPerPieceMin, camoPerPieceMax);
        }
    }
}
