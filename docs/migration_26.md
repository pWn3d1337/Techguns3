# Migration notes for 26.x

Running list of every place that uses an API expected to change on the jump from 1.21.1 to 26.x.
Each entry matches a `// MIGRATION(26.x): ...` comment in the code. See design doc §4.7.

| File | Note |
|---|---|
| `src/main/java/techguns/TGIds.java` | `ResourceLocation` renamed `Identifier` in newer Mojang mappings — only this class creates identifiers. |
| `src/main/java/techguns/registry/TGAttachments.java` | Newer NeoForge syncs attachments automatically; replace manual payload sync. |
| `src/main/java/techguns/client/particle/ClientFx.java` | FX and late-entity pass draw from `RenderLevelStageEvent` (AFTER_WEATHER, camera-relative identity pose stack); replace with the new render pipeline hooks. |
| `src/main/java/techguns/client/render/entity/RenderLateEntityRenderer.java` | Entity queue drained in the level render stage hook; becomes an extraction/submit step with entity render states. |
| `src/main/java/techguns/client/gun/GunAmmoDecorator.java` | Deferred GUI rendering (1.21.6) may change the item decorator API. |
| `src/main/java/techguns/client/deatheffects/DeathEffects.java` | Gore gibs read entity model parts (`ListModel`, `AgeableListModel` via reflection, `HierarchicalModel` root children); entity models are reworked in 1.21.2+. Corpse hiding via `RenderLivingEvent.Pre` moves to render states. |
| `src/main/resources/META-INF/accesstransformer.cfg` | `ModelPart.children` made public for gore gibs. |
