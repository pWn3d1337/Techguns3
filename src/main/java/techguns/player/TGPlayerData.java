package techguns.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.player.Player;
import techguns.registry.TGAttachments;

/**
 * Persistent per-player Techguns settings (Techguns 2 {@code TGExtendedPlayer} flags). The server copy is saved and
 * sent to the client on login and respawn; the client changes its copy and tells the server (see {@code TGNetwork}).
 */
public class TGPlayerData {
    public static final Codec<TGPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("step_assist", true).forGetter(data -> data.stepAssist)
    ).apply(instance, TGPlayerData::new));

    /** Step assist of armor is enabled (Techguns 2 default: on). */
    private boolean stepAssist;

    public TGPlayerData() {
        this(true);
    }

    private TGPlayerData(boolean stepAssist) {
        this.stepAssist = stepAssist;
    }

    public static TGPlayerData of(Player player) {
        return player.getData(TGAttachments.PLAYER_DATA);
    }

    public boolean stepAssist() {
        return stepAssist;
    }

    public void setStepAssist(boolean stepAssist) {
        this.stepAssist = stepAssist;
    }
}
