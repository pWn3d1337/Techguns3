package techguns.gun;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The Techguns 2 dual wield rules ({@code GunManager.canUseOffhand}, {@code TGEventHandler.allowOffhandUse}): a
 * two-handed gun in either hand leaves the off hand unusable. The off-hand item can't be used on items, blocks or
 * entities, doesn't fire or reload, and isn't drawn (client {@code OffhandHiding}). One-and-a-half-handed guns can be
 * dual-wielded like one-handed guns.
 */
public final class GunHands {
    private GunHands() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener(EventPriority.HIGH, GunHands::onRightClickItem);
        gameBus.addListener(EventPriority.HIGH, GunHands::onRightClickBlock);
        gameBus.addListener(EventPriority.HIGH, GunHands::onEntityInteract);
        gameBus.addListener(EventPriority.HIGH, GunHands::onEntityInteractSpecific);
        gameBus.addListener(GunHands::onPlayerTick);
    }

    public static boolean isTwoHanded(ItemStack stack) {
        return stack.getItem() instanceof GunItem gun && gun.stats().handType() == GunHandType.TWO_HANDED;
    }

    public static boolean canUseOffhand(ItemStack mainHand, ItemStack offHand) {
        return !isTwoHanded(mainHand) && !isTwoHanded(offHand);
    }

    public static boolean canUseOffhand(LivingEntity entity) {
        return canUseOffhand(entity.getMainHandItem(), entity.getOffhandItem());
    }

    /** The main hand can always be used, the off hand only without a two-handed gun. */
    public static boolean canUse(LivingEntity entity, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND || canUseOffhand(entity);
    }

    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!canUse(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!canUse(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!canUse(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    private static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!canUse(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    /** Taking a two-handed gun while using the off-hand item (a shield, food) stops that use. */
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.isUsingItem() && !canUse(player, player.getUsedItemHand())) {
            player.stopUsingItem();
        }
    }
}
