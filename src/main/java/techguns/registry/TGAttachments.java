package techguns.registry;

import java.util.function.Supplier;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import techguns.Techguns;
import techguns.gun.ShooterState;
import techguns.player.TGPlayerData;

/**
 * Techguns data attachments (player data, shooter values, radiation, ...). Access them through facades
 * such as {@code ShooterState.of(player)}, never directly.
 */
// MIGRATION(26.x): newer NeoForge versions can sync attachments automatically; until then sync via TGNetwork payloads.
public final class TGAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Techguns.MODID);

    /** Gun timers; transient (no serializer), exists independently on client and server. */
    public static final Supplier<AttachmentType<ShooterState>> SHOOTER_STATE = ATTACHMENTS.register("shooter_state",
            () -> AttachmentType.builder(ShooterState::new).build());

    /** Saved player settings (toggles); copied on death, synced by TGNetwork. */
    public static final Supplier<AttachmentType<TGPlayerData>> PLAYER_DATA = ATTACHMENTS.register("player_data",
            () -> AttachmentType.builder(TGPlayerData::new).serialize(TGPlayerData.CODEC).copyOnDeath().build());

    private TGAttachments() {
    }
}
