# Gun and armor stat overrides (datapacks)

Modpacks and servers can change gun and armor numbers with a datapack, no code needed. This replaces the
Techguns 2 CraftTweaker `mods.techguns.GunStats` / `ArmorStats` tweakers.

- Gun stats: `data/<namespace>/techguns/gun_stats/<item>.json`
- Armor stats: `data/<namespace>/techguns/armor_stats/<item>.json`

The file name is the item id: `data/techguns/techguns/gun_stats/m4.json` changes `techguns:m4`. A file only
lists what it changes; everything left out keeps the built-in value. The server loads the files at start and on
`/reload` and sends them to every client, so tooltips, fire rate and zoom match. Broken files are skipped with a
warning in the server log (`Gun stat override ... skipped: ...`). In a dev client, `/tgdebug stats` prints the stats
of the held gun or armor piece as the client sees them.

## Guns

Every field of a gun's stats can be overridden; nested objects merge field by field.

```json
{
  "firing": { "fire_delay": 2, "clip_size": 40, "reload_ticks": 40 },
  "damage": { "damage": 10.0, "damage_min": 7.0 },
  "projectile": { "spread": 0.03 }
}
```

| Object | Fields |
|---|---|
| `firing` | `semi_auto` (one shot per click), `fire_delay` (ticks between shots), `clip_size`, `reload_ticks`, `ammo_per_reload` (shells per reload for shotguns), `recoil_ticks`, `muzzle_flash_ticks`, `silenced`, `continuous_animation` |
| `damage` | `damage` (close range), `damage_min` (beyond `drop_end`), `drop_start`, `drop_end` (blocks; damage falls linearly in between), `penetration` (armor penetration per piece), `radius` (explosion or area radius) |
| `projectile` | `speed` (blocks per tick), `range` (blocks), `spread`, `gravity`, `forward_offset`, `pellets` (extra projectiles per shot), `pellet_spread`, `burst` (extra projectiles in a line instead of a cone) |
| `zoom` | `fov_multiplier`, `toggle`, `spread_multiplier`, `fire_centered`; guns without a scope get one by adding `zoom` |
| `ai` | NPC use: `attack_range`, `attack_time`, `burst_count`, `burst_attack_time` |
| top level | `hand_type` (`one_handed`, `two_handed`, `one_point_five_handed`; a two-handed gun blocks the off hand), `hold_type` (`crossbow`, `bow`, `lowered`), `range_tooltip` (`drop`, `no_drop`, `radius`: how the tooltip shows `drop_start`/`drop_end`) |

NPCs use the same guns, so an override changes them too. A changed `clip_size` applies to the next reload.

## Armor

```json
{
  "armor": { "physical": 6.0, "projectile": 6.0, "fire": 4.5 },
  "toughness": 2.0,
  "bonuses": { "techguns:speed_bonus": 0.1, "techguns:jump_bonus": 0.0, "minecraft:generic.luck": 1.0 },
  "unpowered_bonuses": { "techguns:speed_bonus": 0.02 }
}
```

- `armor`: this piece's armor against a damage category (`physical`, `projectile`, `fire`, `explosion`,
  `energy`, `poison`, `ice`, `lightning`, `radiation`, `dark`) — the values the tooltip shows.
- `toughness`: vanilla armor toughness of this piece.
- `bonuses`: attribute bonuses of the piece; listed attributes are replaced, `0` removes one, any attribute id
  works. Techguns bonuses: `techguns:speed_bonus`, `water_speed_bonus`, `jump_bonus`, `fall_damage_reduction`,
  `fall_free_height`, `mining_speed_bonus`, `water_mining_speed_bonus`, `gun_accuracy`, `cooling_system`,
  `step_assist`, `night_vision`, `fly_speed_bonus`, `oxygen_gear`, `radiation_resistance`.
- `unpowered_bonuses`: powered armor only, the bonuses while it has no power.

Durability can't be overridden (it is fixed when the item is registered). Worn armor picks up a changed override
when it is put on again.
