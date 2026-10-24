package techguns.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import techguns.client.gun.ClientGunController;

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
                        })));
    }
}
