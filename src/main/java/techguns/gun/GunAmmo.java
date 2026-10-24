package techguns.gun;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import techguns.gun.ammo.AmmoType;

/**
 * Data component: rounds loaded in a gun and the ammo variant they came from.
 */
public record GunAmmo(int loaded, String variant) {
    public static final GunAmmo EMPTY = new GunAmmo(0, AmmoType.DEFAULT);

    public static final Codec<GunAmmo> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("loaded").forGetter(GunAmmo::loaded),
            Codec.STRING.optionalFieldOf("variant", AmmoType.DEFAULT).forGetter(GunAmmo::variant)
    ).apply(i, GunAmmo::new));

    public static final StreamCodec<ByteBuf, GunAmmo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, GunAmmo::loaded,
            ByteBufCodecs.STRING_UTF8, GunAmmo::variant,
            GunAmmo::new);

    public GunAmmo withLoaded(int loaded) {
        return new GunAmmo(loaded, variant);
    }
}
