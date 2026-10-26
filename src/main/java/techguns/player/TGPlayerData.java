package techguns.player;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.player.Player;
import techguns.registry.TGAttachments;

/**
 * Persistent per-player Techguns settings (Techguns 2 {@code TGExtendedPlayer} flags). The server copy is saved and
 * sent to the client on login and respawn; the client changes its copy and tells the server (see {@code TGNetwork}).
 */
public class TGPlayerData {
    /** Player toggles with their Techguns 2 defaults. */
    public enum Toggle {
        /** Step assist of armor (default on). */
        STEP_ASSIST(true),
        /** Night vision of goggles, masks and helmets (default off). */
        NIGHT_VISION(false),
        /** Jetpack and jump pack boost (default on). */
        JETPACK(true);

        private final boolean defaultValue;

        Toggle(boolean defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final Codec<TGPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("step_assist", true).forGetter(data -> data.get(Toggle.STEP_ASSIST)),
            Codec.BOOL.optionalFieldOf("night_vision", false).forGetter(data -> data.get(Toggle.NIGHT_VISION)),
            Codec.BOOL.optionalFieldOf("jetpack", true).forGetter(data -> data.get(Toggle.JETPACK))
    ).apply(instance, (step, nightVision, jetpack) -> {
        TGPlayerData data = new TGPlayerData();
        data.set(Toggle.STEP_ASSIST, step);
        data.set(Toggle.NIGHT_VISION, nightVision);
        data.set(Toggle.JETPACK, jetpack);
        return data;
    }));

    private final Set<Toggle> enabled = EnumSet.noneOf(Toggle.class);
    /** Jetpack boost key held (transient, both sides). */
    private boolean jetpackBoost;

    public TGPlayerData() {
        for (Toggle toggle : Toggle.values()) {
            set(toggle, toggle.defaultValue);
        }
    }

    public static TGPlayerData of(Player player) {
        return player.getData(TGAttachments.PLAYER_DATA);
    }

    public boolean get(Toggle toggle) {
        return enabled.contains(toggle);
    }

    public void set(Toggle toggle, boolean on) {
        if (on) {
            enabled.add(toggle);
        } else {
            enabled.remove(toggle);
        }
    }

    public boolean stepAssist() {
        return get(Toggle.STEP_ASSIST);
    }

    public void setStepAssist(boolean stepAssist) {
        set(Toggle.STEP_ASSIST, stepAssist);
    }

    public boolean jetpackBoost() {
        return jetpackBoost;
    }

    public void setJetpackBoost(boolean boost) {
        this.jetpackBoost = boost;
    }
}
