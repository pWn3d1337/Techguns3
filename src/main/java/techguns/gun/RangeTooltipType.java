package techguns.gun;

import java.util.Locale;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** How the gun tooltip shows the damage drop values (Techguns 2 {@code RangeTooltipType}). */
public enum RangeTooltipType implements StringRepresentable {
    /** Damage drop start, end and the projectile lifetime. */
    DROP,
    /** No damage drop: only the range. */
    NO_DROP,
    /** The drop values are the inner and outer explosion radius. */
    RADIUS;

    public static final Codec<RangeTooltipType> CODEC = StringRepresentable.fromEnum(RangeTooltipType::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
