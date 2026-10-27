package techguns.machine;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * A worn armor or offhand slot of the player inside a bench menu (Techguns 2 {@code SlotArmor}): takes only what fits
 * the slot, shows the empty armor icon and keeps binding curses like the inventory screen.
 */
// MIGRATION(26.x): Slot background icons become sprites in 1.21.2+.
public class PlayerEquipmentSlot extends Slot {
    private static final ResourceLocation[] ARMOR_ICONS = {InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
            InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};

    private final EquipmentSlot equipmentSlot;
    private final Player player;

    private PlayerEquipmentSlot(Inventory inventory, int index, int x, int y, EquipmentSlot equipmentSlot) {
        super(inventory, index, x, y);
        this.equipmentSlot = equipmentSlot;
        this.player = inventory.player;
    }

    /** The worn {@code slot} (head, chest, legs or feet). */
    public static PlayerEquipmentSlot armor(Inventory inventory, EquipmentSlot slot, int x, int y) {
        return new PlayerEquipmentSlot(inventory, Inventory.INVENTORY_SIZE + slot.getIndex(), x, y, slot);
    }

    public static PlayerEquipmentSlot offhand(Inventory inventory, int x, int y) {
        return new PlayerEquipmentSlot(inventory, Inventory.SLOT_OFFHAND, x, y, EquipmentSlot.OFFHAND);
    }

    public EquipmentSlot equipmentSlot() {
        return equipmentSlot;
    }

    @Override
    public int getMaxStackSize() {
        return equipmentSlot == EquipmentSlot.OFFHAND ? super.getMaxStackSize() : 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return equipmentSlot == EquipmentSlot.OFFHAND || player.getEquipmentSlotForItem(stack) == equipmentSlot;
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack stack = getItem();
        return equipmentSlot == EquipmentSlot.OFFHAND || stack.isEmpty() || player.isCreative()
                || !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        ResourceLocation icon = equipmentSlot == EquipmentSlot.OFFHAND ? InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD : ARMOR_ICONS[equipmentSlot.getIndex()];
        return Pair.of(InventoryMenu.BLOCK_ATLAS, icon);
    }
}
