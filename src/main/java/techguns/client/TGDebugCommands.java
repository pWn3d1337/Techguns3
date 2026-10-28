package techguns.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import techguns.client.gun.ClientGunController;
import techguns.client.model.TGGeoBridge;
import techguns.client.model.TGGeoExporter;
import techguns.client.model.gun.ModelSawedOff;
import techguns.client.model.gun.ModelChainsaw;
import techguns.client.model.gun.ModelGrapplingHook;
import techguns.client.model.gun.ModelMiningDrill;
import techguns.client.model.gun.ModelPowerHammer;
import techguns.client.model.gun.ModelShishkebap;
import techguns.client.model.gun.ModelCombatShotgun;
import techguns.client.model.gun.ModelAS50;
import techguns.client.model.gun.ModelGaussRifle;
import techguns.client.model.gun.ModelBlasterRifle;
import techguns.client.model.gun.ModelLasergun2;
import techguns.client.model.gun.ModelPulseRifle;
import techguns.client.model.gun.ModelPDW;
import techguns.client.model.gun.ModelLaserPistol;
import techguns.client.model.gun.ModelBiogun;
import techguns.client.model.gun.ModelFlamethrower;
import techguns.client.model.gun.ModelHandgun;
import techguns.client.model.gun.ModelMagicRifle;
import techguns.client.model.gun.ModelTFG;
import techguns.client.model.gun.ModelNDR;
import techguns.client.model.gun.ModelTeslaGun;
import techguns.client.model.gun.ModelAlienBlaster;
import techguns.client.model.gun.ModelMibGun;
import techguns.client.model.gun.ModelNetherBlaster;
import techguns.client.model.gun.ModelSonicShotgun;
import techguns.client.model.gun.ModelGrimReaper;
import techguns.client.model.gun.ModelGuidedMissileLauncher;
import techguns.client.model.gun.*;
import techguns.client.model.gun.ModelM4;
import techguns.client.model.gun.ModelRocketLauncher;
import techguns.client.particle.ClientFx;
import techguns.client.particle.TGParticleManager;
import techguns.client.render.entity.RenderLateEntityRenderer;

/**
 * Development-only client commands for automated testing (agents can't hold the mouse button via MCP).
 * Not registered in production builds.
 */
public final class TGDebugCommands {
    private TGDebugCommands() {
    }

    public static void register(RegisterClientCommandsEvent event) {
        if (FMLEnvironment.production) {
            return;
        }
        event.getDispatcher().register(Commands.literal("tgdebug")
                .then(Commands.literal("jei")
                        .then(Commands.argument("category", StringArgumentType.word())
                                .executes(ctx -> {
                                    // opens the recipe viewer category for screenshots (no mouse via MCP)
                                    String id = StringArgumentType.getString(ctx, "category");
                                    Minecraft.getInstance().tell(() -> Minecraft.getInstance().player.displayClientMessage(Component.literal(
                                            RecipeViewerHook.showCategory(id) ? "Showing " + id : "No recipe viewer category " + id), false));
                                    return 1;
                                })))
                .then(Commands.literal("fire")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 200))
                                .executes(ctx -> {
                                    int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
                                    ClientGunController.get().holdFireFor(ticks);                                    ctx.getSource().sendSystemMessage(Component.literal("Holding fire for " + ticks + " ticks"));
                                    return ticks;
                                })))
                .then(Commands.literal("aim")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 400))
                                .executes(ctx -> {
                                    // holds the use key (lock-on guns lock while it is held)
                                    int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
                                    ClientGunController.get().holdUseFor(ticks);
                                    ctx.getSource().sendSystemMessage(Component.literal("Holding use for " + ticks + " ticks"));
                                    return ticks;
                                })))
                .then(Commands.literal("projectiles")
                        .executes(ctx -> {
                            var level = net.minecraft.client.Minecraft.getInstance().level;
                            int count = 0;
                            if (level != null) {
                                for (var entity : level.entitiesForRendering()) {
                                    if (entity instanceof techguns.entity.projectile.GenericProjectile projectile) {
                                        count++;
                                        String extra = projectile instanceof techguns.entity.projectile.GenericBeamProjectile beam
                                                ? " length=" + String.format("%.1f", beam.length()) : "";
                                        ctx.getSource().sendSystemMessage(Component.literal(entity.getType().toShortString()
                                                + " age=" + entity.tickCount + " removed=" + entity.isRemoved()
                                                + " pos=" + entity.blockPosition().toShortString() + extra));
                                    }
                                }
                            }
                            ctx.getSource().sendSystemMessage(Component.literal(count + " Techguns projectiles on the client"));
                            return count;
                        }))
                .then(Commands.literal("slowbeams")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(0, 1200))
                                .executes(ctx -> {
                                    // works in singleplayer: client and integrated server share the static
                                    techguns.entity.projectile.GenericBeamProjectile.debugLifetimeOverride = IntegerArgumentType.getInteger(ctx, "ticks");
                                    return 1;
                                })))
                .then(Commands.literal("zoom")
                        .executes(ctx -> {
                            ClientGunController.get().toggleZoomForHeldGun();
                            return 1;
                        }))
                .then(Commands.literal("fx")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    // spawns a particle system where the crosshair hits, bypassing the network
                                    Minecraft mc = Minecraft.getInstance();
                                    if (mc.level == null || mc.hitResult == null) {
                                        return 0;
                                    }
                                    Vec3 pos = mc.hitResult.getLocation();
                                    String name = StringArgumentType.getString(ctx, "name");
                                    ClientFx.createFX(name, mc.level, pos.x, pos.y, pos.z, 0, 0, 0, 0f, 0f, 1.0f);
                                    ctx.getSource().sendSystemMessage(Component.literal("Spawned " + name + " at " + pos));
                                    return 1;
                                })))
                .then(Commands.literal("geo")
                        .executes(ctx -> {
                            // Q3 prototype: switch the M4 between the GeckoLib geo model and the Java model
                            TGGeoBridge.setEnabled(!TGGeoBridge.enabled());
                            ctx.getSource().sendSystemMessage(Component.literal("Geo models: " + TGGeoBridge.enabled()));
                            return 1;
                        }))
                .then(Commands.literal("reloadanim")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 1200))
                                .executes(ctx -> {
                                    // plays the held gun's reload animation slowed down, without touching ammo
                                    var player = Minecraft.getInstance().player;
                                    if (player != null) {
                                        ClientGunController.get().onReloadStarted(player, net.minecraft.world.InteractionHand.MAIN_HAND,
                                                IntegerArgumentType.getInteger(ctx, "ticks"));
                                        ctx.getSource().sendSystemMessage(Component.literal("Playing reload animation"));
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("exportgeo")
                        .executes(ctx -> {
                            // writes run/geo_export/<id>.geo.json from the Java gun models (copy to assets/techguns/geo/item)
                            try {
                                var dir = Minecraft.getInstance().gameDirectory.toPath().resolve("geo_export");
                                TGGeoExporter.export(new ModelM4(), "m4",
                                        Map.of("magazine", List.of("Magazine01", "Magazine02"), "bolt", List.of("Bolt01", "Bolt02", "Bolt03")),
                                        Set.of("RedDot"), dir);
                                TGGeoExporter.export(new ModelLasergun(), "lasergun", Map.of(), Set.of("Glow1", "Glow2", "Glow3", "Glow5"), dir);
                                ModelRocketLauncher launcher = new ModelRocketLauncher();
                                TGGeoExporter.export(launcher, "rocket_launcher", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(launcher.partModel(1), "rocket_launcher_rocket", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelRevolver(), "revolver", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelGoldenRevolver(), "golden_revolver", Map.of(), Set.of(), dir);
                                ModelPistol pistol = new ModelPistol();
                                TGGeoExporter.export(pistol, "pistol", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(pistol.partModel(1), "pistol_1", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelAK(), "ak47", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelM4Infiltrator(), "m4_infiltrator", Map.of(), Set.of(), dir);
                                ModelAUG aug = new ModelAUG();
                                TGGeoExporter.export(aug, "aug", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(aug.partModel(1), "aug_1", Map.of(), Set.of(), dir);
                                ModelScar scar = new ModelScar();
                                TGGeoExporter.export(scar, "scar", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(scar.partModel(1), "scar_1", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelThompson(), "thompson", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelMac10(), "mac10", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelVector(), "vector", Map.of(), Set.of("RedDot"), dir);
                                TGGeoExporter.export(new ModelLMG(), "lmg", Map.of(), Set.of("RedDot"), dir);
                                TGGeoExporter.export(new ModelBoltaction(), "bolt_action", Map.of(), Set.of(), dir);
                                ModelMinigun minigun = new ModelMinigun();
                                TGGeoExporter.export(minigun, "minigun", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(minigun.partModel(1), "minigun_1", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelSawedOff(), "sawed_off", Map.of(), Set.of(), dir);
                                ModelCombatShotgun combatShotgun = new ModelCombatShotgun();
                                TGGeoExporter.export(combatShotgun, "combat_shotgun", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(combatShotgun.partModel(1), "combat_shotgun_1", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelAS50(), "as50", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelGaussRifle(), "gauss_rifle", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelBlasterRifle(), "blaster_rifle", Map.of(), Set.of(), dir);
                                ModelLasergun2 blasterShotgun = new ModelLasergun2();
                                TGGeoExporter.export(blasterShotgun, "blaster_shotgun", Map.of(), Set.of("HoloPlane"), dir);
                                TGGeoExporter.export(blasterShotgun.partModel(1), "blaster_shotgun_1", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelPulseRifle(), "pulse_rifle", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelPDW(), "pdw", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelLaserPistol(), "laser_pistol", Map.of(), Set.of(), dir);
                                var ndr = new ModelNDR();
                                TGGeoExporter.export(ndr, "nuclear_deathray", Map.of(), Set.of("GLOW"), dir);
                                TGGeoExporter.export(ndr.partModel(1), "nuclear_deathray_1", Map.of(), Set.of("Ring1", "Ring2", "Ring3", "Ring4", "Ring5", "Ring6", "Ring7", "Ring8"), dir);
                                TGGeoExporter.export(new ModelTeslaGun(), "tesla_gun", Map.of(), Set.of("Glow1", "Glow2", "Glow3", "Glow4", "Glow5", "Glow6", "Glow7", "Glow8", "RedDot"), dir);
                                TGGeoExporter.export(new ModelHandgun(), "hand_cannon", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelBiogun(), "biogun", Map.of(), Set.of("Glow1", "Glow2", "Glow3", "Glow4", "Glow5"), dir);
                                TGGeoExporter.export(new ModelTFG(), "tfg", Map.of(), Set.of("R03", "L03", "Glow01", "Glow02", "Glow03", "Glow_Barrel"), dir);
                                TGGeoExporter.export(new ModelFlamethrower(), "flamethrower", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelMagicRifle(), "magic_rifle", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelAlienBlaster(), "alien_blaster", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelMibGun(), "mib_gun", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelNetherBlaster(), "nether_blaster", Map.of(), Set.of("Glow1", "Glow2", "Glow3", "Glow4"), dir);
                                TGGeoExporter.export(new ModelSonicShotgun(), "sonic_shotgun", Map.of(), Set.of("GLOW01"), dir);
                                ModelGuidedMissileLauncher guidedMissile = new ModelGuidedMissileLauncher();
                                TGGeoExporter.export(guidedMissile, "guided_missile_launcher", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(guidedMissile.partModel(1), "guided_missile_launcher_1", Map.of(), Set.of("Holo2", "Holo2_1"), dir);
                                TGGeoExporter.export(new ModelGrimReaper(), "grim_reaper", Map.of(), Set.of(), dir);
                                ModelChainsaw chainsaw = new ModelChainsaw();
                                TGGeoExporter.export(chainsaw, "chainsaw", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(chainsaw.partModel(1), "chainsaw_1", Map.of(), Set.of(), dir);
                                ModelMiningDrill miningDrill = new ModelMiningDrill();
                                TGGeoExporter.export(miningDrill, "mining_drill", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(miningDrill.partModel(1), "mining_drill_1", Map.of(), Set.of(), dir);
                                ModelPowerHammer powerHammer = new ModelPowerHammer();
                                TGGeoExporter.export(powerHammer, "power_hammer", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(powerHammer.partModel(1), "power_hammer_1", Map.of(), Set.of(), dir);
                                ModelGrapplingHook grapplingHook = new ModelGrapplingHook();
                                TGGeoExporter.export(grapplingHook, "grappling_hook", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(grapplingHook.partModel(1), "grappling_hook_1", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(grapplingHook.partModel(2), "grappling_hook_2", Map.of(), Set.of(), dir);
                                TGGeoExporter.export(new ModelShishkebap(), "shishkebap", Map.of(), Set.of(), dir);
                                ctx.getSource().sendSystemMessage(Component.literal("Exported gun geo models to " + dir));
                            } catch (java.io.IOException e) {
                                ctx.getSource().sendSystemMessage(Component.literal("Export failed: " + e));
                            }
                            return 1;
                        }))
                .then(Commands.literal("bench")
                        .then(Commands.argument("gun", StringArgumentType.word())
                                .then(Commands.argument("shots", IntegerArgumentType.integer(1, 100))
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(1, 120))
                                                .executes(ctx -> TGBenchmark.start(StringArgumentType.getString(ctx, "gun"),
                                                        IntegerArgumentType.getInteger(ctx, "shots"),
                                                        IntegerArgumentType.getInteger(ctx, "seconds")) ? 1 : 0)))))
                .then(Commands.literal("key")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    // presses a Techguns key once (MCP has no key input): safe_mode, step_assist, night_vision, jetpack, force_reload
                                    String name = StringArgumentType.getString(ctx, "name");
                                    net.minecraft.client.KeyMapping key = switch (name) {
                                        case "safe_mode" -> TGKeyMappings.TOGGLE_SAFE_MODE;
                                        case "step_assist" -> TGKeyMappings.TOGGLE_STEP_ASSIST;
                                        case "night_vision" -> TGKeyMappings.TOGGLE_NIGHT_VISION;
                                        case "jetpack" -> TGKeyMappings.TOGGLE_JETPACK;
                                        case "force_reload" -> TGKeyMappings.FORCE_RELOAD;
                                        default -> null;
                                    };
                                    if (key == null) {
                                        ctx.getSource().sendSystemMessage(Component.literal("Unknown key " + name));
                                        return 0;
                                    }
                                    net.minecraft.client.KeyMapping.click(key.getKey());
                                    return 1;
                                })))
                .then(Commands.literal("stats")
                        .executes(ctx -> {
                            // the client's view of the held gun or armor piece, to check datapack overrides and their sync
                            net.minecraft.world.item.ItemStack held = Minecraft.getInstance().player.getMainHandItem();
                            String text;
                            if (held.getItem() instanceof techguns.gun.GunItem gun) {
                                var stats = gun.stats();
                                text = String.format(java.util.Locale.ROOT, "%s: damage %.1f (min %.1f), clip %d, fire delay %d, reload %d, speed %.2f, spread %.3f%s",
                                        held.getHoverName().getString(), stats.damage().damage(), stats.damage().damageMin(),
                                        stats.firing().clipSize(), stats.firing().fireDelay(), stats.firing().reloadTicks(),
                                        stats.projectile().speed(), stats.projectile().spread(),
                                        stats == gun.defaultStats() ? "" : " (override)");
                            } else if (held.getItem() instanceof techguns.armor.TGArmorItem armor) {
                                StringBuilder out = new StringBuilder(held.getHoverName().getString() + ":");
                                for (var category : techguns.core.damage.TGDamageCategory.values()) {
                                    if (category.isResistable()) {
                                        out.append(String.format(java.util.Locale.ROOT, " %s %.2f", category.id(), armor.armorValue(category)));
                                    }
                                }
                                out.append(String.format(java.util.Locale.ROOT, ", toughness %.1f%s", armor.toughness(),
                                        techguns.stats.StatOverrides.armor(armor) == null ? "" : " (override)"));
                                text = out.toString();
                            } else {
                                text = "Hold a Techguns gun or armor piece";
                            }
                            ctx.getSource().sendSystemMessage(Component.literal(text));
                            return 1;
                        }))
                .then(Commands.literal("teams")
                        .executes(ctx -> {
                            // runs every team source on the integrated server: you vs yourself and vs a stranger
                            var server = Minecraft.getInstance().getSingleplayerServer();
                            var self = Minecraft.getInstance().player.getUUID();
                            if (server == null) {
                                ctx.getSource().sendSystemMessage(Component.literal("Needs the integrated server"));
                                return 0;
                            }
                            server.execute(() -> {
                                StringBuilder out = new StringBuilder("Team sources (active: " + techguns.compat.team.TGTeams.hasTeams() + "):");
                                var stranger = java.util.UUID.randomUUID();
                                for (var provider : techguns.compat.team.TGTeams.providers()) {
                                    try {
                                        out.append(' ').append(provider.getClass().getSimpleName()).append(" self member ")
                                                .append(provider.isMember(self, self)).append(", stranger member/ally/enemy ")
                                                .append(provider.isMember(self, stranger)).append('/').append(provider.isAlly(self, stranger))
                                                .append('/').append(provider.isEnemy(self, stranger)).append(';');
                                    } catch (RuntimeException | LinkageError e) {
                                        out.append(' ').append(provider.getClass().getSimpleName()).append(" FAILED ").append(e).append(';');
                                    }
                                }
                                Minecraft.getInstance().tell(() -> Minecraft.getInstance().player.displayClientMessage(
                                        Component.literal(out.toString()), false));
                            });
                            return 1;
                        }))
                .then(Commands.literal("view")
                        .executes(ctx -> {
                            // cycles first person / third person back / third person front (F5); MCP has no key input
                            var options = Minecraft.getInstance().options;
                            options.setCameraType(options.getCameraType().cycle());
                            ctx.getSource().sendSystemMessage(Component.literal("Camera: " + options.getCameraType()));
                            return 1;
                        }))
                .then(Commands.literal("alwaysdeath")
                        .executes(ctx -> {
                            // works in singleplayer: client and integrated server share the static
                            techguns.damage.TGDamageEvents.debugAlwaysDeathEffect = !techguns.damage.TGDamageEvents.debugAlwaysDeathEffect;
                            ctx.getSource().sendSystemMessage(Component.literal("Always play death effects: "
                                    + techguns.damage.TGDamageEvents.debugAlwaysDeathEffect));
                            return 1;
                        }))
                .then(Commands.literal("fxstats")
                        .executes(ctx -> {
                            ctx.getSource().sendSystemMessage(Component.literal(TGParticleManager.get().debugStats()
                                    + " " + RenderLateEntityRenderer.debugStats()));
                            return 1;
                        })));
    }
}
