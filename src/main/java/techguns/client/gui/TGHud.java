package techguns.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import techguns.TGConfig;
import techguns.TGIds;
import techguns.armor.TGPoweredArmorItem;
import techguns.gun.AmmoInventory;
import techguns.gun.GunData;
import techguns.gun.GunHands;
import techguns.gun.GunItem;
import techguns.player.TGPlayerData;
import techguns.registry.TGAttributes;
import techguns.slot.ExtraSlots;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;
import techguns.slot.item.AntiGravPackItem;
import techguns.slot.item.JetpackItem;
import techguns.slot.item.JumpPackItem;

/**
 * Techguns 2 HUD ({@code TGGuiEvents.onRenderGameOverlyEvent}) in the bottom right corner: the loaded and spare
 * reloads of the held guns (the off-hand gun only when it can be used); above them, with the HUD elements on (client
 * config {@code showHudElements}), the safe mode state while holding a gun, the power armor's power and spare
 * batteries, the fuel and on/off state of a jetpack, jump pack or antigrav pack and its spare refills, the night
 * vision and step assist state when worn gear gives them, and a warning for worn-out armor; then the radiation.
 */
public final class TGHud {
    /** The Techguns 2 HUD icons from {@code tgplayerinventory.png}: off and on column, 7 x 7 each. */
    private static final ResourceLocation ICONS = TGIds.id("textures/gui/hud_icons.png");
    private static final int ICON = 7;
    private static final int ICONS_WIDTH = 14;
    private static final int ICONS_HEIGHT = 42;
    private static final int ROW_NIGHT_VISION = 0;
    private static final int ROW_SAFE_MODE = 7;
    private static final int ROW_STEP_ASSIST = 14;
    /** Off column: low durability, on column: worn out. */
    private static final int ROW_ARMOR = 21;
    /** On column only. */
    private static final int ROW_POWER = 28;
    private static final int ROW_JETPACK = 35;
    /** Techguns 2 warned below 35 % durability. */
    private static final double LOW_DURABILITY = 0.35;

    private static final int MARGIN = 2;
    static final int LINE = 10;

    private TGHud() {
    }

    // MIGRATION(26.x): GUI layers change with the new GUI render pipeline.
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, TGIds.id("hud"), TGHud::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || player.isSpectator()) {
            return;
        }
        int right = graphics.guiWidth() - MARGIN;
        int ammoY = graphics.guiHeight() - MARGIN - mc.font.lineHeight;
        boolean holdsGun = false;
        if (player.getMainHandItem().getItem() instanceof GunItem gun) {
            drawRight(graphics, mc, ammo(player, gun, player.getMainHandItem()), right, ammoY);
            holdsGun = true;
        }
        if (player.getOffhandItem().getItem() instanceof GunItem gun && GunHands.canUseOffhand(player)) {
            drawRight(graphics, mc, ammo(player, gun, player.getOffhandItem()), right, ammoY - 12);
            holdsGun = true;
        }

        int y = graphics.guiHeight() - 32;
        if (TGConfig.SHOW_HUD_ELEMENTS.get()) {
            TGPlayerData data = TGPlayerData.of(player);
            int iconX = right - ICON;
            if (holdsGun) {
                icon(graphics, iconX, y, data.get(TGPlayerData.Toggle.SAFE_MODE), ROW_SAFE_MODE);
            }
            y -= LINE;
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof TGPoweredArmorItem armor && armor.maxPower() > 0) {
                icon(graphics, iconX, y, true, ROW_POWER);
                drawRight(graphics, mc, charge(player, armor.battery(), (double) TGPoweredArmorItem.power(chest) / armor.maxPower()), iconX - 2, y);
                y -= LINE;
            }
            ItemStack back = ExtraSlots.get(player, TGSlotType.BACK);
            if (back.getItem() instanceof TGSlotItem gear && (gear instanceof JetpackItem || gear instanceof JumpPackItem || gear instanceof AntiGravPackItem)) {
                icon(graphics, iconX, y, data.get(TGPlayerData.Toggle.JETPACK), ROW_JETPACK);
                double fuel = back.isDamageableItem() ? 1.0 - (double) back.getDamageValue() / back.getMaxDamage() : 1.0;
                drawRight(graphics, mc, charge(player, gear.refillItem(), fuel), iconX - 2, y);
                y -= LINE;
            }
            if (player.getAttributeValue(TGAttributes.NIGHT_VISION) > 0.0) {
                icon(graphics, iconX, y, data.get(TGPlayerData.Toggle.NIGHT_VISION), ROW_NIGHT_VISION);
                y -= LINE;
            }
            if (player.getAttributeValue(TGAttributes.STEP_ASSIST) > 0.0) {
                icon(graphics, iconX, y, data.stepAssist(), ROW_STEP_ASSIST);
                y -= LINE;
            }
            int armorState = armorState(player);
            if (armorState > 0) {
                icon(graphics, iconX, y, armorState == 2, ROW_ARMOR);
                y -= LINE;
            }
        }
        RadiationOverlay.render(graphics, mc, right, y);
    }

    /** {@code loaded/clip} and in yellow {@code x} the reloads the player carries (Techguns 2 {@code drawGunAmmoCount}). */
    private static Component ammo(Player player, GunItem gun, ItemStack stack) {
        int perReload = Math.max(1, gun.stats().firing().ammoPerReload());
        int reloads = 0;
        for (ItemStack ammo : gun.ammoType().ammoStacks(GunData.variant(stack))) {
            reloads = Math.max(reloads, AmmoInventory.count(player, ammo) / perReload);
        }
        return Component.literal(GunData.loaded(stack) + "/" + gun.stats().firing().clipSize())
                .append(Component.literal("x" + reloads).withStyle(ChatFormatting.YELLOW));
    }

    /** In yellow the spare refills carried ({@code 3x}), then the charge in percent. */
    private static Component charge(Player player, ItemLike refill, double fraction) {
        MutableComponent text = Component.empty();
        if (refill != null) {
            text.append(Component.literal(AmmoInventory.count(player, new ItemStack(refill)) + "x").withStyle(ChatFormatting.YELLOW));
        }
        return text.append(Component.literal((int) (fraction * 100) + "%"));
    }

    /** 0 = fine, 1 = a piece below 35 % durability, 2 = a piece worn out. */
    private static int armorState(Player player) {
        int state = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (!armor.isDamageableItem()) {
                continue;
            }
            if (armor.getDamageValue() >= armor.getMaxDamage() - 1) {
                return 2;
            }
            if ((double) (armor.getMaxDamage() - armor.getDamageValue()) / armor.getMaxDamage() < LOW_DURABILITY) {
                state = 1;
            }
        }
        return state;
    }

    private static void icon(GuiGraphics graphics, int x, int y, boolean on, int row) {
        graphics.blit(ICONS, x, y, on ? ICON : 0, row, ICON, ICON, ICONS_WIDTH, ICONS_HEIGHT);
    }

    private static void drawRight(GuiGraphics graphics, Minecraft mc, Component text, int right, int y) {
        graphics.drawString(mc.font, text, right - mc.font.width(text), y, 0xFFFFFFFF);
    }
}
