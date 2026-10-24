package techguns.gun;

import java.util.Locale;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** Third-person arm pose while holding a gun. */
public enum HoldType implements StringRepresentable {
    /** Two-handed rifle hold (vanilla crossbow pose). */
    CROSSBOW,
    /** Raised like a bow (rocket launcher on the shoulder). */
    BOW,
    /** Arm lowered, like a normal item. */
    LOWERED;

    public static final Codec<HoldType> CODEC = StringRepresentable.fromEnum(HoldType::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
