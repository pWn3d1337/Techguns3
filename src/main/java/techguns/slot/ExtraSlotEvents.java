package techguns.slot;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import techguns.TGIds;
import techguns.network.TGNetwork;
import techguns.player.TGPlayerData;
import techguns.registry.TGAttributes;

/**
 * Extra slot upkeep: every tick the worn gear's bonuses become transient attribute modifiers (one id per slot and
 * attribute, removed when the gear is gone or empty) and the gear ticks. Also night vision and flying speed from the
 * bonus attributes (Techguns 2 {@code TGTickHandler}), and the fallback slots' sync and death drops.
 */
public final class ExtraSlotEvents {
    /** Techguns 2 default flying speed. */
    private static final float DEFAULT_FLY_SPEED = 0.05f;
    /** Night vision effect length; refreshed while active (Techguns 2: 220). */
    private static final int NIGHT_VISION_TICKS = 220;
    private static final int NIGHT_VISION_REFRESH_TICKS = 205;
    /**
     * Modifiers currently applied per player: modifier id -> attribute. One map per side: the tick runs on the client
     * and the integrated server thread at the same time, and the maps are not thread-safe.
     */
    private static final Map<Player, Map<ResourceLocation, Holder<Attribute>>> APPLIED_SERVER = new WeakHashMap<>();
    private static final Map<Player, Map<ResourceLocation, Holder<Attribute>>> APPLIED_CLIENT = new WeakHashMap<>();

    private ExtraSlotEvents() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener(ExtraSlotEvents::onPlayerTick);
        gameBus.addListener(ExtraSlotEvents::onDrops);
        gameBus.addListener((PlayerEvent.PlayerLoggedInEvent event) -> ExtraSlots.markChanged(event.getEntity()));
        gameBus.addListener((PlayerEvent.PlayerRespawnEvent event) -> ExtraSlots.markChanged(event.getEntity()));
        gameBus.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> ExtraSlots.markChanged(event.getEntity()));
        gameBus.addListener((PlayerEvent.StartTracking event) -> {
            if (!ExtraSlots.usesCurios() && event.getTarget() instanceof Player tracked && event.getEntity() instanceof ServerPlayer receiver) {
                TGNetwork.syncExtraSlotsTo(tracked, receiver);
            }
        });
    }

    private static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        Map<ResourceLocation, Holder<Attribute>> wanted = new HashMap<>();
        Map<ResourceLocation, Double> values = new HashMap<>();
        for (TGSlotType slot : TGSlotType.values()) {
            ItemStack stack = ExtraSlots.get(player, slot);
            if (stack.getItem() instanceof TGSlotItem item) {
                item.slotTick(player, stack);
                item.activeBonuses(player, stack).forEach((attribute, value) -> {
                    ResourceLocation id = TGIds.id("slot." + slot.id() + "." + attribute.unwrapKey().map(key -> key.location().getPath()).orElse("bonus"));
                    wanted.put(id, attribute);
                    values.put(id, value);
                });
            }
        }
        Map<ResourceLocation, Holder<Attribute>> applied = (player.level().isClientSide ? APPLIED_CLIENT : APPLIED_SERVER)
                .computeIfAbsent(player, p -> new HashMap<>());
        applied.forEach((id, attribute) -> {
            if (!wanted.containsKey(id)) {
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null) {
                    instance.removeModifier(id);
                }
            }
        });
        wanted.forEach((id, attribute) -> {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.addOrUpdateTransientModifier(new AttributeModifier(id, values.get(id), AttributeModifier.Operation.ADD_VALUE));
            }
        });
        applied.clear();
        applied.putAll(wanted);

        nightVision(player);
        flySpeed(player);
    }

    /**
     * Night vision bonus and the toggle on: keep the effect up (Techguns 2 refreshed it to 220 ticks every tick). It is
     * only refreshed before it drops to the 200 ticks where the screen starts to flicker: every refresh sends a packet.
     */
    private static void nightVision(Player player) {
        if (!player.level().isClientSide && player.getAttributeValue(TGAttributes.NIGHT_VISION) > 0.0
                && TGPlayerData.of(player).get(TGPlayerData.Toggle.NIGHT_VISION)) {
            MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
            if (current != null && !current.endsWithin(NIGHT_VISION_REFRESH_TICKS)) {
                return;
            }
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_TICKS, 0, false, false));
        }
    }

    /** Flying speed from the bonus (creative flight itself is NeoForge's {@code creative_flight} attribute). */
    private static void flySpeed(Player player) {
        if (player.getAbilities().instabuild) {
            return;
        }
        float speed = DEFAULT_FLY_SPEED * (1.0f + (float) player.getAttributeValue(TGAttributes.FLY_SPEED_BONUS));
        if (player.getAbilities().getFlyingSpeed() != speed) {
            player.getAbilities().setFlyingSpeed(speed);
        }
    }

    /** Fallback slots drop with the inventory on death (kept with keepInventory). */
    private static void onDrops(LivingDropsEvent event) {
        if (ExtraSlots.usesCurios() || !(event.getEntity() instanceof Player player)
                || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }
        FallbackSlots.Inventory inventory = FallbackSlots.inventory(player);
        for (TGSlotType slot : TGSlotType.values()) {
            ItemStack stack = inventory.get(slot);
            if (!stack.isEmpty()) {
                ItemEntity drop = new ItemEntity(player.level(), player.getX(), player.getEyeY() - 0.3, player.getZ(), stack);
                drop.setDefaultPickUpDelay();
                event.getDrops().add(drop);
                inventory.set(slot, ItemStack.EMPTY);
            }
        }
        ExtraSlots.markChanged(player);
    }
}
