package techguns.client.render.geo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.keyframe.event.data.CustomInstructionKeyframeData;
import software.bernie.geckolib.animation.keyframe.event.data.ParticleKeyframeData;
import software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData;
import techguns.TGIds;
import techguns.Techguns;
import techguns.client.model.GeoAnimInput;
import techguns.client.particle.ClientFx;

/**
 * Sound, particle and instruction keyframes of gun animations (Blockbench's "Sound", "Particle" and "Instructions"
 * timelines). Animations are sampled from the gun's timers ({@link GunAnimationSampler}), so an event fires when the
 * sampled time of its animation passes it: once per holder, hand, animation and run of the timer (recoil or reload),
 * however often the gun is drawn in between.
 * <ul>
 *     <li><b>Sound</b>: the sound event id ({@code techguns:} when no namespace is given), played at the holder for this
 *     client only. Gameplay sounds (shot, reload start) stay server-side; keyframe sounds are timed details on top.</li>
 *     <li><b>Particle</b>: a Techguns particle system ({@code TGFX} name, as {@code /tgdebug fx}); the locator is a marker
 *     bone ({@code muzzle}, {@code idle_fx}, ...) and defaults to {@code muzzle}. Without that bone the effect starts at
 *     the holder's eyes.</li>
 *     <li><b>Instructions</b>: {@code name args}, several separated by {@code ;}. Built in: {@code sound <id> [volume]
 *     [pitch]}. Others are registered with {@link #registerInstruction}.</li>
 * </ul>
 * Events need a running timer, so they don't fire for aim, charge or frozen {@code /tgdebug pose} draws. An event on the
 * very last frame may be missed: the timer can end between two frames.
 */
// MIGRATION(26.x): GeckoLib 5 changes the keyframe event data classes.
public final class GunAnimationEvents {
    /** Instruction handler: holder of the gun, its hand and the text after the instruction name. */
    @FunctionalInterface
    public interface Instruction {
        void run(Entity holder, InteractionHand hand, String args);
    }

    private static final String DEFAULT_LOCATOR = "muzzle";
    private static final long PRUNE_MS = 10_000L;

    private static final Map<String, Instruction> INSTRUCTIONS = new HashMap<>();
    private static final Map<Key, Run> RUNS = new HashMap<>();
    private static final Set<String> WARNED = new HashSet<>();
    private static long lastPrune;
    private static boolean debug;

    static {
        registerInstruction("sound", (holder, hand, args) -> {
            String[] parts = args.trim().split("\\s+");
            if (!parts[0].isEmpty()) {
                playSound(holder, parts[0], parts.length > 1 ? parseFloat(parts[1], 1f) : 1f, parts.length > 2 ? parseFloat(parts[2], 1f) : 1f);
            }
        });
    }

    private record Key(int holder, int hand, Animation animation) {
    }

    private static final class Run {
        long cycle;
        double tick;
        long used;
    }

    /** A particle keyframe of the current draw, spawned once the model is drawn and its marker bones are known. */
    record Particle(Entity holder, boolean firstPerson, String effect, String locator) {
    }

    private GunAnimationEvents() {
    }

    /** Registers a custom instruction keyframe ({@code name args}); replaces an existing one of that name. */
    public static void registerInstruction(String name, Instruction instruction) {
        INSTRUCTIONS.put(name, instruction);
    }

    /** Dev ({@code /tgdebug animevents}): prints every fired event to chat. Returns the new state. */
    public static boolean toggleDebug() {
        debug = !debug;
        return debug;
    }

    /**
     * Fires the events of {@code animation} that its sampled time passed since the last draw of this run ({@code cycle},
     * 0 = no running timer: nothing fires). Particles are added to {@code particles}.
     */
    static void advance(@Nullable GeoAnimInput.Events events, long cycle, Animation animation, float progress, List<Particle> particles) {
        if (events == null || cycle == 0L) {
            return;
        }
        Animation.Keyframes frames = animation.keyFrames();
        if (frames.sounds().length == 0 && frames.particles().length == 0 && frames.customInstructions().length == 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        Entity holder = level.getEntity(events.holder());
        // the local player's third person draw while the camera is in first person is the inventory screen's model
        if (holder == null || (!events.firstPerson() && holder == mc.player && mc.options.getCameraType().isFirstPerson())) {
            return;
        }
        long now = Util.getMillis();
        prune(now);
        Run run = RUNS.computeIfAbsent(new Key(events.holder(), events.hand(), animation), k -> new Run());
        double from = run.cycle == cycle ? run.tick : -1d;
        double to = progress * animation.length();
        run.used = now;
        if (to <= from) {
            return;
        }
        run.cycle = cycle;
        run.tick = to;

        InteractionHand hand = events.hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        for (SoundKeyframeData sound : frames.sounds()) {
            if (passed(sound.getStartTick(), from, to)) {
                log(animation, "sound", sound.getSound());
                playSound(holder, sound.getSound(), 1f, 1f);
            }
        }
        for (ParticleKeyframeData particle : frames.particles()) {
            if (passed(particle.getStartTick(), from, to)) {
                String locator = particle.getLocator() == null || particle.getLocator().isBlank() ? DEFAULT_LOCATOR : particle.getLocator();
                log(animation, "particle", particle.getEffect() + " @ " + locator);
                particles.add(new Particle(holder, events.firstPerson(), particle.getEffect(), locator));
            }
        }
        for (CustomInstructionKeyframeData instruction : frames.customInstructions()) {
            if (passed(instruction.getStartTick(), from, to)) {
                for (String line : instruction.getInstructions().split(";")) {
                    String text = line.trim();
                    if (!text.isEmpty()) {
                        log(animation, "instruction", text);
                        runInstruction(holder, hand, text);
                    }
                }
            }
        }
    }

    /**
     * Spawns the particle keyframes of a finished draw at their marker bone ({@code markers}: pose of each marker drawn,
     * camera-relative world space, or view space in first person).
     */
    static void spawnParticles(List<Particle> particles, Map<String, Matrix4f> markers) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        Camera camera = mc.gameRenderer.getMainCamera();
        for (Particle particle : particles) {
            Matrix4f marker = markers.get(particle.locator());
            Vec3 pos;
            if (marker != null) {
                Vector3f local = marker.transformPosition(new Vector3f());
                if (particle.firstPerson()) {
                    // view space to world directions, see GameRenderer#renderLevel
                    local.rotate(camera.rotation());
                }
                pos = camera.getPosition().add(local.x(), local.y(), local.z());
            } else {
                pos = particle.holder().getEyePosition();
            }
            if (debug && mc.player != null) {
                Vec3 eye = particle.holder().getEyePosition();
                mc.player.displayClientMessage(Component.literal(String.format(Locale.ROOT, "[anim] %s at %s, eye offset %.2f %.2f %.2f",
                        particle.effect(), marker != null ? particle.locator() : "eyes", pos.x - eye.x, pos.y - eye.y, pos.z - eye.z)), false);
            }
            ClientFx.createFX(particle.effect(), level, pos.x, pos.y, pos.z, 0, 0, 0, 0f, 0f, 1.0f);
        }
    }

    private static boolean passed(double tick, double from, double to) {
        return tick > from && tick <= to;
    }

    private static void runInstruction(Entity holder, InteractionHand hand, String text) {
        int space = text.indexOf(' ');
        String name = space < 0 ? text : text.substring(0, space);
        Instruction instruction = INSTRUCTIONS.get(name.toLowerCase(Locale.ROOT));
        if (instruction != null) {
            instruction.run(holder, hand, space < 0 ? "" : text.substring(space + 1));
        } else if (WARNED.add("instruction " + name)) {
            Techguns.LOGGER.warn("Unknown gun animation instruction '{}'", name);
        }
    }

    private static void playSound(Entity holder, String name, float volume, float pitch) {
        ResourceLocation id = name.indexOf(':') < 0 ? TGIds.id(name) : TGIds.tryParse(name);
        if (id == null) {
            if (WARNED.add("sound " + name)) {
                Techguns.LOGGER.warn("Invalid gun animation sound '{}'", name);
            }
            return;
        }
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(id);
        if (sound == null) {
            // a sound defined only in sounds.json (resource packs)
            sound = SoundEvent.createVariableRangeEvent(id);
        }
        holder.level().playLocalSound(holder.getX(), holder.getEyeY(), holder.getZ(), sound, holder.getSoundSource(), volume, pitch, false);
    }

    private static float parseFloat(String text, float fallback) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static void log(Animation animation, String kind, String text) {
        if (debug && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("[anim] " + animation.name() + " " + kind + ": " + text), false);
        }
    }

    /** Forgets runs of holders not drawn for a while (also old animation instances after a resource reload). */
    private static void prune(long now) {
        if (now - lastPrune > PRUNE_MS) {
            lastPrune = now;
            RUNS.values().removeIf(run -> now - run.used > PRUNE_MS);
        }
    }
}
