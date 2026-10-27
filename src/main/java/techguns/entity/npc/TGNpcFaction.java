package techguns.entity.npc;

/**
 * Techguns 2 {@code TGNpcFaction} and {@code TGNpcFactions} hostility table: hostile NPCs don't fight each other,
 * turrets and neutrals stay peaceful with each other, hostiles attack both.
 */
public enum TGNpcFaction {
    HOSTILE, TURRET, NEUTRAL;

    public boolean isHostileTo(TGNpcFaction other) {
        return (this == HOSTILE) != (other == HOSTILE);
    }
}
