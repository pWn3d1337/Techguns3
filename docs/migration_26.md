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
| `src/main/java/techguns/client/render/geo/GeckoLibGunRenderer.java` | GeckoLib 4 `GeoObjectRenderer`/`AnimationController` API; GeckoLib 5 (1.21.2+) uses render states — rewrite the bridge. |
| `src/main/java/techguns/client/gui/LockOnOverlay.java` | GUI layers (`RegisterGuiLayersEvent`) and `GuiGraphics.blit` change with the new GUI render pipeline. |
| `src/main/java/techguns/client/render/MiningPlaneOutline.java` | Drill plane outline via `RenderHighlightEvent.Block` and `RenderType.lines()`; block highlight rendering moves to render states in 1.21.2+. |
| `src/main/java/techguns/item/AmmoBagItem.java`, `AmmoBagContents.java`, `client/gui/ClientAmmoBagTooltip.java` | Bundle-like item (`overrideStackedOnOther`, `TooltipComponent`); bundles are reworked in 1.21.2+ (bundle selection, new tooltip). |
| `src/main/java/techguns/client/render/item/TGShieldRenderer.java`, `client/TGShieldBlocking.java` | Shield BEWLR and the `minecraft:blocking` item property; item properties are replaced by item model definitions in 1.21.4. |
| `src/main/java/techguns/slot/menu/ExtraSlotsMenu.java`, `registry/TGMenus.java`, `client/gui/ExtraSlotsScreen.java` | Fallback extra slot menu/screen: `AbstractContainerMenu`/`Slot`/`MenuType` constructors, `GuiGraphics` fills/blit, `InventoryScreen.renderEntityInInventoryFollowsMouse` and `ScreenEvent.Init.Post` button injection change in 1.21.2+ (new GUI render pipeline). |
| `src/main/java/techguns/client/render/slot/SlotGearLayer.java`, `client/model/slot/*` | Player render layer for extra-slot gear; render layers move to render states in 1.21.2+. |
| `src/main/java/techguns/compat/curios/CuriosExtraSlots.java` | Curios API (`ICuriosItemHandler`, stacks handlers) differs per Curios/MC version. |
| `src/main/java/techguns/machine/TGMachineMenu.java`, `client/gui/machine/*`, `registry/TGMenus.java` | Machine menus/screens: `AbstractContainerMenu`, `SlotItemHandler`, `IMenuTypeExtension`, `GuiGraphics.blit` change with the 1.21.2+ menu API and the 1.21.6 GUI render pipeline. |
| `src/main/java/techguns/client/render/block/*`, `client/model/machine/*` | Machine block entity renderers and `ModelPart` layer definitions; render states (1.21.2) and submit nodes (1.21.9) replace `BlockEntityRenderer#render`; the machine item BEWLR becomes a special item model renderer (1.21.4). |
| `src/main/java/techguns/registry/TGBlockEntities.java` | `BlockEntityType.Builder` replaced by the `BlockEntityType` constructor in 1.21.2+. |
