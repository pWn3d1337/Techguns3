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

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

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
import techguns.TGConfig;
import techguns.TGIds;
import techguns.Techguns;
import techguns.client.particle.ClientFx;
import techguns.client.particle.TGFX;
import techguns.client.particle.TGParticleManager;
import techguns.client.particle.TGParticleSystem;
import techguns.client.particle.TGParticleSystemType;
import techguns.core.damage.DeathType;
import techguns.registry.TGNpcs;
import techguns.registry.TGSounds;

/**
 * Client side of the death effects (Fabric {@code DeathEffectHandler}): gore bursts the dying entity into gibs made
 * of its own model parts in its blood color (Techguns 2 {@code DeathEffect}), bio and laser deaths play their particle FX. The death type is kept per entity so the
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

    /**
     * Gore settings by entity type (Techguns 2 {@code DeathEffect.goreStats}); everything else uses {@link #GENERIC_GORE}.
     * Filled on first use, the NPC types are registered after this class loads.
     */
    private static final Map<EntityType<?>, GoreData> GORE_DATA = new HashMap<>();
    private static final GoreData GENERIC_GORE = new GoreData(160, 21, 31);
    private static boolean goreDataFilled;

    /**
     * Sets the gore of an entity type: blood color, FX size, gibs (Techguns 2 {@code DeathEffect.addGoreData}). Only
     * entities in the {@code techguns:gore} entity tag are gored.
     */
    public static void addGore(EntityType<?> type, GoreData data) {
        GORE_DATA.put(type, data);
    }

    private static GoreData goreData(EntityType<?> type) {
        if (!goreDataFilled) {
            goreDataFilled = true;
            addDefaultGore();
        }
        return GORE_DATA.getOrDefault(type, GENERIC_GORE);
    }

    /** Techguns 2 {@code DeathEffect} gore table, plus the newer vanilla creatures. */
    private static void addDefaultGore() {
        GoreData human = new GoreData(160, 21, 31);
        GoreData zombie = new GoreData(110, 21, 41);
        GoreData pigman = new GoreData(110, 51, 11);
        GoreData animal = new GoreData(170, 26, 37);
        GoreData villager = new GoreData(150, 21, 51);
        // Fabric: skeletons crumble into dust; Techguns 2 sprayed white blood
        GoreData skeleton = new GoreData(192, 192, 192).setFX("GoreFX_Dust", "GoreTrailFX_Dust").setSound(() -> SoundEvents.SKELETON_DEATH);

        for (EntityType<?> type : List.of(EntityType.PLAYER, EntityType.WITCH, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE,
                TGNpcs.ARMY_SOLDIER.type().get(), TGNpcs.STORMTROOPER.type().get(), TGNpcs.COMMANDO.type().get(),
                TGNpcs.DICTATOR_DAVE.type().get(), TGNpcs.PSYCHO_STEVE.type().get(), TGNpcs.BANDIT.type().get(), TGNpcs.OUTCAST.type().get())) {
            addGore(type, human);
        }
        for (EntityType<?> type : List.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER,
                EntityType.ZOMBIE_HORSE, EntityType.EVOKER, EntityType.VINDICATOR, EntityType.PILLAGER, EntityType.ILLUSIONER,
                EntityType.PHANTOM, TGNpcs.ZOMBIE_SOLDIER.type().get(), TGNpcs.ZOMBIE_FARMER.type().get(), TGNpcs.ZOMBIE_MINER.type().get(),
                TGNpcs.ZOMBIE_POLICEMAN.type().get())) {
            addGore(type, zombie);
        }
        addGore(EntityType.GIANT, new GoreData(110, 21, 41).setFXScale(3.0f));
        for (EntityType<?> type : List.of(EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN, TGNpcs.ZOMBIE_PIGMAN_SOLDIER.type().get())) {
            addGore(type, pigman);
        }
        addGore(EntityType.VILLAGER, villager);
        addGore(EntityType.WANDERING_TRADER, villager);
        for (EntityType<?> type : List.of(EntityType.SKELETON, EntityType.STRAY, EntityType.BOGGED, EntityType.SKELETON_HORSE,
                TGNpcs.SKELETON_SOLDIER.type().get())) {
            addGore(type, skeleton);
        }
        addGore(EntityType.WITHER_SKELETON, new GoreData(64, 64, 64).setFX("GoreFX_Dust", "GoreTrailFX_Dust").setFXScale(1.4f)
                .setSound(() -> SoundEvents.WITHER_SKELETON_DEATH));

        for (EntityType<?> type : List.of(EntityType.COW, EntityType.MOOSHROOM, EntityType.LLAMA, EntityType.TRADER_LLAMA, EntityType.HORSE,
                EntityType.DONKEY, EntityType.MULE, EntityType.CAMEL, EntityType.WOLF, EntityType.GOAT, EntityType.PANDA, EntityType.STRIDER,
                EntityType.GUARDIAN, EntityType.DOLPHIN)) {
            addGore(type, animal);
        }
        addGore(EntityType.SHEEP, new GoreData(170, 26, 37).setFXScale(0.8f));
        addGore(EntityType.PIG, new GoreData(170, 26, 37).setFXScale(0.8f));
        addGore(EntityType.POLAR_BEAR, new GoreData(170, 26, 37).setFXScale(0.8f));
        addGore(EntityType.TURTLE, new GoreData(170, 26, 37).setFXScale(0.8f));
        addGore(EntityType.CHICKEN, new GoreData(170, 26, 37).setFXScale(0.5f));
        addGore(EntityType.PARROT, new GoreData(170, 26, 37).setFXScale(0.5f));
        addGore(EntityType.RABBIT, new GoreData(170, 26, 37).setFXScale(0.3f));
        for (EntityType<?> type : List.of(EntityType.CAT, EntityType.OCELOT, EntityType.FOX)) {
            addGore(type, new GoreData(170, 26, 37).setFXScale(0.6f));
        }
        for (EntityType<?> type : List.of(EntityType.ARMADILLO, EntityType.FROG, EntityType.AXOLOTL, EntityType.COD, EntityType.SALMON,
                EntityType.PUFFERFISH, EntityType.TROPICAL_FISH)) {
            addGore(type, new GoreData(170, 26, 37).setFXScale(0.4f));
        }
        addGore(EntityType.BAT, new GoreData(170, 26, 37).setFXScale(0.3f));
        addGore(EntityType.TADPOLE, new GoreData(170, 26, 37).setFXScale(0.2f));
        addGore(EntityType.HOGLIN, new GoreData(170, 26, 37).setFXScale(1.2f));
        addGore(EntityType.SNIFFER, new GoreData(170, 26, 37).setFXScale(1.2f));
        addGore(EntityType.RAVAGER, new GoreData(170, 26, 37).setFXScale(1.4f));
        addGore(EntityType.ELDER_GUARDIAN, new GoreData(170, 26, 37).setFXScale(1.4f));
        addGore(EntityType.SQUID, new GoreData(30, 30, 70));
        addGore(EntityType.GLOW_SQUID, new GoreData(40, 200, 190));

        addGore(EntityType.CREEPER, new GoreData(50, 175, 57));
        addGore(EntityType.ENDERMAN, new GoreData(160, 36, 167));
        addGore(EntityType.ENDERMITE, new GoreData(160, 36, 167).setFXScale(0.4f));
        addGore(EntityType.SPIDER, new GoreData(85, 156, 17));
        addGore(EntityType.CAVE_SPIDER, new GoreData(85, 156, 17).setFXScale(0.7f));
        addGore(EntityType.BEE, new GoreData(235, 200, 60).setFXScale(0.3f));
        addGore(EntityType.SILVERFISH, new GoreData(90, 16, 27).setFXScale(0.4f));
        addGore(EntityType.WARDEN, new GoreData(30, 70, 80).setFXScale(1.4f));
        addGore(TGNpcs.CYBER_DEMON.type().get(), new GoreData(85, 156, 17));
        for (TGNpcs.Npc mutant : List.of(TGNpcs.SUPER_MUTANT_BASIC, TGNpcs.SUPER_MUTANT_HEAVY, TGNpcs.SUPER_MUTANT_ELITE)) {
            addGore(mutant.type().get(), new GoreData(109, 60, 25).setFXScale(1.2f));
        }
        addGore(TGNpcs.ALIEN_BUG.type().get(), new GoreData(235, 255, 70));

        // Techguns 2 ModelGibsSlime: invisible gibs, only their goo trails show
        addGore(EntityType.SLIME, new GoreData(40, 255, 40).setBlobs());
        addGore(EntityType.MAGMA_CUBE, new GoreData(92, 26, 0).setBlobs());
        addGore(EntityType.GHAST, new GoreData(255, 255, 255).setFXScale(3.5f).setBlobs());
        addGore(TGNpcs.GHASTLING.type().get(), new GoreData(255, 255, 255).setBlobs());
        addGore(EntityType.VEX, new GoreData(215, 215, 215).setFXScale(0.4f).setBlobs());
        addGore(EntityType.ALLAY, new GoreData(140, 200, 255).setFXScale(0.4f).setBlobs());
        addGore(EntityType.SHULKER, new GoreData(125, 0, 106).setFXScale(1.5f).setBlobs());
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
        if (!(entity.level() instanceof ClientLevel level) || type == DeathType.DEFAULT || !TGConfig.DEATH_EFFECTS.get()
                || type == DeathType.GORE && !TGConfig.GORE_DEATH_EFFECT.get()) {
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
        GoreData data = goreData(entity.getType());
        data.refresh();

        List<ModelPart> gibs = null;
        ResourceLocation gibsTexture = GORE_TEXTURE;
        float gibsScale = 1.0f;
        EntityRenderer<? super LivingEntity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
            gibsScale = RenderScale.of(livingRenderer, entity);
            gibs = data.blobs ? BLOBS : modelParts(livingRenderer.getModel());
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
            gibsSystem.modelsScale = gibsScale;
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

    /** Techguns 2 {@code ModelGibsSlime}: eight gibs without cubes, so only their blood trails show. */
    private static final List<ModelPart> BLOBS = java.util.Collections.nCopies(8, new ModelPart(List.of(), Map.of()));

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

    /**
     * Render scale of a living entity (Techguns 2 drew the gibs after the renderer's {@code preRenderCallback}): slime
     * size, ghast, giant, wither skeleton. Calls the protected {@code LivingEntityRenderer.scale}, which every renderer
     * overrides as protected, so an access transformer can't widen it.
     */
    // MIGRATION(26.x): reflective access by Mojang name; 1.21.2+ renderers scale a render state instead of the entity.
    private static final class RenderScale {
        @Nullable
        private static final MethodHandle SCALE = find();

        @Nullable
        private static MethodHandle find() {
            try {
                Method method = LivingEntityRenderer.class.getDeclaredMethod("scale", LivingEntity.class, PoseStack.class, float.class);
                method.setAccessible(true);
                return MethodHandles.lookup().unreflect(method);
            } catch (ReflectiveOperationException | RuntimeException e) {
                Techguns.LOGGER.warn("Gore gibs: LivingEntityRenderer.scale not accessible, gibs keep the model size", e);
                return null;
            }
        }

        static float of(LivingEntityRenderer<?, ?> renderer, LivingEntity entity) {
            if (SCALE == null) {
                return 1.0f;
            }
            try {
                PoseStack poseStack = new PoseStack();
                SCALE.invoke(renderer, entity, poseStack, 1.0f);
                Vector3f scale = poseStack.last().pose().getScale(new Vector3f());
                float average = (scale.x + scale.y + scale.z) / 3.0f;
                return average > 0.0f && Float.isFinite(average) ? average : 1.0f;
            } catch (Throwable e) {
                return 1.0f;
            }
        }
    }

    /** Blood color, FX, size and sound of a gore death (Techguns 2 {@code DeathEffect.GoreData}). */
    public static final class GoreData {
        private final int bloodR;
        private final int bloodG;
        private final int bloodB;
        private String fxMain = "GoreFX_Blood";
        private String fxTrail = "GoreTrailFX_Blood";
        private final String fxGibs = "Gore3dGibsFX_Generic";
        private Supplier<SoundEvent> sound = TGSounds.DEATH_GORE;
        /** Scales the blood FX (particle sizes and spawn volume). */
        private float fxScale = 1.0f;
        /** Invisible gibs that only leave blood trails, instead of the entity's model parts (slimes, ghasts). */
        private boolean blobs;

        @Nullable
        private TGParticleSystemType main;
        @Nullable
        private TGParticleSystemType trail;
        @Nullable
        private TGParticleSystemType gibs;
        /** {@link TGFX#generation()} the types were built for; -1 = not built. */
        private int generation = -1;

        public GoreData(int bloodR, int bloodG, int bloodB) {
            this.bloodR = bloodR;
            this.bloodG = bloodG;
            this.bloodB = bloodB;
        }

        public GoreData setFX(String main, String trail) {
            this.fxMain = main;
            this.fxTrail = trail;
            return this;
        }

        public GoreData setSound(Supplier<SoundEvent> sound) {
            this.sound = sound;
            return this;
        }

        public GoreData setFXScale(float scale) {
            this.fxScale = scale;
            return this;
        }

        public GoreData setBlobs() {
            this.blobs = true;
            return this;
        }

        /** Rebuilds the blood colored FX types after the FX definitions were (re)loaded. */
        void refresh() {
            if (generation == TGFX.generation()) {
                return;
            }
            generation = TGFX.generation();
            main = scaled(tinted(fxMain));
            trail = scaled(tinted(fxTrail));
            gibs = tinted(fxGibs);
        }

        /**
         * Techguns 2 {@code GoreData.getExtendedType}: the blood FX grow with the entity (Techguns 2 also scaled a size
         * damping factor, which made large entities' blood grow exponentially).
         */
        @Nullable
        private TGParticleSystemType scaled(@Nullable TGParticleSystemType type) {
            if (type != null && fxScale != 1.0f) {
                type.sizeMin *= fxScale;
                type.sizeMax *= fxScale;
                type.sizeRateMin *= fxScale;
                type.sizeRateMax *= fxScale;
                type.startSizeRateMin *= fxScale;
                type.startSizeRateMax *= fxScale;
                for (int i = 0; i < type.volumeData.length; i++) {
                    type.volumeData[i] *= fxScale;
                }
            }
            return type;
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
