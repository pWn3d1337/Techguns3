package techguns.client.deatheffects;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import techguns.TGIds;
import techguns.Techguns;
import techguns.client.particle.ClientFx;
import techguns.client.particle.TGFX;
import techguns.client.particle.TGParticleManager;
import techguns.client.particle.TGParticleSystem;
import techguns.client.particle.TGParticleSystemType;
import techguns.core.damage.DeathType;
import techguns.entity.npc.AlienBug;
import techguns.registry.TGSounds;

/**
 * Client side of the death effects (Fabric {@code DeathEffectHandler}): gore bursts the dying entity into gibs made
 * of its own model parts, bio and laser deaths play their particle FX. The death type is kept per entity so the
 * corpse renderer can react to it.
 */
public final class DeathEffects {
    public static final ResourceLocation GORE_TEXTURE = TGIds.id("textures/entity/gore.png");

    /**
     * Death type of dying entities on the client; entries vanish with the entity. Entities are equal by id, and a
     * respawned player keeps its id: the entry remembers which object died, so the next (plain) death is not mistaken.
     */
    private static final Map<LivingEntity, Death> DEATH_TYPES = new WeakHashMap<>();

    private record Death(DeathType type, int identity) {
    }

    /** Gore settings by entity type; everything else uses {@link #GENERIC_GORE}. */
    private static final Map<EntityType<?>, GoreData> GORE_DATA = new HashMap<>();
    private static final GoreData GENERIC_GORE = new GoreData(160, 21, 31);
    /** Techguns 2: the alien bug bleeds yellow-green (a class check, the type isn't registered yet when this loads). */
    private static final GoreData ALIEN_BUG_GORE = new GoreData(235, 255, 70);

    static {
        GORE_DATA.put(EntityType.SKELETON, new GoreData(192, 192, 192).setFX("GoreFX_Dust", "GoreTrailFX_Dust").setSound(() -> SoundEvents.SKELETON_DEATH));
        GORE_DATA.put(EntityType.WITHER_SKELETON, new GoreData(64, 64, 64).setFX("GoreFX_Dust", "GoreTrailFX_Dust").setSound(() -> SoundEvents.WITHER_SKELETON_DEATH));
    }

    @Nullable
    private static List<ModelPart> genericGibs;

    private DeathEffects() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener(DeathEffects::onRenderLivingPre);
        gameBus.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> DEATH_TYPES.clear());
    }

    @Nullable
    public static DeathType deathType(LivingEntity entity) {
        Death death = DEATH_TYPES.get(entity);
        return death != null && death.identity() == System.identityHashCode(entity) ? death.type() : null;
    }

    /** Hides the corpse of gored entities (their gibs fly as particles), dissolves laser and bio corpses. */
    // MIGRATION(26.x): entity rendering moves to render states; check how to replace a dying entity's render there.
    private static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity.deathTime <= 0) {
            return;
        }
        DeathType type = deathType(entity);
        if (type == DeathType.GORE) {
            event.setCanceled(true);
        } else if (type == DeathType.LASER || type == DeathType.BIO) {
            event.setCanceled(true);
            CorpseDissolveRenderer.render(entity, event.getRenderer(), type, event.getPartialTick(), event.getPoseStack(),
                    event.getMultiBufferSource(), event.getPackedLight());
        }
    }

    /** Plays the death effect of {@code entity}. */
    public static void play(LivingEntity entity, DeathType type, float motionX, float motionY, float motionZ) {
        if (!(entity.level() instanceof ClientLevel level) || type == DeathType.DEFAULT) {
            return;
        }
        DEATH_TYPES.put(entity, new Death(type, System.identityHashCode(entity)));
        entity.setDeltaMovement(motionX, motionY, motionZ);

        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() / 2.0;
        double z = entity.getZ();
        switch (type) {
            case GORE -> gore(level, entity, motionX, motionY, motionZ);
            case BIO -> {
                ClientFx.createFX("biodeath", level, x, y, z, motionX, motionY, motionZ, 0f, 0f, 1.0f);
                level.playLocalSound(x, y, z, TGSounds.DEATH_BIO.get(), SoundSource.HOSTILE, 1.0f, 1.0f, false);
            }
            case LASER -> {
                ClientFx.createFX("laserdeathFire", level, x, y, z, motionX, 0, motionZ, 0f, 0f, 1.0f);
                ClientFx.createFX("laserdeathAsh", level, x, y, z, motionX, 0, motionZ, 0f, 0f, 1.0f);
                level.playLocalSound(x, y, z, TGSounds.DEATH_LASER.get(), SoundSource.HOSTILE, 1.0f, 1.0f, false);
            }
            default -> {
                // DISMEMBER has no effect yet (not produced by any weapon in Techguns 2 or Fabric)
            }
        }
    }

    private static void gore(ClientLevel level, LivingEntity entity, float motionX, float motionY, float motionZ) {
        GoreData data = entity instanceof AlienBug ? ALIEN_BUG_GORE : GORE_DATA.getOrDefault(entity.getType(), GENERIC_GORE);
        data.refresh();

        List<ModelPart> gibs = null;
        ResourceLocation gibsTexture = GORE_TEXTURE;
        EntityRenderer<? super LivingEntity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
            gibs = modelParts(livingRenderer.getModel());
            if (gibs != null) {
                gibsTexture = renderer.getTextureLocation(entity);
            }
        }
        if (gibs == null || gibs.isEmpty()) {
            gibs = genericGibs();
            gibsTexture = GORE_TEXTURE;
        }

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        level.playLocalSound(x, y + entity.getBbHeight() / 2.0, z, data.sound.get(), SoundSource.HOSTILE, 1.0f, 1.0f, false);

        if (data.main != null) {
            TGParticleManager.get().addEffect(new TGParticleSystem(level, data.main, x, y, z, motionX, motionY, motionZ));
        }
        if (data.gibs != null) {
            // one 3d particle per model part; the type is copied because the count depends on the model
            TGParticleSystemType gibsType = new TGParticleSystemType();
            gibsType.extend(data.gibs);
            gibsType.is3d = true;
            gibsType.particleCountMin = gibs.size();
            gibsType.particleCountMax = gibs.size();
            TGParticleSystem gibsSystem = new TGParticleSystem(level, gibsType, x, y, z, motionX, motionY, motionZ);
            gibsSystem.models = new ArrayList<>(gibs);
            gibsSystem.modelsTexture = gibsTexture;
            gibsSystem.attachedFXType = data.trail;
            TGParticleManager.get().addEffect(gibsSystem);
        }
    }

    /** Top level parts of an entity model, or null for model types that don't expose them. */
    @Nullable
    static List<ModelPart> modelParts(EntityModel<?> model) {
        List<ModelPart> parts = new ArrayList<>();
        if (model instanceof ListModel<?> listModel) {
            listModel.parts().forEach(parts::add);
        } else if (model instanceof AgeableListModel<?> ageable) {
            if (!AgeableParts.add(ageable, parts)) {
                return null;
            }
        } else if (model instanceof HierarchicalModel<?> hierarchical) {
            // ModelPart.children is made public by accesstransformer.cfg
            parts.addAll(hierarchical.root().children.values());
        } else {
            return null;
        }
        return parts;
    }

    private static List<ModelPart> genericGibs() {
        if (genericGibs == null) {
            ZombieModel<Zombie> zombie = new ZombieModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ZOMBIE));
            genericGibs = modelParts(zombie);
        }
        return genericGibs;
    }

    /**
     * Calls the protected {@code AgeableListModel.headParts()/bodyParts()}. An access transformer can't widen them:
     * every vanilla subclass overrides them as protected.
     */
    // MIGRATION(26.x): reflective access by Mojang name; entity models are reworked around a root ModelPart in 1.21.2+, recheck.
    private static final class AgeableParts {
        @Nullable
        private static final MethodHandle HEAD = find("headParts");
        @Nullable
        private static final MethodHandle BODY = find("bodyParts");

        @Nullable
        private static MethodHandle find(String name) {
            try {
                Method method = AgeableListModel.class.getDeclaredMethod(name);
                method.setAccessible(true);
                return MethodHandles.lookup().unreflect(method);
            } catch (ReflectiveOperationException | RuntimeException e) {
                Techguns.LOGGER.warn("Gore gibs: AgeableListModel.{} not accessible, using generic gibs", name, e);
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        static boolean add(AgeableListModel<?> model, List<ModelPart> parts) {
            if (HEAD == null || BODY == null) {
                return false;
            }
            try {
                ((Iterable<ModelPart>) BODY.invoke(model)).forEach(parts::add);
                ((Iterable<ModelPart>) HEAD.invoke(model)).forEach(parts::add);
                return true;
            } catch (Throwable e) {
                return false;
            }
        }
    }

    /** Blood color, FX and sound of a gore death (Fabric {@code DeathEffectHandler.GoreData}). */
    private static final class GoreData {
        private final int bloodR;
        private final int bloodG;
        private final int bloodB;
        private String fxMain = "GoreFX_Blood";
        private String fxTrail = "GoreTrailFX_Blood";
        private final String fxGibs = "Gore3dGibsFX_Generic";
        private Supplier<SoundEvent> sound = TGSounds.DEATH_GORE;

        @Nullable
        private TGParticleSystemType main;
        @Nullable
        private TGParticleSystemType trail;
        @Nullable
        private TGParticleSystemType gibs;
        /** {@link TGFX#generation()} the types were built for; -1 = not built. */
        private int generation = -1;

        GoreData(int bloodR, int bloodG, int bloodB) {
            this.bloodR = bloodR;
            this.bloodG = bloodG;
            this.bloodB = bloodB;
        }

        GoreData setFX(String main, String trail) {
            this.fxMain = main;
            this.fxTrail = trail;
            return this;
        }

        GoreData setSound(Supplier<SoundEvent> sound) {
            this.sound = sound;
            return this;
        }

        /** Rebuilds the blood colored FX types after the FX definitions were (re)loaded. */
        void refresh() {
            if (generation == TGFX.generation()) {
                return;
            }
            generation = TGFX.generation();
            main = tinted(fxMain);
            trail = tinted(fxTrail);
            gibs = tinted(fxGibs);
        }

        @Nullable
        private TGParticleSystemType tinted(String name) {
            TGParticleSystemType base = TGFX.systemType(name);
            if (base == null) {
                return null;
            }
            TGParticleSystemType type = new TGParticleSystemType();
            type.extend(base);
            if (!type.colorEntries.isEmpty()) {
                type.colorEntries.get(0).r = bloodR / 255.0f;
                type.colorEntries.get(0).g = bloodG / 255.0f;
                type.colorEntries.get(0).b = bloodB / 255.0f;
            }
            return type;
        }
    }
}
