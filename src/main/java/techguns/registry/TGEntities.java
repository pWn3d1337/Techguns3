package techguns.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;
import techguns.entity.projectile.BioGunProjectile;
import techguns.entity.projectile.ChainsawProjectile;
import techguns.entity.projectile.GenericBeamProjectile;
import techguns.entity.projectile.GenericProjectile;
import techguns.entity.projectile.GrapplingHookProjectile;
import techguns.entity.projectile.GrenadeProjectile;
import techguns.entity.projectile.GuidedMissileProjectile;
import techguns.entity.projectile.MagicRifleProjectile;
import techguns.entity.projectile.RocketProjectile;
import techguns.entity.projectile.SonicShotgunProjectile;
import techguns.entity.projectile.TFGProjectile;

public final class TGEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Techguns.MODID);

    /** Tracking range of fast projectiles, in chunks. */
    private static final int PROJECTILE_TRACKING_RANGE = 8;

    public static final Supplier<EntityType<GenericProjectile>> GENERIC_PROJECTILE = ENTITY_TYPES.register("generic_projectile",
            () -> EntityType.Builder.<GenericProjectile>of(GenericProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(PROJECTILE_TRACKING_RANGE).updateInterval(20)
                    .build("generic_projectile"));

    public static final Supplier<EntityType<GenericBeamProjectile>> GENERIC_BEAM_PROJECTILE = ENTITY_TYPES.register("generic_beam_projectile",
            () -> EntityType.Builder.<GenericBeamProjectile>of(GenericBeamProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(PROJECTILE_TRACKING_RANGE).updateInterval(20)
                    .build("generic_beam_projectile"));

    public static final Supplier<EntityType<RocketProjectile>> ROCKET_PROJECTILE = ENTITY_TYPES.register("rocket_projectile",
            () -> EntityType.Builder.<RocketProjectile>of(RocketProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(PROJECTILE_TRACKING_RANGE).updateInterval(20)
                    .build("rocket_projectile"));

    public static final Supplier<EntityType<SonicShotgunProjectile>> SONIC_SHOTGUN_PROJECTILE = ENTITY_TYPES.register("sonic_shotgun_projectile",
            () -> EntityType.Builder.<SonicShotgunProjectile>of(SonicShotgunProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(PROJECTILE_TRACKING_RANGE).updateInterval(20)
                    .build("sonic_shotgun_projectile"));

    public static final Supplier<EntityType<GuidedMissileProjectile>> GUIDED_MISSILE = ENTITY_TYPES.register("guided_missile",
            () -> EntityType.Builder.<GuidedMissileProjectile>of(GuidedMissileProjectile::new, MobCategory.MISC)
                    // missiles turn: sync position and motion every tick
                    .sized(0.25f, 0.25f).clientTrackingRange(PROJECTILE_TRACKING_RANGE).updateInterval(1)
                    .build("guided_missile"));

    public static final Supplier<EntityType<GrenadeProjectile>> GRENADE_PROJECTILE = ENTITY_TYPES.register("grenade_projectile",
            () -> EntityType.Builder.<GrenadeProjectile>of(GrenadeProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(PROJECTILE_TRACKING_RANGE).updateInterval(5)
                    .build("grenade_projectile"));

    public static final Supplier<EntityType<BioGunProjectile>> BIOGUN_PROJECTILE = ENTITY_TYPES.register("biogun_projectile",
            () -> EntityType.Builder.<BioGunProjectile>of(BioGunProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(PROJECTILE_TRACKING_RANGE).updateInterval(20)
                    .build("biogun_projectile"));

    public static final Supplier<EntityType<TFGProjectile>> TFG_PROJECTILE = ENTITY_TYPES.register("tfg_projectile",
            () -> EntityType.Builder.<TFGProjectile>of(TFGProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(PROJECTILE_TRACKING_RANGE).updateInterval(20)
                    .build("tfg_projectile"));

    public static final Supplier<EntityType<MagicRifleProjectile>> MAGIC_RIFLE_PROJECTILE = ENTITY_TYPES.register("magic_rifle_projectile",
            () -> EntityType.Builder.<MagicRifleProjectile>of(MagicRifleProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(PROJECTILE_TRACKING_RANGE).updateInterval(20)
                    .build("magic_rifle_projectile"));

    public static final Supplier<EntityType<ChainsawProjectile>> CHAINSAW_PROJECTILE = ENTITY_TYPES.register("chainsaw_projectile",
            () -> EntityType.Builder.<ChainsawProjectile>of(ChainsawProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(PROJECTILE_TRACKING_RANGE).updateInterval(20)
                    .build("chainsaw_projectile"));

    public static final Supplier<EntityType<GrapplingHookProjectile>> GRAPPLING_HOOK_PROJECTILE = ENTITY_TYPES.register("grappling_hook_projectile",
            () -> EntityType.Builder.<GrapplingHookProjectile>of(GrapplingHookProjectile::new, MobCategory.MISC)
                    // follows its target while grappling: sync position every tick
                    .sized(0.25f, 0.25f).clientTrackingRange(PROJECTILE_TRACKING_RANGE).updateInterval(1)
                    .build("grappling_hook_projectile"));

    /** Techguns 2 {@code NPCTurret}: 0.6 x 0.9 (Techguns 2 shrank the creature to 0.9 high). */
    public static final Supplier<EntityType<techguns.entity.npc.TurretEntity>> TURRET = ENTITY_TYPES.register("turret",
            () -> EntityType.Builder.<techguns.entity.npc.TurretEntity>of(techguns.entity.npc.TurretEntity::new, MobCategory.MISC)
                    .sized(0.6f, 0.9f).eyeHeight(0.75f).clientTrackingRange(10).fireImmune().build("turret"));

    private TGEntities() {
    }

    public static void registerAttributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(TURRET.get(), techguns.entity.npc.TurretEntity.createAttributes().build());
    }
}
