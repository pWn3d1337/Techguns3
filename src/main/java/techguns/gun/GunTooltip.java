package techguns.gun;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import techguns.core.damage.TGDamageCategory;

/**
 * Pieces of the Techguns 2 gun tooltip ({@code GenericGun.addInformation}): grey stat lines, values an ammo variant
 * changes in green (better) or red (worse) with the change in brackets, and the coloured hand type and damage type.
 */
public final class GunTooltip {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    private GunTooltip() {
    }

    /** A grey tooltip line. */
    public static MutableComponent line(String key, Object... args) {
        return Component.translatable(key, args).withStyle(ChatFormatting.GRAY);
    }

    /** One decimal, as Techguns 2 showed damage, range and velocity. */
    public static String oneDecimal(float value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    /** Up to three decimals without trailing zeros. */
    public static String number(double value) {
        return FORMAT.format(value);
    }

    /** {@code text} green when the ammo raises the value above {@code base}, red when it lowers it. */
    public static MutableComponent modified(String text, float base, float value) {
        MutableComponent component = Component.literal(text);
        return value == base ? component : component.withStyle(value > base ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    /** The ammo's change of a value in brackets ({@code (+10%)}, {@code (-1.5)}), coloured like the value; empty if none. */
    public static Component change(float base, float value, float multiplier, float add) {
        if (value == base) {
            return Component.empty();
        }
        StringBuilder text = new StringBuilder();
        if (multiplier != 1.0f) {
            text.append(String.format(Locale.ROOT, " (%+.0f%%)", (multiplier - 1.0f) * 100.0f));
        }
        if (add != 0.0f) {
            text.append(String.format(Locale.ROOT, " (%+.1f)", add));
        }
        return Component.literal(text.toString()).withStyle(value > base ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    public static Component handType(GunHandType type) {
        ChatFormatting color = switch (type) {
            case ONE_HANDED -> ChatFormatting.GREEN;
            case ONE_POINT_FIVE_HANDED -> ChatFormatting.YELLOW;
            case TWO_HANDED -> ChatFormatting.GOLD;
        };
        return Component.translatable("techguns.gun_hand_type." + type.getSerializedName()).withStyle(color);
    }

    /** Techguns 2 {@code DamageType.toString} colours; black (unresistable, dark) is unreadable on a tooltip, so those got others. */
    public static Component damageCategory(TGDamageCategory category) {
        ChatFormatting color = switch (category) {
            case PROJECTILE -> ChatFormatting.GRAY;
            case FIRE -> ChatFormatting.RED;
            case EXPLOSION -> ChatFormatting.DARK_RED;
            case ENERGY -> ChatFormatting.DARK_AQUA;
            case POISON -> ChatFormatting.DARK_GREEN;
            case UNRESISTABLE -> ChatFormatting.WHITE;
            case ICE -> ChatFormatting.AQUA;
            case LIGHTNING -> ChatFormatting.YELLOW;
            case RADIATION -> ChatFormatting.GREEN;
            case DARK -> ChatFormatting.DARK_PURPLE;
            case PHYSICAL -> ChatFormatting.DARK_GRAY;
        };
        return Component.translatable("techguns.damage_category." + category.id()).withStyle(color);
    }
}
