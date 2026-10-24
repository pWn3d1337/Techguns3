package techguns.gun;

import java.util.Locale;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** Whether a gun can be dual-wielded. */
public enum GunHandType implements StringRepresentable {
    ONE_HANDED,
    TWO_HANDED;

    public static final Codec<GunHandType> CODEC = StringRepresentable.fromEnum(GunHandType::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
