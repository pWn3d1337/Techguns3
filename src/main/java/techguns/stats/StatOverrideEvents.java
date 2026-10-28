package techguns.stats;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import techguns.network.TGNetwork;

/** Registers the stat override loaders and sends the overrides to joining players and after {@code /reload}. */
public final class StatOverrideEvents {
    private StatOverrideEvents() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener((AddReloadListenerEvent event) -> {
            event.addListener(new StatOverrideLoader.Guns());
            event.addListener(new StatOverrideLoader.Armor());
        });
        gameBus.addListener((OnDatapackSyncEvent event) -> event.getRelevantPlayers().forEach(TGNetwork::syncStatOverrides));
    }
}
