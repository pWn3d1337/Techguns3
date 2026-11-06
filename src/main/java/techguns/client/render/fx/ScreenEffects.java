package techguns.client.render.fx;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

/**
 * The screen effects a gun display file can name ({@code muzzle_flash}, {@code charge_effect}, {@code idle_effect},
 * {@code scope}). The effects themselves (texture, frames, blending, fade) stay in code: {@link IScreenEffect}.
 */
public final class ScreenEffects {
    private static final Map<String, IScreenEffect> BY_NAME = new LinkedHashMap<>();

    static {
        add("rifle", IScreenEffect.muzzleFlash_rifle);
        add("gun", IScreenEffect.muzzleFlash_gun);
        add("minigun", IScreenEffect.muzzleFlash_minigun);
        add("blue", IScreenEffect.muzzleFlash_blue);
        add("laser", IScreenEffect.muzzleFlashLaser);
        add("sonic", IScreenEffect.muzzleFlashSonic);
        add("nuke_beam", IScreenEffect.muzzleFlashNukeBeam);
        add("fireball", IScreenEffect.muzzleFlashFireball_alpha);
        add("alien_blaster", IScreenEffect.muzzleFlashAlienBlaster);
        add("lightning", IScreenEffect.muzzleFlashLightning);
        add("mib_gun", IScreenEffect.muzzleFlashMibGun);
        add("tfg", IScreenEffect.muzzleFlashTFG);
        add("green_flare", IScreenEffect.muzzleGreenFlare);
        add("flamethrower_flash", IScreenEffect.FlamethrowerMuzzleFlash);
        add("flamethrower_flame", IScreenEffect.FlamethrowerMuzzleFlame);
        add("magic_rifle_charge", IScreenEffect.magicRifleCharge);
        add("sniper_scope", IScreenEffect.sniperScope);
        add("tech_scope", IScreenEffect.techScope);
    }

    public static final Codec<IScreenEffect> CODEC = Codec.STRING.comapFlatMap(
            name -> {
                IScreenEffect effect = BY_NAME.get(name);
                return effect != null ? DataResult.success(effect) : DataResult.error(() -> "Unknown screen effect: " + name);
            },
            ScreenEffects::nameOf);

    private ScreenEffects() {
    }

    private static void add(String name, IScreenEffect effect) {
        BY_NAME.put(name, effect);
    }

    @Nullable
    public static IScreenEffect get(String name) {
        return BY_NAME.get(name);
    }

    public static String nameOf(IScreenEffect effect) {
        for (Map.Entry<String, IScreenEffect> entry : BY_NAME.entrySet()) {
            if (entry.getValue() == effect) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Screen effect without a name: " + effect);
    }
}
