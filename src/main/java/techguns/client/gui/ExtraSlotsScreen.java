package techguns.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.datafixers.util.Either;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import techguns.TGConfig;
import techguns.TGIds;
import techguns.armor.TGArmorTooltip;
import techguns.core.damage.TGDamageCategory;
import techguns.core.radiation.RadiationRules;
import techguns.damage.TGDamageEvents;
import techguns.network.TGNetwork;
import techguns.radiation.PlayerRadiation;
import techguns.registry.TGAmmo;
import techguns.registry.TGMenus;
import techguns.slot.ExtraSlots;
import techguns.slot.TabSlots;
import techguns.slot.menu.ExtraSlotsMenu;

/**
 * The Techguns inventory tab (Techguns 2 {@code TGPlayerInventoryGui}): armor, face/back/hand gear (with Curios the same
 * items as its slots) and offhand around the player, food, heal and ammo box slots on the right, the radiation bar
 * with its tooltip, the shield icon with the total armor tooltip and the vanilla inventory's recipe book; opened with a button on the vanilla inventory screen. Texture {@code tg_inventory.png}, the radiation
 * fill strip beside the panel; the empty slot icons are the 1.12 ones (armor and offhand use the vanilla ones).
 */
// MIGRATION(26.x): AbstractContainerScreen, GuiGraphics drawing and ScreenEvent.Init change with the 1.21.2+ GUI pipeline.
public class ExtraSlotsScreen extends AbstractContainerScreen<ExtraSlotsMenu> implements RecipeUpdateListener {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/tg_inventory.png");
    private static final ResourceLocation[] GEAR_ICONS = {
            TGIds.id("textures/gui/emptyslots/emptyslot_face.png"),
            TGIds.id("textures/gui/emptyslots/emptyslot_back.png"),
            TGIds.id("textures/gui/emptyslots/emptyslot_hand.png")};
    private static final ResourceLocation FOOD_ICON = TGIds.id("textures/gui/emptyslots/emptyslot_food.png");
    private static final ResourceLocation HEAL_ICON = TGIds.id("textures/gui/emptyslots/emptyslot_heal.png");
    private static final ResourceLocation AMMO_ICON = TGIds.id("textures/gui/emptyslots/emptyslot_bullet.png");
    /** Radiation bar: frame in the panel, fill strip beside it in the texture (Techguns 2 positions). */
    private static final int BAR_X = 115;
    private static final int BAR_Y = 7;
    private static final int BAR_WIDTH = 54;
    private static final int BAR_HEIGHT = 6;
    private static final int FILL_U = 177;
    private static final int FILL_WIDTH = 52;
    private static final int FILL_HEIGHT = 4;
    /** The shield icon in the texture: hovering shows the total armor (Techguns 2 position). */
    private static final int ARMOR_X = 98;
    private static final int ARMOR_Y = 7;
    private static final int ARMOR_SIZE = 14;

    /** The vanilla inventory's recipe book (same type, so it opens and closes together with the inventory's). */
    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean widthTooNarrow;
    private boolean buttonClicked;
    private Button backButton;

    public ExtraSlotsScreen(ExtraSlotsMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    public static void register(RegisterMenuScreensEvent event) {
        event.register(TGMenus.EXTRA_SLOTS.get(), ExtraSlotsScreen::new);
    }

    /**
     * Adds the "TG" button to the vanilla survival inventory (not to the creative one, which is another screen class); with
     * Curios only if the client config wants it.
     */
    public static void onInventoryScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen screen && (!ExtraSlots.usesCurios() || TGConfig.INVENTORY_TAB_WITH_CURIOS.get())) {
            event.addListener(new OpenButton(screen));
        }
    }

    /** The recipe book as in the vanilla {@code InventoryScreen}: button at the same place, the panel moves the screen aside. */
    @Override
    protected void init() {
        super.init();
        widthTooNarrow = width < 379;
        recipeBookComponent.init(width, height, minecraft, widthTooNarrow, menu);
        leftPos = recipeBookComponent.updateScreenPosition(width, imageWidth);
        addRenderableWidget(new ImageButton(leftPos + 104, height / 2 - 22, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
            recipeBookComponent.toggleVisibility();
            leftPos = recipeBookComponent.updateScreenPosition(width, imageWidth);
            button.setPosition(leftPos + 104, height / 2 - 22);
            backButton.setPosition(leftPos + 126, height / 2 - 22);
            buttonClicked = true;
        }));
        backButton = addRenderableWidget(new RecipeStyleButton(leftPos + 126, height / 2 - 22, Component.translatable("gui.techguns.inventory"),
                Component.translatable("gui.techguns.inventory.tooltip"), button -> backToInventory()));
        addWidget(recipeBookComponent);
    }

    /**
     * Closes this menu like {@code LocalPlayer#closeContainer} and opens the vanilla inventory, but without its
     * setScreen(null): with no screen in between, the mouse isn't grabbed and released again, which moved the cursor to
     * the window centre.
     */
    private void backToInventory() {
        minecraft.player.connection.send(new ServerboundContainerClosePacket(menu.containerId));
        minecraft.player.containerMenu = minecraft.player.inventoryMenu;
        minecraft.setScreen(new InventoryScreen(minecraft.player));
    }

    /**
     * The server's answer to a recipe clicked in the tab's recipe book: back to the vanilla inventory, whose crafting grid
     * takes the recipe (or shows it as a ghost recipe without the ingredients).
     */
    public static void placeRecipeInInventory(ResourceLocation id, boolean placeAll) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !(mc.screen instanceof ExtraSlotsScreen screen)) {
            return;
        }
        screen.backToInventory();
        mc.level.getRecipeManager().byKey(id).ifPresent(recipe -> mc.gameMode.handlePlaceRecipe(mc.player.inventoryMenu.containerId, recipe, placeAll));
    }

    @Override
    protected void containerTick() {
        recipeBookComponent.tick();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (recipeBookComponent.isVisible() && widthTooNarrow) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
            recipeBookComponent.render(graphics, mouseX, mouseY, partialTick);
        } else {
            super.render(graphics, mouseX, mouseY, partialTick);
            recipeBookComponent.render(graphics, mouseX, mouseY, partialTick);
        }
        renderTooltip(graphics, mouseX, mouseY);
        recipeBookComponent.renderTooltip(graphics, leftPos, topPos, mouseX, mouseY);
        if (isHovering(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, radiationTooltip(PlayerRadiation.get(minecraft.player)), mouseX, mouseY);
        } else if (isHovering(ARMOR_X, ARMOR_Y, ARMOR_SIZE, ARMOR_SIZE, mouseX, mouseY)) {
            graphics.renderComponentTooltipFromElements(font, armorTooltip(minecraft.player), mouseX, mouseY, ItemStack.EMPTY);
        }
    }

    /**
     * Techguns 2: the player's total armor against each damage category, each line with the category's armor icon in
     * the order and colours of the armor tooltips.
     */
    private static List<Either<FormattedText, TooltipComponent>> armorTooltip(Player player) {
        List<Either<FormattedText, TooltipComponent>> lines = new ArrayList<>();
        lines.add(Either.left(Component.translatable("gui.techguns.armor_total").withStyle(ChatFormatting.UNDERLINE)));
        for (int i = 0; i < TGArmorTooltip.CATEGORIES.size(); i++) {
            TGDamageCategory category = TGArmorTooltip.CATEGORIES.get(i);
            Component text = Component.translatable("techguns.damage_category." + category.id())
                    .append(String.format(Locale.ROOT, ": %.1f", TGDamageEvents.armorAgainst(player, category)))
                    .withStyle(ClientArmorTooltip.COLORS[i]);
            lines.add(Either.right(new ArmorSumLine(i, text)));
        }
        return lines;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return recipeBookComponent.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return recipeBookComponent.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!widthTooNarrow || !recipeBookComponent.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
            setFocused(recipeBookComponent);
            return true;
        }
        return (!widthTooNarrow || !recipeBookComponent.isVisible()) && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (buttonClicked) {
            buttonClicked = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        boolean outside = mouseX < guiLeft || mouseY < guiTop || mouseX >= guiLeft + imageWidth || mouseY >= guiTop + imageHeight;
        return recipeBookComponent.hasClickedOutside(mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight, mouseButton) && outside;
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        recipeBookComponent.slotClicked(slot);
    }

    @Override
    public void recipesUpdated() {
        recipeBookComponent.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return recipeBookComponent;
    }

    /** No labels: the texture has no room for them (the vanilla inventory drops its "Inventory" label too). */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    /** Techguns 2: the contamination out of the maximum and the effects of the current poisoning stage. */
    private static List<Component> radiationTooltip(int level) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui.techguns.radiation").withStyle(ChatFormatting.GREEN, ChatFormatting.UNDERLINE));
        lines.add(Component.translatable("gui.techguns.radiation.level", level, RadiationRules.MAX));
        RadiationRules.Stage stage = RadiationRules.stage(level);
        if (stage == RadiationRules.Stage.NONE) {
            lines.add(Component.translatable("gui.techguns.radiation.effects").append(": ")
                    .append(Component.translatable("gui.techguns.radiation.effects.none").withStyle(ChatFormatting.GREEN)));
            return lines;
        }
        lines.add(Component.translatable("gui.techguns.radiation.effects").append(":"));
        // the effects of RadiationEvents.poisoning
        if (stage == RadiationRules.Stage.MINOR) {
            lines.add(effect(MobEffects.HUNGER, 0));
        } else {
            lines.add(effect(MobEffects.HUNGER, 1));
            lines.add(effect(MobEffects.DIG_SLOWDOWN, 1));
            lines.add(effect(MobEffects.WEAKNESS, 1));
        }
        if (stage == RadiationRules.Stage.LETHAL) {
            lines.add(effect(MobEffects.CONFUSION, 1));
            lines.add(effect(MobEffects.MOVEMENT_SLOWDOWN, 1));
            lines.add(Component.literal(" ").append(Component.translatable("gui.techguns.radiation.damage")).withStyle(ChatFormatting.RED));
        }
        return lines;
    }

    private static Component effect(Holder<MobEffect> effect, int amplifier) {
        MutableComponent line = Component.literal(" ").append(Component.translatable(effect.value().getDescriptionId()));
        if (amplifier > 0) {
            line.append(" ").append(Component.translatable("enchantment.level." + (amplifier + 1)));
        }
        return line;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        int fill = Math.round(FILL_WIDTH * Math.min(PlayerRadiation.get(minecraft.player), RadiationRules.MAX) / (float) RadiationRules.MAX);
        if (fill > 0) {
            graphics.blit(TEXTURE, x + BAR_X + 1, y + BAR_Y + 1, FILL_U, 0, fill, FILL_HEIGHT);
        }
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x + 26, y + 8, x + 75, y + 78, 30, 0.0625F, mouseX, mouseY,
                minecraft.player);
        // empty slot icons of the gear and tab slots (armor and offhand draw their own)
        for (Slot slot : menu.slots) {
            ResourceLocation icon = emptyIcon(slot);
            if (icon != null && slot.isActive() && !slot.hasItem()) {
                graphics.blit(icon, x + slot.x, y + slot.y, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    private static ResourceLocation emptyIcon(Slot slot) {
        if (slot instanceof ExtraSlotsMenu.GearSlot gear) {
            return GEAR_ICONS[ExtraSlotsMenu.GEAR.indexOf(gear.type())];
        }
        if (slot instanceof ExtraSlotsMenu.TabSlot tab) {
            int index = tab.tabIndex();
            return index < TabSlots.HEAL ? FOOD_ICON : index == TabSlots.HEAL ? HEAL_ICON : AMMO_ICON;
        }
        return null;
    }

    /** Button on the vanilla inventory screen that stays beside the recipe book button, wherever the screen moves. */
    private static final class OpenButton extends RecipeStyleButton {
        private final InventoryScreen screen;

        OpenButton(InventoryScreen screen) {
            // the Techguns creative tab icon
            super(screen.getGuiLeft() + 126, screen.getGuiTop() + 61, Component.translatable("gui.techguns.extra_slots.button"),
                    TGAmmo.PISTOL_ROUNDS.get().getDefaultInstance(), Component.translatable("gui.techguns.extra_slots"),
                    button -> TGNetwork.sendOpenExtraSlots());
            this.screen = screen;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            setPosition(screen.getGuiLeft() + 126, screen.getGuiTop() + 61);
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
        }
    }
}
