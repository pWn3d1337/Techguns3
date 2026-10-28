package techguns.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import techguns.TGIds;
import techguns.Techguns;
import techguns.client.particle.TGParticleManager;
import techguns.gun.FirePos;
import techguns.gun.GunItem;

/**
 * Development-only load test ({@code /tgdebug bench <gun> <shots per tick> <seconds>}): the player's integrated
 * server fires the gun {@code shots per tick} times every tick along the player's view (NPC fire path, wide spread)
 * and the server tick time, the client frame time and the peak projectile and particle counts are reported for a
 * quiet phase before, the firing phase and a phase after (impacts and trails fading). Singleplayer only.
 */
public final class TGBenchmark {
    private static final int QUIET_TICKS = 100;
    /** Spread multiplier on the gun's zoomed spread, so the shots fan out over the target area. */
    private static final float SPREAD_SCALE = 4.0f;

    private enum Phase { BEFORE, FIRING, AFTER }

    private static boolean registered;
    private static Run run;

    private TGBenchmark() {
    }

    /** Starts a run; false (with a reason sent to chat) when it cannot. */
    public static boolean start(String gunId, int shotsPerTick, int seconds) {
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            message("Needs the integrated server");
            return false;
        }
        if (run != null) {
            message("A benchmark is already running");
            return false;
        }
        if (!(BuiltInRegistries.ITEM.get(gunId.contains(":") ? TGIds.parse(gunId) : TGIds.id(gunId)) instanceof GunItem gun)) {
            message("No gun " + gunId);
            return false;
        }
        if (!registered) {
            registered = true;
            NeoForge.EVENT_BUS.addListener(TGBenchmark::onServerTickPre);
            NeoForge.EVENT_BUS.addListener(TGBenchmark::onServerTickPost);
            NeoForge.EVENT_BUS.addListener(TGBenchmark::onClientTick);
            NeoForge.EVENT_BUS.addListener(TGBenchmark::onRenderFrame);
        }
        run = new Run(gun, mc.player.getUUID(), shotsPerTick, seconds * 20);
        message("Benchmark " + gunId + ": " + shotsPerTick + " shots/tick for " + seconds + " s (quiet "
                + QUIET_TICKS / 20 + " s before and after)");
        return true;
    }

    // --- server thread --------------------------------------------------------------------------------------------

    private static void onServerTickPre(ServerTickEvent.Pre event) {
        Run r = run;
        if (r == null) {
            return;
        }
        r.tickStart = System.nanoTime();
        if (r.phase() == Phase.FIRING) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(r.player);
            if (player != null) {
                ItemStack stack = new ItemStack(r.gun);
                for (int i = 0; i < r.shotsPerTick; i++) {
                    r.gun.fireFromNpc(player, stack, 1.0f, SPREAD_SCALE, FirePos.CENTER);
                }
            }
        }
    }

    private static void onServerTickPost(ServerTickEvent.Post event) {
        Run r = run;
        if (r == null || r.tickStart == 0L) {
            return;
        }
        r.stats(r.phase()).serverTicks.add(System.nanoTime() - r.tickStart);
        if (r.serverTick % 10 == 0) {
            int projectiles = 0;
            for (ServerLevel level : event.getServer().getAllLevels()) {
                projectiles += countProjectiles(level.getAllEntities());
            }
            Stats s = r.stats(r.phase());
            s.serverProjectiles = Math.max(s.serverProjectiles, projectiles);
        }
        r.serverTick++;
    }

    // --- client thread --------------------------------------------------------------------------------------------

    private static void onClientTick(ClientTickEvent.Post event) {
        Run r = run;
        Minecraft mc = Minecraft.getInstance();
        if (r == null || mc.level == null) {
            return;
        }
        Stats s = r.stats(r.phase());
        s.clientProjectiles = Math.max(s.clientProjectiles, countProjectiles(mc.level.entitiesForRendering()));
        s.particles = Math.max(s.particles, TGParticleManager.get().particleCount());
        s.systems = Math.max(s.systems, TGParticleManager.get().systemCount());
        if (r.serverTick >= r.totalTicks()) {
            run = null;
            message(r.report());
        }
    }

    private static void onRenderFrame(RenderFrameEvent.Pre event) {
        Run r = run;
        if (r == null) {
            return;
        }
        long now = System.nanoTime();
        if (r.lastFrame != 0L) {
            r.stats(r.phase()).frames.add(now - r.lastFrame);
        }
        r.lastFrame = now;
    }

    private static int countProjectiles(Iterable<Entity> entities) {
        int count = 0;
        for (Entity entity : entities) {
            if (entity instanceof Projectile
                    && Techguns.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace())) {
                count++;
            }
        }
        return count;
    }

    private static void message(String text) {
        Minecraft.getInstance().tell(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal(text), false);
            }
        });
        Techguns.LOGGER.info("[bench] {}", text);
    }

    private static final class Stats {
        final List<Long> serverTicks = Collections.synchronizedList(new ArrayList<>());
        final List<Long> frames = new ArrayList<>();
        volatile int serverProjectiles;
        int clientProjectiles;
        int particles;
        int systems;

        String report(String name) {
            return String.format(java.util.Locale.ROOT, "%s: server tick avg %.2f / p95 %.2f / max %.2f ms, frame avg %.2f / p95 %.2f ms (%d frames), "
                            + "peak projectiles server %d / client %d, peak particles %d in %d systems",
                    name, avgMs(serverTicks), percentileMs(serverTicks, 0.95), percentileMs(serverTicks, 1.0),
                    avgMs(frames), percentileMs(frames, 0.95), frames.size(), serverProjectiles, clientProjectiles, particles, systems);
        }

        private static double avgMs(List<Long> values) {
            synchronized (values) {
                return values.stream().mapToLong(Long::longValue).average().orElse(0) / 1.0e6;
            }
        }

        private static double percentileMs(List<Long> values, double p) {
            List<Long> sorted;
            synchronized (values) {
                sorted = new ArrayList<>(values);
            }
            if (sorted.isEmpty()) {
                return 0;
            }
            Collections.sort(sorted);
            return sorted.get((int) Math.min(sorted.size() - 1, Math.round(p * (sorted.size() - 1)))) / 1.0e6;
        }
    }

    private static final class Run {
        final GunItem gun;
        final java.util.UUID player;
        final int shotsPerTick;
        final int firingTicks;
        final Stats before = new Stats();
        final Stats firing = new Stats();
        final Stats after = new Stats();
        volatile int serverTick;
        long tickStart;
        long lastFrame;

        Run(GunItem gun, java.util.UUID player, int shotsPerTick, int firingTicks) {
            this.gun = gun;
            this.player = player;
            this.shotsPerTick = shotsPerTick;
            this.firingTicks = firingTicks;
        }

        int totalTicks() {
            return QUIET_TICKS + firingTicks + QUIET_TICKS;
        }

        Phase phase() {
            int t = serverTick;
            return t < QUIET_TICKS ? Phase.BEFORE : t < QUIET_TICKS + firingTicks ? Phase.FIRING : Phase.AFTER;
        }

        Stats stats(Phase phase) {
            return switch (phase) {
                case BEFORE -> before;
                case FIRING -> firing;
                case AFTER -> after;
            };
        }

        String report() {
            return "Benchmark " + BuiltInRegistries.ITEM.getKey(gun).getPath() + " x" + shotsPerTick + " | "
                    + before.report("before") + " | " + firing.report("firing") + " | " + after.report("after");
        }
    }
}
