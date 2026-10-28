package techguns.compat.team;

import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;

/**
 * Open Parties and Claims: members of one party are members, allied parties are allies; OPAC has no enemies. Only
 * loaded with OPAC installed.
 */
// MIGRATION(26.x): OPAC server API; check the party manager access on the new version.
final class OpenPartiesProvider implements TeamProvider {
    @Override
    public boolean isMember(UUID a, UUID b) {
        IServerPartyAPI party = party(a);
        IServerPartyAPI other = party(b);
        return party != null && other != null && party.getId().equals(other.getId());
    }

    @Override
    public boolean isAlly(UUID a, UUID b) {
        IServerPartyAPI party = party(a);
        IServerPartyAPI other = party(b);
        return party != null && other != null && (party.getId().equals(other.getId()) || party.isAlly(other.getId()));
    }

    @Override
    public boolean isEnemy(UUID a, UUID b) {
        return false;
    }

    private static IServerPartyAPI party(UUID player) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : OpenPACServerAPI.get(server).getPartyManager().getPartyByMember(player);
    }
}
