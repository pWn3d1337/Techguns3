package techguns.client.render.item;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import techguns.gun.GunHands;

/**
 * Techguns 2 {@code ItemRenderHack.shouldRenderItem}: the off-hand item of a player holding a two-handed gun is not
 * drawn. First person skips the off hand; third person empties the off-hand slot of the client copy of the player for
 * the time of its render (the item layer has no per-hand hook) and puts the item back right after.
 */
// MIGRATION(26.x): entity render states carry the held items; clear the off-hand item state there instead.
public final class OffhandHiding {
    /** Players rendered right now with their off-hand item taken out. */
    private static final Map<Player, ItemStack> HIDDEN = new IdentityHashMap<>();

    private OffhandHiding() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener(OffhandHiding::onRenderHand);
        gameBus.addListener(EventPriority.LOWEST, OffhandHiding::onRenderLivingPre);
        gameBus.addListener(EventPriority.HIGHEST, true, OffhandHiding::onRenderLivingPost);
        // a render cancelled after the item was taken out gets no Post
        gameBus.addListener((ClientTickEvent.Pre event) -> restoreAll());
    }

    private static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (event.getHand() == InteractionHand.OFF_HAND && player != null && !GunHands.canUseOffhand(player)) {
            event.setCanceled(true);
        }
    }

    private static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (!event.isCanceled() && event.getEntity() instanceof Player player && !player.getOffhandItem().isEmpty()
                && !GunHands.canUseOffhand(player) && !HIDDEN.containsKey(player)) {
            HIDDEN.put(player, player.getOffhandItem());
            // the inventory slot directly: setItemSlot would count as equipping (sounds, game events)
            player.getInventory().offhand.set(0, ItemStack.EMPTY);
        }
    }

    private static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (event.getEntity() instanceof Player player) {
            restore(player);
        }
    }

    private static void restore(Player player) {
        ItemStack stack = HIDDEN.remove(player);
        if (stack != null && player.getInventory().offhand.get(0).isEmpty()) {
            player.getInventory().offhand.set(0, stack);
        }
    }

    private static void restoreAll() {
        if (!HIDDEN.isEmpty()) {
            for (Player player : HIDDEN.keySet().toArray(Player[]::new)) {
                restore(player);
            }
        }
    }
}
