package techguns.registry;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import techguns.Techguns;

/**
 * Techguns data attachments (player data, shooter values, radiation, ...). Access them through facades
 * such as {@code TGPlayerData}, never directly.
 */
// MIGRATION(26.x): newer NeoForge versions can sync attachments automatically; until then sync via TGNetwork payloads.
public final class TGAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Techguns.MODID);

    private TGAttachments() {
    }
}
