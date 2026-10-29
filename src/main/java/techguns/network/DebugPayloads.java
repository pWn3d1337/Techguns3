package techguns.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import techguns.TGIds;

/** The debug tools (Techguns 2 {@code buttonClicked} with a text): menu buttons that carry a template name. */
public final class DebugPayloads {
    /** Longest template name the screens send. */
    public static final int MAX_NAME = 64;

    private DebugPayloads() {
    }

    /** Client to server: a button of an open dungeon scanner or generator screen, with the template name typed in. */
    public record TemplateAction(int containerId, int action, String name) implements CustomPacketPayload {
        public static final Type<TemplateAction> TYPE = new Type<>(TGIds.id("debug_template_action"));
        public static final StreamCodec<ByteBuf, TemplateAction> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, TemplateAction::containerId,
                ByteBufCodecs.VAR_INT, TemplateAction::action, ByteBufCodecs.stringUtf8(MAX_NAME), TemplateAction::name, TemplateAction::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
