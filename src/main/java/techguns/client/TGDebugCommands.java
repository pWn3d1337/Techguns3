package techguns.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import techguns.client.gun.ClientGunController;
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
                .then(Commands.literal("fire")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 200))
                                .executes(ctx -> {
                                    int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
                                    ClientGunController.get().holdFireFor(ticks);                                    ctx.getSource().sendSystemMessage(Component.literal("Holding fire for " + ticks + " ticks"));
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
