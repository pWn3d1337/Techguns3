package techguns.gun;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import techguns.TGConfig;
import techguns.core.damage.TGDamageCategory;
import techguns.player.TGPlayerData;
import techguns.stats.StatOverrides;
import techguns.gun.ammo.AmmoType;
import techguns.gun.ammo.DamageModifier;
import techguns.core.fx.EntityCondition;
import techguns.network.TGNetwork;
import techguns.registry.TGAttributes;
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

    /** The built-in stats; datapacks can override them ({@link StatOverrides}). */
    private final GunStats defaultStats;
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
        this.defaultStats = stats;
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

    /** Current stats: the datapack override if there is one, else the built-in stats. */
    public GunStats stats() {
        return StatOverrides.gunStats(this, defaultStats);
    }

    public GunStats defaultStats() {
        return defaultStats;
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
        return GunData.loaded(stack) >= stats().firing().clipSize();
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
        if (state.fireDelay(hand) > 0 || !GunHands.canUse(player, hand)) {
            return;
        }
        if (GunData.loaded(stack) <= 0) {
            startReload(stack, level, player, hand, false);
            return;
        }

        GunStats.Firing firing = stats().firing();
        state.setFireDelay(hand, firing.fireDelay());
        if (!player.getAbilities().instabuild) {
            GunData.consume(stack, 1);
        }

        if (!level.isClientSide) {
            float spreadMultiplier = 1.0f;
            // shooting while blocking with a shield is inaccurate
            if (hand == InteractionHand.MAIN_HAND && player.isBlocking()) {
                spreadMultiplier *= stats().handType() == GunHandType.ONE_HANDED ? 4.0f : 8.0f;
            }
            // armor gun accuracy bonus (Techguns 2 GUN_ACCURACY)
            spreadMultiplier *= (float) Math.clamp(1.0 - player.getAttributeValue(TGAttributes.GUN_ACCURACY), 0.0, 1.0);
            FirePos firePos = firePos(player, hand);
            if (zooming && stats().zoom().isPresent()) {
                spreadMultiplier *= stats().zoom().get().spreadMultiplier();
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

    /**
     * Techguns 2 {@code fireWeaponFromNPC}: one shot of an NPC or turret. The shooter keeps its own attack timing and
     * ammo; spread is the zoomed spread (NPCs aim) times {@code accuracyScale}. Server only.
     */
    public void fireFromNpc(LivingEntity shooter, ItemStack stack, float damageScale, float accuracyScale, FirePos firePos) {
        Level level = shooter.level();
        if (level.isClientSide) {
            return;
        }
        float spreadMultiplier = stats().zoom().map(GunStats.Zoom::spreadMultiplier).orElse(1.0f) * accuracyScale;
        fireProjectiles(level, shooter, stack, spreadMultiplier, damageScale, firePos, null);
        level.playSound(null, shooter.getX(), shooter.getEyeY(), shooter.getZ(), sounds.fire().get(), shooter.getSoundSource(), SOUND_VOLUME, 1.0f);
    }

    /**
     * Techguns 2 {@code TurretTileEnt.doReload}: loads the gun from the slots {@code from..to} of {@code handler}
     * (the loaded variant first, then any other); empty magazines go to {@code empties}. False without ammo.
     */
    public boolean reloadFrom(ItemStack stack, net.neoforged.neoforge.items.IItemHandler handler, int from, int to,
                              java.util.function.Consumer<ItemStack> empties) {
        GunAmmo current = GunData.ammo(stack);
        String variant = null;
        if (hasAll(handler, from, to, ammoType().ammoStacks(current.variant()))) {
            variant = current.variant();
        } else {
            for (AmmoType.Variant candidate : ammoType().variants()) {
                if (hasAll(handler, from, to, ammoType().ammoStacks(candidate.key()))) {
                    variant = candidate.key();
                    break;
                }
            }
        }
        if (variant == null) {
            return false;
        }
        List<ItemStack> ammo = ammoType().ammoStacks(variant);
        takeAll(handler, from, to, ammo);
        ammoType().emptyStacks().forEach(empties);
        GunStats.Firing firing = stats().firing();
        int loaded = firing.clipSize();
        if (firing.ammoPerReload() > 1) {
            int items = 1;
            int wanted = techguns.core.gun.GunMath.reloadItems(firing.clipSize(), firing.ammoPerReload(), 0);
            while (items < wanted && hasAll(handler, from, to, ammo)) {
                takeAll(handler, from, to, ammo);
                items++;
            }
            loaded = Math.min(firing.clipSize(), items * techguns.core.gun.GunMath.shotsPerReloadItem(firing.clipSize(), firing.ammoPerReload()));
        }
        GunData.setAmmo(stack, new GunAmmo(loaded, variant));
        return true;
    }

    private static boolean hasAll(net.neoforged.neoforge.items.IItemHandler handler, int from, int to, List<ItemStack> wanted) {
        if (wanted.isEmpty()) {
            return false;
        }
        for (ItemStack want : wanted) {
            int count = 0;
            for (int slot = from; slot <= to; slot++) {
                ItemStack present = handler.getStackInSlot(slot);
                if (ItemStack.isSameItemSameComponents(present, want)) {
                    count += present.getCount();
                }
            }
            if (count < want.getCount()) {
                return false;
            }
        }
        return true;
    }

    private static void takeAll(net.neoforged.neoforge.items.IItemHandler handler, int from, int to, List<ItemStack> wanted) {
        for (ItemStack want : wanted) {
            int left = want.getCount();
            for (int slot = from; slot <= to && left > 0; slot++) {
                if (ItemStack.isSameItemSameComponents(handler.getStackInSlot(slot), want)) {
                    left -= handler.extractItem(slot, left, false).getCount();
                }
            }
        }
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
        GunStats.Projectile projectile = stats().projectile();
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
            float speed = projectiles.factory(GunData.variant(stack)).modifier().velocity(stats().projectile().speed());
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
        GunStats.Damage damage = stats().damage();
        GunStats.Projectile projectile = stats().projectile();

        float speed = mod.velocity(projectile.speed());
        int lifetime = Math.round(projectile.lifetimeTicks() * mod.rangeMul() + mod.rangeAdd());
        return new ShotParams(this, level, shooter,
                mod.damage(damage.damage()) * damageMultiplier, mod.damage(damage.damageMin()) * damageMultiplier,
                mod.range(damage.dropStart()), mod.range(damage.dropEnd()), damage.penetration(),
                speed, lifetime, spread, projectile.gravity(), mod.radius(damage.radius()),
                blockDamage(shooter), firePos, target);
    }

    /**
     * Techguns 2 {@code getDoBlockDamage}: only players damage blocks, unless they switched on safe mode or the server
     * doesn't allow unsafe mode; NPC and turret shots never do.
     */
    public static boolean blockDamage(LivingEntity shooter) {
        return shooter instanceof Player player && TGConfig.ALLOW_UNSAFE_MODE.get()
                && !TGPlayerData.of(player).get(TGPlayerData.Toggle.SAFE_MODE);
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
        if (state.fireDelay(hand) > 0 || (forced && isFullyLoaded(stack)) || !GunHands.canUse(player, hand)) {
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

        GunStats.Firing firing = stats().firing();
        int loaded = firing.clipSize();
        if (firing.ammoPerReload() > 1) {
            int items = 1;
            int start = forced ? current.loaded() : 0;
            int wanted = techguns.core.gun.GunMath.reloadItems(firing.clipSize(), firing.ammoPerReload(), start);
            while (items < wanted && AmmoInventory.consume(player, ammoType().ammoStacks(variant))) {
                items++;
            }
            loaded = Math.min(firing.clipSize(), start + items * techguns.core.gun.GunMath.shotsPerReloadItem(firing.clipSize(), firing.ammoPerReload()));
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
        if (current.loaded() <= 0 || stats().firing().ammoPerReload() > 1) {
            return;
        }
        AmmoType.Variant variant = ammoType().variant(current.variant());
        float shotsPerBullet = techguns.core.gun.GunMath.shotsPerBullet(stats().firing().clipSize(), stats().firing().ammoPerReload(), ammoType().bulletsPerMag());
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
        GunStats.Firing firing = stats().firing();
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
        if (level.isClientSide && stats().zoom().isPresent() && stats().zoom().get().toggle() && ShooterState.of(player).fireDelay(hand) <= 0) {
            GunClientEffects.get().toggleZoom(stats().zoom().get().fovMultiplier());
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

    /**
     * Techguns 2 {@code GenericGun.addInformation}: loaded ammo, ammo items and damage; with shift also the hand type,
     * damage type, range (or explosion radius), velocity, spread, clip size, reload time, armor penetration and zoom.
     * Values the loaded ammo variant changes are coloured.
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        appendGunTooltip(stack, tooltip, GunClientEffects.get().hasShiftDown());
    }

    /** The gun tooltip lines; {@code detailed} with shift held. */
    public void appendGunTooltip(ItemStack stack, List<Component> tooltip, boolean detailed) {
        GunStats stats = stats();
        DamageModifier mod = factory(stack).modifier();
        tooltip.add(GunTooltip.line("tooltip.techguns.gun.loaded", GunData.loaded(stack), stats.firing().clipSize()));
        if (detailed) {
            tooltip.add(GunTooltip.line("tooltip.techguns.gun.hand_type", GunTooltip.handType(stats.handType())));
        }
        int perReload = stats.firing().ammoPerReload();
        for (ItemStack ammoItem : ammoType().ammoStacks(GunData.variant(stack))) {
            Component name = ammoItem.getHoverName().copy().withStyle(ChatFormatting.WHITE);
            tooltip.add(GunTooltip.line("tooltip.techguns.gun.ammo", perReload > 1 ? Component.literal(perReload + "x ").append(name) : name));
        }
        appendToolTooltip(stack, tooltip, detailed);
        if (detailed) {
            tooltip.add(GunTooltip.line("tooltip.techguns.gun.damage_type", GunTooltip.damageCategory(damageCategory(stack))));
        }
        tooltip.add(damageLine(stats, mod, detailed));
        if (!detailed) {
            tooltip.add(Component.translatable("tooltip.techguns.shift_for_details").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.add(rangeLine(stats, mod));
        float speed = stats.projectile().speed();
        float velocity = mod.velocity(speed);
        tooltip.add(GunTooltip.line("tooltip.techguns.gun.velocity", GunTooltip.modified(GunTooltip.oneDecimal(velocity), speed, velocity))
                .append(GunTooltip.change(speed, velocity, mod.velocityMul(), mod.velocityAdd())));
        float spread = stats.projectile().spread();
        float zoomSpread = stats.zoom().map(zoom -> zoom.spreadMultiplier() * spread).orElse(spread);
        tooltip.add(zoomSpread != spread
                ? GunTooltip.line("tooltip.techguns.gun.spread_zoomed", GunTooltip.number(spread), GunTooltip.number(zoomSpread))
                : GunTooltip.line("tooltip.techguns.gun.spread", GunTooltip.number(spread)));
        tooltip.add(GunTooltip.line("tooltip.techguns.gun.clip_size", stats.firing().clipSize()));
        tooltip.add(GunTooltip.line("tooltip.techguns.gun.reload_time", GunTooltip.number(stats.firing().reloadTicks() * 0.05)));
        if (stats.damage().penetration() > 0.0f) {
            tooltip.add(GunTooltip.line("tooltip.techguns.gun.penetration", GunTooltip.oneDecimal(stats.damage().penetration())));
        }
        stats.zoom().ifPresent(zoom -> tooltip.add(GunTooltip.line("tooltip.techguns.gun.zoom", GunTooltip.number(zoom.fovMultiplier()),
                Component.translatable(zoom.toggle() ? "tooltip.techguns.gun.zoom.toggle" : "tooltip.techguns.gun.zoom.hold"))));
    }

    /** Tooltip lines of the tool side (powered melee tools: mining head, tool classes); {@code detailed} with shift. */
    protected void appendToolTooltip(ItemStack stack, List<Component> tooltip, boolean detailed) {
    }

    /** Damage at short range, the damage beyond the drop and the pellet count (Techguns 2 {@code getTooltipTextDmg}). */
    private static Component damageLine(GunStats stats, DamageModifier mod, boolean detailed) {
        GunStats.Damage damage = stats.damage();
        float max = mod.damage(damage.damage());
        float min = mod.damage(damage.damageMin());
        String text = GunTooltip.oneDecimal(max) + (min < max ? "-" + GunTooltip.oneDecimal(min) : "");
        MutableComponent value = GunTooltip.modified(text, damage.damage(), max);
        if (detailed) {
            value.append(GunTooltip.change(damage.damage(), max, mod.damageMul(), mod.damageAdd()));
        }
        int pellets = stats.projectile().pellets();
        return pellets > 0 ? GunTooltip.line("tooltip.techguns.gun.damage_pellets", value, pellets + 1) : GunTooltip.line("tooltip.techguns.gun.damage", value);
    }

    /** Techguns 2 {@code getTooltipTextRange}: damage drop start, end and lifetime, the range alone, or the explosion radius. */
    private static Component rangeLine(GunStats stats, DamageModifier mod) {
        GunStats.Damage damage = stats.damage();
        float start = mod.range(damage.dropStart());
        float end = mod.range(damage.dropEnd());
        Component from = GunTooltip.modified(GunTooltip.oneDecimal(start), damage.dropStart(), start);
        Component to = GunTooltip.modified(GunTooltip.oneDecimal(end), damage.dropEnd(), end);
        Component change = GunTooltip.change(damage.dropStart(), start, mod.rangeMul(), mod.rangeAdd());
        return switch (stats.rangeTooltip()) {
            case DROP -> {
                int lifetime = Math.round(stats.projectile().lifetimeTicks() * mod.rangeMul() + mod.rangeAdd());
                yield GunTooltip.line("tooltip.techguns.gun.range_drop", from, to, lifetime).append(change);
            }
            case NO_DROP -> GunTooltip.line("tooltip.techguns.gun.range", from).append(change);
            case RADIUS -> GunTooltip.line("tooltip.techguns.gun.radius", from, to).append(change);
        };
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        // no melee hits with guns that shoot on left click
        return shootsWithLeftClick();
    }
}
