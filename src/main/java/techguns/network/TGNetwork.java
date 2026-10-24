package techguns.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers all Techguns payloads. All payload types live in this package.
 */
public final class TGNetwork {
    /** Bump when payloads change incompatibly. */
    public static final String PROTOCOL_VERSION = "1";

    private TGNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
    }
}
