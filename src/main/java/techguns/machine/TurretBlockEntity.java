package techguns.machine;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import techguns.block.TurretBlock;
import techguns.compat.team.TGTeams;
import techguns.entity.npc.TurretEntity;
import techguns.gun.GunData;
import techguns.gun.GunItem;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGEntities;
import techguns.registry.TGItems;

/**
 * Turret base (Techguns 2 {@code TurretTileEnt}): holds the turret's gun, armor plate, 9 ammo and 9 empty-magazine
 * slots and spawns the {@link TurretEntity} on top. The turret uses 5 FE per tick while active, heals 1 HP for 500
 * FE every 3 s, and after its death the base rebuilds it in 10 s at 50 FE per tick. Redstone mode and security as
 * for other machines; animal targeting and (owner only) the PvP setting are menu buttons.
 */
public class TurretBlockEntity extends TGMachineBlockEntity {
    public static final int SLOT_INPUT_FIRST = 0;
    public static final int SLOT_INPUT_LAST = 8;
    public static final int SLOT_OUTPUT_FIRST = 9;
    public static final int SLOT_OUTPUT_LAST = 17;
    public static final int SLOT_WEAPON = 18;
    public static final int SLOT_ARMOR = 19;

    public static final int ENERGY_CAPACITY = 5000;
    public static final int REPAIR_TIME = 10 * 20;
    public static final int REPAIR_POWER = 50;
    public static final int OPERATION_POWER = 5;
    public static final int HEAL_POWER = 500;
    public static final int HEAL_DELAY = 3 * 20;
    /** Health of a rebuilt turret. */
    public static final float REPAIRED_HEALTH = 10.0f;
    /** Ticks to wait for a saved turret to load before rebuilding it. */
    private static final int TURRET_LOAD_GRACE = 100;

    public static final int BUTTON_TARGET_ANIMALS = BUTTON_MACHINE;
    public static final int BUTTON_PVP = BUTTON_MACHINE + 1;

    public static final int DATA_DEAD = DATA_COUNT;
    public static final int DATA_REPAIR_TIME = DATA_COUNT + 1;
    /** Turret health times 10. */
    public static final int DATA_HEALTH = DATA_COUNT + 2;
    public static final int DATA_ATTACK_ANIMALS = DATA_COUNT + 3;
    public static final int DATA_PVP = DATA_COUNT + 4;

    /** Techguns 2 turret armor plates: tier 1-4, 5 armor per tier. */
    private static final List<java.util.function.Supplier<? extends Item>> ARMORS = List.of(TGItems.TURRET_ARMOR_IRON, TGItems.TURRET_ARMOR_STEEL,
            TGItems.TURRET_ARMOR_OBSIDIAN_STEEL, TGItems.TURRET_ARMOR_CARBON);

    @Nullable
    private UUID turretId;
    @Nullable
    private TurretEntity turret;
    private int missingTicks;
    private boolean dead;
    private int repairTime;
    private int healTime;
    private boolean attackAnimals;
    private int pvpSetting = TGTeams.PVP_NONE;
    /** Last synced gun and plate, to send an update when they change. */
    private ItemStack shownWeapon = ItemStack.EMPTY;
    private ItemStack shownArmor = ItemStack.EMPTY;
    /** Client: gun and plate for the renderers. */
    private ItemStack clientWeapon = ItemStack.EMPTY;
    private ItemStack clientArmor = ItemStack.EMPTY;

    public TurretBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.TURRET.get(), pos, state, SLOT_ARMOR + 1, ENERGY_CAPACITY);
    }

    // --- slots ---

    public static int armorTier(ItemStack stack) {
        for (int i = 0; i < ARMORS.size(); i++) {
            if (stack.is(ARMORS.get(i).get())) {
                return i + 1;
            }
        }
        return 0;
    }

    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        if (slot == SLOT_WEAPON) {
            return stack.getItem() instanceof GunItem;
        }
        if (slot == SLOT_ARMOR) {
            return armorTier(stack) > 0;
        }
        return slot >= SLOT_INPUT_FIRST && slot <= SLOT_INPUT_LAST;
    }

    @Override
    protected boolean isOutputSlot(int slot) {
        return slot >= SLOT_OUTPUT_FIRST && slot <= SLOT_OUTPUT_LAST;
    }

    public int targetSlot(ItemStack stack) {
        return stack.getItem() instanceof GunItem ? SLOT_WEAPON : armorTier(stack) > 0 ? SLOT_ARMOR : SLOT_INPUT_FIRST;
    }

    public int targetSlotCount(int target) {
        return target == SLOT_INPUT_FIRST ? SLOT_INPUT_LAST - SLOT_INPUT_FIRST + 1 : 1;
    }

    public ItemStack getWeapon() {
        return level != null && level.isClientSide ? clientWeapon : inventory.getStackInSlot(SLOT_WEAPON);
    }

    public ItemStack getArmor() {
        return level != null && level.isClientSide ? clientArmor : inventory.getStackInSlot(SLOT_ARMOR);
    }

    /** Techguns 2 {@code getTurretTier}: 0 without a plate, 1 iron ... 4 carbon (base and turret textures). */
    public int getTier() {
        return armorTier(getArmor());
    }

    /** Techguns 2 {@code getTurretArmorValue}. */
    public int armorValue() {
        return getTier() * 5;
    }

    public boolean isHanging() {
        return getBlockState().hasProperty(TurretBlock.HANGING) && getBlockState().getValue(TurretBlock.HANGING);
    }

    public boolean isAttackAnimals() {
        return attackAnimals;
    }

    public int getPvpSetting() {
        return pvpSetting;
    }

    public boolean isDead() {
        return dead;
    }

    // --- ammo ---

    /** Techguns 2 {@code consumeAmmo}: one shot from the gun, reloading it from the ammo slots when empty. */
    public boolean consumeAmmo() {
        ItemStack weapon = inventory.getStackInSlot(SLOT_WEAPON);
        if (!(weapon.getItem() instanceof GunItem gun)) {
            return false;
        }
        if (GunData.loaded(weapon) <= 0) {
            gun.reloadFrom(weapon, inventory, SLOT_INPUT_FIRST, SLOT_INPUT_LAST, empty -> insertAll(SLOT_OUTPUT_FIRST, SLOT_OUTPUT_LAST,
                    List.of(empty), false).stream().filter(rest -> !rest.isEmpty()).forEach(rest -> Containers.dropItemStack(level,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, rest)));
        }
        if (GunData.loaded(weapon) >= 1) {
            GunData.consume(weapon, 1);
            setChanged();
            return true;
        }
        return false;
    }

    // --- the turret ---

    /** Whether {@code entity} is this base's turret. */
    public boolean isMounted(TurretEntity entity) {
        return turretId != null && turretId.equals(entity.getUUID());
    }

    @Nullable
    public TurretEntity getTurret() {
        if (turret != null && !turret.isRemoved()) {
            return turret;
        }
        if (turretId != null && level instanceof ServerLevel server && server.getEntity(turretId) instanceof TurretEntity found) {
            turret = found;
            return found;
        }
        return null;
    }

    private void spawnTurret(ServerLevel level, float health) {
        TurretEntity entity = TGEntities.TURRET.get().create(level);
        if (entity == null) {
            return;
        }
        entity.mount(this);
        entity.setHealth(health);
        level.addFreshEntity(entity);
        turret = entity;
        turretId = entity.getUUID();
        missingTicks = 0;
        dead = false;
        setChanged();
        sendUpdate();
    }

    /** Techguns 2 {@code onTurretDeath}: the base starts rebuilding. */
    public void onTurretDeath() {
        turret = null;
        turretId = null;
        dead = true;
        repairTime = REPAIR_TIME;
        setChanged();
        sendUpdate();
    }

    /** The base is broken: the turret goes too. */
    public void removeTurret() {
        TurretEntity current = getTurret();
        if (current != null) {
            current.discard();
        }
        turret = null;
        turretId = null;
    }

    /** Techguns 2 {@code TurretTileEnt.update} (server). */
    public static void serverTick(Level level, BlockPos pos, BlockState state, TurretBlockEntity base) {
        base.tickTurret((ServerLevel) level);
    }

    private void tickTurret(ServerLevel level) {
        boolean enabled = getRedstoneMode().allows(hasSignal());
        if (dead) {
            if (enabled) {
                if (consumePower(REPAIR_POWER)) {
                    repairTime--;
                }
                if (repairTime <= 0) {
                    repairTime = 0;
                    spawnTurret(level, REPAIRED_HEALTH);
                }
            }
        } else {
            TurretEntity current = getTurret();
            if (current == null) {
                // a placed base builds its turret; a saved one waits for it to load first
                if (turretId == null || ++missingTicks > TURRET_LOAD_GRACE) {
                    spawnTurret(level, (float) TurretEntity.MAX_HEALTH);
                }
            } else if (!current.isAlive()) {
                onTurretDeath();
            } else {
                boolean active = enabled && consumePower(OPERATION_POWER);
                current.setNoAi(!active);
                if (!active) {
                    current.setTarget(null);
                }
                if (enabled) {
                    if (healTime <= 0) {
                        if (current.getHealth() < current.getMaxHealth() && consumePower(HEAL_POWER)) {
                            current.heal(1.0f);
                            healTime = HEAL_DELAY;
                        }
                    } else {
                        healTime--;
                    }
                }
            }
        }
        ItemStack weapon = inventory.getStackInSlot(SLOT_WEAPON);
        ItemStack armor = inventory.getStackInSlot(SLOT_ARMOR);
        if (!ItemStack.isSameItem(weapon, shownWeapon) || !ItemStack.isSameItem(armor, shownArmor)) {
            shownWeapon = weapon.copy();
            shownArmor = armor.copy();
            sendUpdate();
        }
    }

    @Override
    protected void checkAndStartOperation() {
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
    }

    @Override
    protected boolean pressMachineButton(Player player, int id) {
        if (id == BUTTON_TARGET_ANIMALS) {
            attackAnimals = !attackAnimals;
            return true;
        }
        if (id == BUTTON_PVP && isOwner(player)) {
            pvpSetting = TGTeams.nextPvpSetting(pvpSetting);
            return true;
        }
        return false;
    }

    // --- menu data ---

    @Override
    protected int extraDataCount() {
        return 5;
    }

    @Override
    protected int getExtraData(int index) {
        TurretEntity current = level instanceof ServerLevel ? getTurret() : null;
        return switch (index) {
            case DATA_DEAD -> dead ? 1 : 0;
            case DATA_REPAIR_TIME -> repairTime;
            case DATA_HEALTH -> current != null ? Math.round(current.getHealth() * 10) : 0;
            case DATA_ATTACK_ANIMALS -> attackAnimals ? 1 : 0;
            case DATA_PVP -> pvpSetting;
            default -> 0;
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("techguns.container.turretbase");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TurretMenu(id, inventory, this);
    }

    // --- saving and syncing ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (turretId != null) {
            tag.putUUID("turret", turretId);
        }
        tag.putInt("repair_time", repairTime);
        tag.putInt("heal_time", healTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        turretId = tag.hasUUID("turret") ? tag.getUUID("turret") : null;
        repairTime = tag.getInt("repair_time");
        healTime = tag.getInt("heal_time");
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        tag.putBoolean("dead", dead);
        tag.putBoolean("attack_animals", attackAnimals);
        tag.putByte("pvp", (byte) pvpSetting);
        if (level != null) {
            HolderLookup.Provider registries = level.registryAccess();
            tag.put("weapon", inventory.getStackInSlot(SLOT_WEAPON).saveOptional(registries));
            tag.put("armor", inventory.getStackInSlot(SLOT_ARMOR).saveOptional(registries));
        }
    }

    @Override
    protected void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        dead = tag.getBoolean("dead");
        attackAnimals = tag.getBoolean("attack_animals");
        pvpSetting = tag.getByte("pvp");
    }

    @Override
    protected void loadUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadUpdateTag(tag, registries);
        clientWeapon = ItemStack.parseOptional(registries, tag.getCompound("weapon"));
        clientArmor = ItemStack.parseOptional(registries, tag.getCompound("armor"));
    }
}
