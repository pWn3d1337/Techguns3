package techguns.slot;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.item.AmmoBagData;
import techguns.network.TGNetwork;
import techguns.registry.TGAttachments;
import techguns.registry.TGTags;

/**
 * The item slots of the Techguns inventory tab (Techguns 2 {@code TGPlayerInventory}): three auto-feed slots, one heal
 * item slot and two ammo box slots, kept in the {@code techguns:tab_slots} attachment with the food points left over
 * from the last meal. Independent of Curios. Only the server feeds; the stacks are resent to the owner's client whenever
 * they change (the ammo in the boxes counts for reloads and the HUD).
 */
public final class TabSlots {
    public static final int FOOD_START = 0;
    public static final int FOOD_COUNT = 3;
    public static final int HEAL = FOOD_START + FOOD_COUNT;
    public static final int AMMO_START = HEAL + 1;
    public static final int AMMO_COUNT = 2;
    public static final int SIZE = AMMO_START + AMMO_COUNT;

    /** Server: copies of the stacks last sent to each player's client. */
    private static final Map<Player, List<ItemStack>> SENT = Collections.synchronizedMap(new WeakHashMap<>());

    private TabSlots() {
    }

    /** Attachment data: the slot stacks and the kept food points. */
    public static final class Inventory implements AutoFeeding.Source {
        public static final Codec<Inventory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.OPTIONAL_CODEC.listOf().fieldOf("stacks").forGetter(inventory -> List.copyOf(inventory.stacks)),
                Codec.INT.optionalFieldOf("food_left", 0).forGetter(Inventory::foodLeft)
        ).apply(instance, (stacks, foodLeft) -> {
            Inventory inventory = new Inventory();
            for (int i = 0; i < stacks.size() && i < SIZE; i++) {
                inventory.stacks.set(i, stacks.get(i));
            }
            inventory.foodLeft = foodLeft;
            return inventory;
        }));

        private final NonNullList<ItemStack> stacks = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        private int foodLeft;

        public NonNullList<ItemStack> stacks() {
            return stacks;
        }

        /** The ammo box slots' stacks (live). */
        public List<ItemStack> ammoBoxes() {
            return stacks.subList(AMMO_START, AMMO_START + AMMO_COUNT);
        }

        @Override
        public int foodLeft() {
            return foodLeft;
        }

        @Override
        public void setFoodLeft(int points) {
            foodLeft = Math.max(0, points);
        }

        /** The first food of the food slots, in slot order (Techguns 2 {@code InventoryUtil.consumeFood}). */
        @Override
        public ItemStack takeMeal(Player player) {
            for (int i = FOOD_START; i < FOOD_START + FOOD_COUNT; i++) {
                ItemStack stack = stacks.get(i);
                if (!stack.isEmpty() && stack.getFoodProperties(player) != null) {
                    return stack.split(1);
                }
            }
            return ItemStack.EMPTY;
        }
    }

    public static Inventory of(Player player) {
        return player.getData(TGAttachments.TAB_SLOTS);
    }

    /** Whether the stack may go into tab slot {@code index}. */
    public static boolean accepts(int index, ItemStack stack) {
        if (index < HEAL) {
            return isFood(stack);
        }
        return index == HEAL ? stack.is(TGTags.Items.HEAL_ITEMS) : AmmoBagData.isBag(stack);
    }

    public static boolean isFood(ItemStack stack) {
        return stack.getFoodProperties(null) != null;
    }

    /** Sends the stacks to the player's client if they differ from what it last got (server, every tick). */
    public static void syncIfChanged(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        List<ItemStack> stacks = of(player).stacks();
        List<ItemStack> sent = SENT.get(player);
        if (sent != null && ItemStack.listMatches(sent, stacks)) {
            return;
        }
        List<ItemStack> copy = stacks.stream().map(ItemStack::copy).toList();
        SENT.put(player, copy);
        TGNetwork.syncTabSlots(serverPlayer, copy);
    }

    /** The client lost or never got the stacks (new player entity on its side); they are sent again with the next check. */
    public static void forgetSent(Player player) {
        SENT.remove(player);
    }

    /** One auto-feed step from the player's food slots; returns whether the player was fed. */
    public static boolean feed(Player player) {
        return AutoFeeding.feed(player, of(player));
    }
}
