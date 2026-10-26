package techguns.armor;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import techguns.TGIds;
import techguns.gun.AmmoInventory;
import techguns.network.TGNetwork;
import techguns.player.TGPlayerData;
import techguns.registry.TGAttributes;
import techguns.registry.TGSounds;
import techguns.slot.ExtraSlots;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/**
 * Powered armor upkeep (Techguns 2 {@code PoweredArmor.calculateConsumptionTick}) and step assist. Every tick the
 * chestplate tops up the other pieces of its power type; every second of movement each powered piece uses 1 power;
 * an empty chestplate uses up a battery from the inventory. Bonus actions (jump, fall protection, cooling) use extra
 * power through {@link #useActionPower}.
 */
public final class ArmorPowerEvents {
    private static final ResourceLocation STEP_ASSIST_MODIFIER = TGIds.id("armor_step_assist");
    /** Step height with step assist: vanilla 0.6 + 0.5 = 1.1 (Techguns 2). */
    private static final double STEP_ASSIST_HEIGHT = 0.5;
    private static final List<EquipmentSlot> SLOTS = List.of(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    /** Server: player position at the last power check, to tell whether they moved. */
    private static final Map<Player, Vec3> LAST_POSITION = new WeakHashMap<>();

    private ArmorPowerEvents() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener(ArmorPowerEvents::onPlayerTick);
        gameBus.addListener((PlayerEvent.PlayerLoggedInEvent event) -> TGNetwork.syncPlayerData(event.getEntity()));
        gameBus.addListener((PlayerEvent.PlayerRespawnEvent event) -> TGNetwork.syncPlayerData(event.getEntity()));
        gameBus.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> TGNetwork.syncPlayerData(event.getEntity()));
    }

    private static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        updateStepAssist(player);
        if (!player.level().isClientSide) {
            boolean secondTick = player.level().getGameTime() % 20 == 0;
            boolean moved = false;
            if (secondTick) {
                Vec3 last = LAST_POSITION.put(player, player.position());
                moved = last != null && last.distanceToSqr(player.position()) > 1.0e-6;
            }
            consumptionTick(player, secondTick && moved);
        }
    }

    /** Step assist bonus with the player's toggle on: step up full blocks (both sides, movement is client side). */
    private static void updateStepAssist(Player player) {
        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
        AttributeInstance bonus = player.getAttribute(TGAttributes.STEP_ASSIST);
        if (step == null || bonus == null) {
            return;
        }
        if (bonus.getValue() > 0.0 && TGPlayerData.of(player).stepAssist()) {
            step.addOrUpdateTransientModifier(new AttributeModifier(STEP_ASSIST_MODIFIER, STEP_ASSIST_HEIGHT, AttributeModifier.Operation.ADD_VALUE));
        } else {
            step.removeModifier(STEP_ASSIST_MODIFIER);
        }
    }

    /** Techguns 2 {@code calculateConsumptionTick}: share chest power, use power while moving, swap batteries. */
    public static void consumptionTick(Player player, boolean consume) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        TGPoweredArmorItem chestItem = chest.getItem() instanceof TGPoweredArmorItem item ? item : null;
        powerSlots(player, chestItem, chest, consume);
        if (chestItem != null && !TGPoweredArmorItem.hasPower(chest) && chestItem.battery() != null) {
            ItemLike battery = chestItem.battery();
            if (AmmoInventory.consume(player, List.of(new ItemStack(battery)))) {
                ItemLike empty = chestItem.emptyBattery();
                if (empty != null) {
                    AmmoInventory.give(player, new ItemStack(empty));
                }
                chestItem.setFullyPowered(chest);
                powerSlots(player, chestItem, chest, false);
            }
        }
    }

    private static void powerSlots(Player player, TGPoweredArmorItem chestItem, ItemStack chest, boolean consume) {
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof TGPoweredArmorItem item)) {
                continue;
            }
            if (slot != EquipmentSlot.CHEST && chestItem != null && item.powerType() == chestItem.powerType()) {
                TGPoweredArmorItem.consumePower(chest, item.addPower(stack, TGPoweredArmorItem.power(chest)));
            }
            if (consume) {
                TGPoweredArmorItem.consumePower(stack, 1);
            }
        }
    }

    /**
     * A bonus action happened (Techguns 2 {@code getArmorBonusForPlayer(..., consumePower=true)}): every powered
     * piece that gives the bonus pays its {@link TGPoweredArmorItem#actionCost}; jumping with steam-powered legs plays
     * the steam jump sound.
     */
    public static void useActionPower(Player player, Holder<Attribute> bonus) {
        if (player.level().isClientSide || player.getAbilities().instabuild) {
            return;
        }
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof TGPoweredArmorItem item && TGPoweredArmorItem.hasPower(stack) && item.bonus(bonus) > 0.0) {
                int cost = item.actionCost(bonus);
                if (cost > 0) {
                    TGPoweredArmorItem.consumePower(stack, cost);
                    if (bonus.value() == TGAttributes.JUMP_BONUS.value()) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), TGSounds.STEAM_JUMP.get(),
                                SoundSource.PLAYERS, 1.0f, 1.0f);
                    }
                }
            }
        }
        for (TGSlotType slot : TGSlotType.values()) {
            ItemStack stack = ExtraSlots.get(player, slot);
            if (stack.getItem() instanceof TGSlotItem item && item.activeBonuses(player, stack).containsKey(bonus)) {
                item.onBonusAction(player, stack, bonus);
            }
        }
    }
}
