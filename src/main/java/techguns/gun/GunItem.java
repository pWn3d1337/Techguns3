package techguns.gun;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import techguns.core.damage.TGDamageCategory;
import techguns.gun.ammo.AmmoType;
import techguns.gun.ammo.DamageModifier;
import techguns.core.fx.EntityCondition;
import techguns.network.TGNetwork;
import techguns.registry.TGComponents;

/**
 * Base class of all Techguns guns (Techguns 2 {@code GenericGun}). Stats come from {@link GunStats},
 * projectiles from the {@link ProjectileSelector}.
 * <p>
 * Firing runs on both sides: the client predicts ammo use and effects, the server spawns projectiles.
 * Both are gated by the per-hand fire delay in {@link ShooterState}.
 */
public class GunItem extends Item {
    /** Max distance (blocks) at which other players hear the gun. */
    public static final float SOUND_VOLUME = 4.0f;

    private final GunStats stats;
    private final GunSounds sounds;
    private final ProjectileSelector projectiles;
    /** Particle system attached to the shooter on every shot (NDR muzzle beam); null = none. */
    @Nullable
    private String fireFx;
    private float fireFxX;
    private float fireFxY;
    private float fireFxZ;

    public GunItem(Properties properties, GunStats stats, GunSounds sounds, ProjectileSelector projectiles) {
        super(properties.stacksTo(1).component(TGComponents.GUN_AMMO.get(), new GunAmmo(stats.firing().clipSize(), AmmoType.DEFAULT)));
        this.stats = stats;
        this.sounds = sounds;
        this.projectiles = projectiles;
    }

    /** Fabric {@code setFireFX}: the x offset is mirrored for the right hand. */
    public GunItem withFireFx(String name, float x, float y, float z) {
        this.fireFx = name;
        this.fireFxX = x;
        this.fireFxY = y;
        this.fireFxZ = z;
        return this;
    }

    public GunSounds sounds() {
        return sounds;
    }

    public GunStats stats() {
        return stats;
    }

    public AmmoType ammoType() {
        return projectiles.ammoType();
    }

    /** Server: the target a client claims to have locked on, if this gun locks on and the claim is plausible; else null. */
    @Nullable
    public Entity acceptTarget(Player player, @Nullable Entity target) {
        return null;
    }

    /** Guns shoot with left click; tools like the chainsaw override this. */
    public boolean shootsWithLeftClick() {
        return true;
    }

    public boolean isFullyLoaded(ItemStack stack) {
        return GunData.loaded(stack) >= stats.firing().clipSize();
    }

    // ------------------------------------------------------------------------------------------------
    // Firing
    // ------------------------------------------------------------------------------------------------

    /**
     * Fire (or start a reload when empty). Called on the client for prediction and on the server from the
     * shoot payload.
     */
    public void shoot(ItemStack stack, Level level, Player player, InteractionHand hand, boolean zooming) {
        shoot(stack, level, player, hand, zooming, null);
    }

    /** @param target locked-on target sent by the client (lock-on guns), or null */
    public void shoot(ItemStack stack, Level level, Player player, InteractionHand hand, boolean zooming, @Nullable Entity target) {
        ShooterState state = ShooterState.of(player);
        if (state.fireDelay(hand) > 0) {
            return;
        }
        if (GunData.loaded(stack) <= 0) {
            startReload(stack, level, player, hand, false);
            return;
        }

        GunStats.Firing firing = stats.firing();
        state.setFireDelay(hand, firing.fireDelay());
        if (!player.getAbilities().instabuild) {
            GunData.consume(stack, 1);
        }

        if (!level.isClientSide) {
            float spreadMultiplier = 1.0f;
            // shooting while blocking with a shield is inaccurate
            if (hand == InteractionHand.MAIN_HAND && player.isBlocking()) {
                spreadMultiplier *= stats.handType() == GunHandType.ONE_HANDED ? 4.0f : 8.0f;
            }
            FirePos firePos = firePos(player, hand);
            if (zooming && stats.zoom().isPresent()) {
                spreadMultiplier *= stats.zoom().get().spreadMultiplier();
                firePos = FirePos.CENTER;
            }
            fireProjectiles(level, player, stack, spreadMultiplier, 1.0f, firePos, target);
            if (fireFx != null) {
                float x = firePos == FirePos.RIGHT ? -fireFxX : fireFxX;
                TGNetwork.sendFxOnEntityAndSelf(player, fireFx, x, fireFxY - 0.1f, fireFxZ, true, EntityCondition.ENTITY_ALIVE);
            }
            TGNetwork.sendGunFired(player, hand, firing.recoilTicks(), firing.muzzleFlashTicks());
        } else {
            GunClientEffects.get().onFired(player, hand, firing.recoilTicks(), firing.muzzleFlashTicks());
        }
        playFireSounds(level, player, hand, state);
    }

    private void playFireSounds(Level level, Player player, InteractionHand hand, ShooterState state) {
        if (sounds.maxLoopDelay() > 0 && sounds.fireStart() != null && state.loopSoundDelay(hand) <= 0) {
            playSound(level, player, sounds.fireStart().get(), SOUND_VOLUME);
        } else {
            playSound(level, player, sounds.fire().get(), SOUND_VOLUME);
        }
        if (sounds.maxLoopDelay() > 0) {
            state.setLoopSoundDelay(hand, sounds.maxLoopDelay());
        }
        if (sounds.rechamber() != null) {
            playSound(level, player, sounds.rechamber().get(), 1.0f);
        }
    }

    /**
     * Plays a sound at the shooter: on the client only for the local player (prediction), on the server
     * for everybody else.
     */
    protected static void playSound(Level level, Player player, SoundEvent sound, float volume) {
        level.playSound(player, player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, volume, 1.0f);
    }

    protected static FirePos firePos(LivingEntity shooter, InteractionHand hand) {
        boolean right = (hand == InteractionHand.MAIN_HAND) == (shooter.getMainArm() == HumanoidArm.RIGHT);
        return right ? FirePos.RIGHT : FirePos.LEFT;
    }

    /** Spawns the projectile(s) of one shot. Server side only. */
    protected void fireProjectiles(Level level, LivingEntity shooter, ItemStack stack, float spreadMultiplier, float damageMultiplier, FirePos firePos,
                                   @Nullable Entity target) {
        GunStats.Projectile projectile = stats.projectile();
        spawnProjectile(level, shooter, stack, projectile.spread() * spreadMultiplier, projectile.forwardOffset(), damageMultiplier, firePos, target);

        if (projectile.pellets() > 0) {
            float burstOffset = projectile.burst() ? projectile.speed() / projectile.pellets() : 0.0f;
            for (int i = 0; i < projectile.pellets(); i++) {
                spawnProjectile(level, shooter, stack, projectile.pelletSpread() * spreadMultiplier,
                        projectile.forwardOffset() + burstOffset * (i + 1.0f), damageMultiplier, firePos, target);
            }
        }
    }

    protected void spawnProjectile(Level level, LivingEntity shooter, ItemStack stack, float spread, float forwardOffset, float damageMultiplier, FirePos firePos,
                                   @Nullable Entity target) {
        Projectile entity = createProjectile(level, shooter, stack, spread, damageMultiplier, firePos, target);
        if (forwardOffset > 0.0f) {
            float speed = projectiles.factory(GunData.variant(stack)).modifier().velocity(stats.projectile().speed());
            entity.setPos(entity.position().add(entity.getDeltaMovement().scale(forwardOffset / speed)));
        }
        level.addFreshEntity(entity);
    }

    /** Creates (but does not spawn) one projectile of the loaded ammo variant. */
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack stack, float spread, float damageMultiplier, FirePos firePos,
                                          @Nullable Entity target) {
        return factory(stack).create(shotParams(level, shooter, stack, spread, damageMultiplier, firePos, target));
    }

    /** Projectile factory of the loaded ammo variant. */
    protected ProjectileFactory<?> factory(ItemStack stack) {
        return projectiles.factory(GunData.variant(stack));
    }

    /** Gun stats with the loaded ammo's damage modifier applied, for one projectile. */
    protected ShotParams shotParams(Level level, LivingEntity shooter, ItemStack stack, float spread, float damageMultiplier, FirePos firePos,
                                    @Nullable Entity target) {
        DamageModifier mod = factory(stack).modifier();
        GunStats.Damage damage = stats.damage();
        GunStats.Projectile projectile = stats.projectile();

        float speed = mod.velocity(projectile.speed());
        int lifetime = Math.round(projectile.lifetimeTicks() * mod.rangeMul() + mod.rangeAdd());
        return new ShotParams(this, level, shooter,
                mod.damage(damage.damage()) * damageMultiplier, mod.damage(damage.damageMin()) * damageMultiplier,
                mod.range(damage.dropStart()), mod.range(damage.dropEnd()), damage.penetration(),
                speed, lifetime, spread, projectile.gravity(), mod.radius(damage.radius()),
                blockDamage(shooter), firePos, target);
    }

    /** Players in safe mode never damage blocks. */
    public static boolean blockDamage(LivingEntity shooter) {
        // TODO Phase 4: safe mode toggle in player data; until then guns never damage blocks
        return !(shooter instanceof Player);
    }

    // ------------------------------------------------------------------------------------------------
    // Reloading
    // ------------------------------------------------------------------------------------------------

    /**
     * Starts a reload if ammo is available.
     *
     * @param forced reload a partially filled gun (force reload key); loose rounds are returned
     */
    public void startReload(ItemStack stack, Level level, Player player, InteractionHand hand, boolean forced) {
        ShooterState state = ShooterState.of(player);
        if (state.fireDelay(hand) > 0 || (forced && isFullyLoaded(stack))) {
            return;
        }
        GunAmmo current = GunData.ammo(stack);
        // ammo of the loaded variant first, otherwise any variant the player carries
        String variant = findReloadVariant(player, current.variant());
        if (variant == null) {
            if (forced && !level.isClientSide) {
                level.playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0f, 1.2f);
            }
            return;
        }
        AmmoInventory.consume(player, ammoType().ammoStacks(variant));
        if (forced) {
            returnLooseRounds(player, current);
        }
        ammoType().emptyStacks().forEach(empty -> AmmoInventory.give(player, empty));

        GunStats.Firing firing = stats.firing();
        int loaded = firing.clipSize();
        if (firing.ammoPerReload() > 1) {
            int items = 1;
            int start = forced ? current.loaded() : 0;
            while (items < firing.ammoPerReload() - start && AmmoInventory.consume(player, ammoType().ammoStacks(variant))) {
                items++;
            }
            loaded = Math.min(firing.clipSize(), start + items * (firing.clipSize() / firing.ammoPerReload()));
        }
        GunData.setAmmo(stack, new GunAmmo(loaded, variant));
        state.setFireDelay(hand, firing.reloadTicks());

        playSound(level, player, sounds.reload().get(), 1.0f);
        if (level.isClientSide) {
            GunClientEffects.get().resetZoom();
            GunClientEffects.get().onReloadStarted(player, hand, firing.reloadTicks());
        } else {
            TGNetwork.sendReloadStarted(player, hand, firing.reloadTicks());
        }
    }

    @Nullable
    private String findReloadVariant(Player player, String preferred) {
        if (AmmoInventory.has(player, ammoType().ammoStacks(preferred))) {
            return preferred;
        }
        for (AmmoType.Variant variant : ammoType().variants()) {
            if (AmmoInventory.has(player, ammoType().ammoStacks(variant.key()))) {
                return variant.key();
            }
        }
        return null;
    }

    /** On a forced reload of a magazine gun, rounds left in the old magazine come back as loose bullets. */
    private void returnLooseRounds(Player player, GunAmmo current) {
        if (current.loaded() <= 0 || stats.firing().ammoPerReload() > 1) {
            return;
        }
        AmmoType.Variant variant = ammoType().variant(current.variant());
        float shotsPerBullet = techguns.core.gun.GunMath.shotsPerBullet(stats.firing().clipSize(), stats.firing().ammoPerReload(), ammoType().bulletsPerMag());
        int bullets = (int) Math.floor(current.loaded() / shotsPerBullet);
        if (bullets > 0) {
            variant.bullet().stream().filter(java.util.Objects::nonNull)
                    .forEach(bullet -> AmmoInventory.give(player, new ItemStack(bullet, bullets)));
        }
    }

    /**
     * Items an unloaded gun gives back (ammo change recipe, Fabric {@code AmmoChangeRecipe}): per ammo slot a full
     * magazine if the rounds left fill one, else the loose rounds and the empty item.
     */
    public List<ItemStack> unloadedItems(GunAmmo current) {
        List<ItemStack> items = new java.util.ArrayList<>();
        AmmoType type = ammoType();
        AmmoType.Variant variant = type.variant(current.variant());
        GunStats.Firing firing = stats.firing();
        float shotsPerBullet = techguns.core.gun.GunMath.shotsPerBullet(firing.clipSize(), firing.ammoPerReload(), type.bulletsPerMag());
        int bullets = (int) Math.floor(current.loaded() / shotsPerBullet);
        List<ItemStack> empties = type.emptyStacks();
        for (int slot = 0; slot < variant.ammo().size(); slot++) {
            ItemStack empty = slot < empties.size() ? empties.get(slot) : ItemStack.EMPTY;
            net.minecraft.world.level.ItemLike bullet = slot < variant.bullet().size() ? variant.bullet().get(slot) : null;
            if (!empty.isEmpty() && type.bulletsPerMag() > 0 && bullets == type.bulletsPerMag()) {
                items.add(new ItemStack(variant.ammo().get(slot)));
                continue;
            }
            if (bullets > 0 && bullet != null) {
                // split into valid stack sizes
                ItemStack rounds = new ItemStack(bullet);
                for (int left = bullets; left > 0; left -= rounds.getMaxStackSize()) {
                    items.add(rounds.copyWithCount(Math.min(left, rounds.getMaxStackSize())));
                }
            }
            if (!empty.isEmpty()) {
                items.add(empty);
            }
        }
        return items;
    }

    // ------------------------------------------------------------------------------------------------
    // Item behaviour
    // ------------------------------------------------------------------------------------------------

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide && stats.zoom().isPresent() && stats.zoom().get().toggle() && ShooterState.of(player).fireDelay(hand) <= 0) {
            GunClientEffects.get().toggleZoom(stats.zoom().get().fovMultiplier());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !shootsWithLeftClick();
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        // no arm swing when firing
        return shootsWithLeftClick();
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // ammo changes must not make the gun bob
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    public TGDamageCategory damageCategory(ItemStack stack) {
        return projectiles.factory(GunData.variant(stack)).damageCategory();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        GunAmmo ammo = GunData.ammo(stack);
        tooltip.add(Component.translatable("tooltip.techguns.gun.ammo", ammo.loaded(), stats.firing().clipSize()).withStyle(ChatFormatting.GRAY));
        for (ItemStack ammoItem : ammoType().ammoStacks(ammo.variant())) {
            tooltip.add(Component.translatable("tooltip.techguns.gun.ammo_item", ammoItem.getHoverName()).withStyle(ChatFormatting.GRAY));
        }
        GunStats.Damage damage = stats.damage();
        String dmg = damage.damageMin() < damage.damage() ? "%.1f-%.1f".formatted(damage.damage(), damage.damageMin()) : "%.1f".formatted(damage.damage());
        String pellets = stats.projectile().pellets() > 0 ? " (x" + (stats.projectile().pellets() + 1) + ")" : "";
        tooltip.add(Component.translatable("tooltip.techguns.gun.damage", dmg + pellets).withStyle(ChatFormatting.GRAY));
        if (GunClientEffects.get().hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.techguns.gun.damage_type",
                    Component.translatable("techguns.damage_category." + damageCategory(stack).id())).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.techguns.gun.range", "%.0f".formatted(stats.projectile().range())).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.techguns.gun.reload_time", "%.2f".formatted(stats.firing().reloadTicks() * 0.05f)).withStyle(ChatFormatting.GRAY));
            if (damage.penetration() > 0.0f) {
                tooltip.add(Component.translatable("tooltip.techguns.gun.penetration", "%.1f".formatted(damage.penetration())).withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.techguns.shift_for_details").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        // no melee hits with guns that shoot on left click
        return shootsWithLeftClick();
    }
}
