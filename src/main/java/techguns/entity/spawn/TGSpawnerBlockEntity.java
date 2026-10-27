package techguns.entity.spawn;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import techguns.entity.npc.TGNpc;
import techguns.registry.TGBlockEntities;

/**
 * Techguns 2 {@code TGSpawnerTileEnt}: spawns NPCs of its weighted mob types around itself, at most {@code maxActive}
 * at a time, until {@code mobsLeft} of them were killed; then the spawner block vanishes. Despawned NPCs don't count.
 * Spawned NPCs remember the spawner ({@link SpawnerLink}) and stay within 10 blocks of it.
 */
public class TGSpawnerBlockEntity extends BlockEntity {
    private record MobType(ResourceLocation id, int weight) {
    }

    private static final int HOME_RADIUS = 10;

    private int delay = 200;
    private int spawnDelay = 200;
    private int mobsLeft = 5;
    private int maxActive = 3;
    private int spawnHeightOffset;
    private int spawnRange = 2;
    private final List<MobType> mobTypes = new ArrayList<>();
    private ItemStack weaponOverride = ItemStack.EMPTY;
    /** Not saved: NPCs re-register on their first tick after loading. */
    private final List<TGNpc> activeMobs = new ArrayList<>();

    public TGSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.TG_SPAWNER.get(), pos, state);
    }

    public void addMobType(EntityType<? extends TGNpc> type, int weight) {
        mobTypes.add(new MobType(BuiltInRegistries.ENTITY_TYPE.getKey(type), weight));
        setChanged();
    }

    public void setParams(int mobsLeft, int maxActive, int spawnDelay, int spawnRange) {
        this.mobsLeft = mobsLeft;
        this.maxActive = maxActive;
        this.spawnDelay = spawnDelay;
        this.delay = spawnDelay;
        this.spawnRange = spawnRange;
        setChanged();
    }

    public void setWeaponOverride(ItemStack weapon) {
        this.weaponOverride = weapon.copy();
        setChanged();
    }

    public void setSpawnHeightOffset(int offset) {
        this.spawnHeightOffset = offset;
        setChanged();
    }

    public int mobsLeft() {
        return mobsLeft;
    }

    public int activeMobs() {
        return activeMobs.size();
    }

    public boolean hasMobTypes() {
        return !mobTypes.isEmpty();
    }

    public void killed(TGNpc npc) {
        if (activeMobs.remove(npc)) {
            mobsLeft--;
            setChanged();
        }
    }

    public void despawned(TGNpc npc) {
        activeMobs.remove(npc);
    }

    public void relink(TGNpc npc) {
        if (npc.isAlive() && !activeMobs.contains(npc)) {
            activeMobs.add(npc);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TGSpawnerBlockEntity spawner) {
        spawner.tick((ServerLevel) level);
    }

    private void tick(ServerLevel level) {
        delay--;
        if (delay <= 0) {
            delay = spawnDelay;
            if (activeMobs.size() < Math.min(maxActive, mobsLeft) && hasMobTypes()) {
                if (level.getDifficulty() != Difficulty.PEACEFUL) {
                    spawn(level);
                }
            } else {
                activeMobs.removeIf(npc -> !npc.isAlive() || npc.level() != level);
            }
        }
        if (mobsLeft <= 0) {
            level.removeBlock(worldPosition, false);
        }
    }

    private void spawn(ServerLevel level) {
        int roll = level.random.nextInt(mobTypes.stream().mapToInt(MobType::weight).sum());
        MobType picked = mobTypes.getLast();
        for (MobType type : mobTypes) {
            roll -= type.weight;
            if (roll < 0) {
                picked = type;
                break;
            }
        }
        Entity entity = BuiltInRegistries.ENTITY_TYPE.get(picked.id).create(level);
        if (!(entity instanceof TGNpc npc)) {
            if (entity != null) {
                entity.discard();
            }
            return;
        }
        double x = worldPosition.getX() + (level.random.nextDouble() - level.random.nextDouble()) * spawnRange + 0.5;
        double y = worldPosition.getY() + 1 + spawnHeightOffset;
        double z = worldPosition.getZ() + (level.random.nextDouble() - level.random.nextDouble()) * spawnRange + 0.5;
        npc.moveTo(x, y, z, level.random.nextFloat() * 360.0f, 0.0f);
        EventHooks.finalizeMobSpawn(npc, level, level.getCurrentDifficultyAt(npc.blockPosition()), MobSpawnType.SPAWNER, null);
        npc.restrictTo(worldPosition, HOME_RADIUS);
        SpawnerLink.set(npc, worldPosition);
        level.addFreshEntity(npc);
        level.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, worldPosition, 0);
        if (!weaponOverride.isEmpty()) {
            npc.setItemSlot(EquipmentSlot.MAINHAND, weaponOverride.copy());
        }
        npc.spawnAnim();
        activeMobs.add(npc);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("mobsLeft", mobsLeft);
        tag.putInt("delay", delay);
        tag.putInt("maxActive", maxActive);
        tag.putInt("spawnDelay", spawnDelay);
        tag.putInt("spawnRange", spawnRange);
        tag.putInt("spawnHeightOffset", spawnHeightOffset);
        ListTag types = new ListTag();
        for (MobType type : mobTypes) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", type.id.toString());
            entry.putInt("weight", type.weight);
            types.add(entry);
        }
        tag.put("mobTypes", types);
        if (!weaponOverride.isEmpty()) {
            tag.put("weapon", weaponOverride.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mobsLeft = tag.getInt("mobsLeft");
        delay = tag.getInt("delay");
        spawnDelay = tag.getInt("spawnDelay") < 1 ? 200 : tag.getInt("spawnDelay");
        maxActive = Math.max(tag.getInt("maxActive"), 1);
        spawnRange = tag.getInt("spawnRange");
        spawnHeightOffset = tag.getInt("spawnHeightOffset");
        mobTypes.clear();
        for (Tag entry : tag.getList("mobTypes", Tag.TAG_COMPOUND)) {
            CompoundTag type = (CompoundTag) entry;
            ResourceLocation id = ResourceLocation.tryParse(type.getString("id"));
            if (id != null) {
                mobTypes.add(new MobType(id, Math.max(type.getInt("weight"), 1)));
            }
        }
        weaponOverride = tag.contains("weapon") ? ItemStack.parseOptional(registries, tag.getCompound("weapon")) : ItemStack.EMPTY;
    }
}
