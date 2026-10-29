package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;
import techguns.slot.item.AntiGravPackItem;
import techguns.slot.item.AutoFeederItem;
import techguns.slot.item.GasMaskItem;
import techguns.slot.item.GliderItem;
import techguns.slot.item.JetpackItem;
import techguns.slot.item.JumpPackItem;
import techguns.slot.item.NightVisionGogglesItem;
import techguns.slot.item.ScubaTanksItem;
import techguns.slot.item.TacticalMaskItem;

/**
 * Gear worn in the extra face/back/hand/mouth slots (Techguns 2 {@code TGItems} additional slot items and the shared
 * oxygen mask and working gloves). Durability is the charge.
 */
public final class TGSlotItems {
    private static final List<DeferredItem<? extends TGSlotItem>> ITEMS = new ArrayList<>();
    private static final List<DeferredItem<Item>> COMPONENTS = new ArrayList<>();

    // --- Face ---
    public static final DeferredItem<GasMaskItem> GAS_MASK = slot("gas_mask", props -> new GasMaskItem(props, 300, 1.0));
    public static final DeferredItem<TGSlotItem> OXYGEN_MASK = slot("oxygen_mask",
            props -> new TGSlotItem(props, TGSlotType.FACE, 0).bonus(TGAttributes.OXYGEN_GEAR, 1.0));
    public static final DeferredItem<NightVisionGogglesItem> NIGHT_VISION_GOGGLES = slot("night_vision_goggles",
            props -> new NightVisionGogglesItem(props, 24000));
    public static final DeferredItem<TacticalMaskItem> TACTICAL_MASK = slot("tactical_mask", props -> new TacticalMaskItem(props, 24000));

    // --- Back ---
    public static final DeferredItem<GliderItem> GLIDER = slot("glider", GliderItem::new);
    public static final DeferredItem<JumpPackItem> JUMP_PACK = slot("jump_pack", props -> new JumpPackItem(props, 1000));
    public static final DeferredItem<JetpackItem> JETPACK = slot("jetpack", props -> new JetpackItem(props, 5000));
    public static final DeferredItem<ScubaTanksItem> SCUBA_TANKS = slot("scuba_tanks", props -> new ScubaTanksItem(props, 600));
    public static final DeferredItem<AntiGravPackItem> ANTIGRAV_PACK = slot("antigrav_pack", props -> new AntiGravPackItem(props, 12000));

    // --- Hand ---
    public static final DeferredItem<TGSlotItem> WORKING_GLOVES = slot("working_gloves",
            props -> new TGSlotItem(props, TGSlotType.HAND, 0).bonus(TGAttributes.MINING_SPEED_BONUS, 0.10));

    // --- Mouth ---
    /** Replaces the Techguns 2 auto-feeder: three stacks of food, like the three food slots of the Techguns 2 inventory tab. */
    public static final DeferredItem<AutoFeederItem> AUTO_FEEDER = slot("auto_feeder", props -> new AutoFeederItem(props, 64 * 3));

    // --- Crafting components of the gear (flat models, like TGItems.simple) ---
    public static final DeferredItem<Item> GAS_MASK_FILTER = component("gas_mask_filter");
    public static final DeferredItem<Item> GLIDER_BACKPACK = component("glider_backpack");
    public static final DeferredItem<Item> GLIDER_WING = component("glider_wing");

    private TGSlotItems() {
    }

    /** Loads the item holders before the item register is attached. */
    public static void init() {
    }

    /** All wearable extra slot items. */
    public static List<DeferredItem<? extends TGSlotItem>> items() {
        return Collections.unmodifiableList(ITEMS);
    }

    /** Plain crafting items of the gear. */
    public static List<DeferredItem<Item>> components() {
        return Collections.unmodifiableList(COMPONENTS);
    }

    private static <T extends TGSlotItem> DeferredItem<T> slot(String name, Function<Item.Properties, T> factory) {
        DeferredItem<T> item = TGItems.ITEMS.registerItem(name, factory);
        ITEMS.add(item);
        return item;
    }

    private static DeferredItem<Item> component(String name) {
        DeferredItem<Item> item = TGItems.ITEMS.registerSimpleItem(name);
        COMPONENTS.add(item);
        return item;
    }
}
