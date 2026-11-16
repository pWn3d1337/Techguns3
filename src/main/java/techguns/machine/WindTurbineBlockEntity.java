package techguns.machine;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import techguns.TGConfig;
import techguns.core.power.SolarOutput;
import techguns.core.power.WindOutput;
import techguns.core.transport.EnergySplit;
import techguns.registry.TGBlockEntities;

/**
 * One Wind Turbine segment (Techguns 3). The lowest segment of a column is the generator: it ticks, looks at the column
 * once a second (segments, their height factors, the biome and sky at the top, see {@link WindOutput}), keeps the
 * column's energy and pushes it into the blocks on its sides and bottom. Upper segments only add rotor; energy they
 * hold (they were a base before) moves into the base. The base syncs its wind factor to the client, which turns the
 * whole column's rotor with it.
 */
public class WindTurbineBlockEntity extends BlockEntity {
    /** FE each segment adds to the column's storage. */
    public static final int CAPACITY = 4000;
    /** The column is looked at once in this many ticks. */
    static final int SCAN_TICKS = 20;
    private static final Direction[] OUTPUTS = {Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private int energy;
    private final double[] remainder = {0.0};
    // server, base only: the column as last looked at
    private int segments;
    private double heightFactorSum;
    private double biomeFactor = 1.0;
    private boolean openSky;
    private double lastPerTick;
    @Nullable
    private List<BlockCapabilityCache<IEnergyStorage, Direction>> targets;
    /** Wind factor of the column (height average x biome x weather, 0 without open sky); synced to the client. */
    private float wind;
    // client: rotor
    private float angle;
    private float prevAngle;
    private float speed;
    /** Client: the column's base, set by the base for every segment above it. */
    @Nullable
    private WindTurbineBlockEntity clientBase;

    private final IEnergyStorage storage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            int taken = Math.max(0, Math.min(energy, toExtract));
            if (!simulate && taken > 0) {
                energy -= taken;
            }
            return taken;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public WindTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.WIND_TURBINE.get(), pos, state);
    }

    /** Whether this is the lowest segment of its column (the generator). */
    public boolean isBase() {
        return !getBlockState().getValue(WindTurbineBlock.DOWN);
    }

    /** The column's energy, offered by the base only. */
    public IEnergyStorage storage() {
        return storage;
    }

    /** The lowest segment of this column. */
    public WindTurbineBlockEntity base() {
        if (level == null) {
            return this;
        }
        BlockPos.MutableBlockPos pos = getBlockPos().mutable();
        WindTurbineBlockEntity base = this;
        while (true) {
            pos.move(Direction.DOWN);
            if (level.isOutsideBuildHeight(pos) || !(level.getBlockEntity(pos) instanceof WindTurbineBlockEntity below)) {
                return base;
            }
            base = below;
        }
    }

    // --- server ---

    void serverTick() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (segments == 0 || (server.getGameTime() + getBlockPos().asLong()) % SCAN_TICKS == 0) {
            scan(server);
        }
        double weather = WindOutput.weatherFactor(server.getRainLevel(1.0f), server.getThunderLevel(1.0f));
        lastPerTick = WindOutput.perTick(TGConfig.WIND_TURBINE_FE_PER_TICK.get(), heightFactorSum, biomeFactor, weather, openSky);
        int made = SolarOutput.produce(lastPerTick, remainder);
        if (made > 0) {
            energy = Math.min(capacity(), energy + made);
        }
        push();
    }

    /** Walks up the column: segments, height factors, the sky and biome at the top; takes in energy left in upper segments. */
    private void scan(ServerLevel server) {
        int count = 0;
        double sum = 0.0;
        int seaLevel = server.getSeaLevel();
        BlockPos.MutableBlockPos pos = getBlockPos().mutable();
        BlockPos top = getBlockPos();
        while (!server.isOutsideBuildHeight(pos) && server.getBlockEntity(pos) instanceof WindTurbineBlockEntity segment) {
            count++;
            sum += WindOutput.heightFactor(pos.getY() + 0.5 - seaLevel);
            if (segment != this && segment.energy > 0) {
                energy += segment.energy;
                segment.energy = 0;
                segment.setChanged();
            }
            top = pos.immutable();
            pos.move(Direction.UP);
        }
        segments = Math.max(1, count);
        heightFactorSum = sum;
        energy = Math.min(capacity(), energy);
        openSky = server.canSeeSky(top.above());
        biomeFactor = BiomeEfficiency.wind(server.getBiome(top)).multiplier();
        double weather = WindOutput.weatherFactor(server.getRainLevel(1.0f), server.getThunderLevel(1.0f));
        float newWind = openSky ? (float) (heightFactorSum / segments * biomeFactor * weather) : 0.0f;
        if (Math.abs(newWind - wind) > 0.005f) {
            wind = newWind;
            BlockState state = getBlockState();
            server.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        }
        if (targets == null) {
            targets = new ArrayList<>(OUTPUTS.length);
            for (Direction side : OUTPUTS) {
                targets.add(BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, server, getBlockPos().relative(side), side.getOpposite()));
            }
        }
        setChanged();
    }

    private void push() {
        if (energy <= 0 || targets == null) {
            return;
        }
        List<IEnergyStorage> storages = new ArrayList<>(targets.size());
        for (BlockCapabilityCache<IEnergyStorage, Direction> cache : targets) {
            IEnergyStorage target = cache.getCapability();
            if (target != null && target != storage && target.canReceive()) {
                storages.add(target);
            }
        }
        if (storages.isEmpty()) {
            return;
        }
        int[] accept = new int[storages.size()];
        for (int i = 0; i < accept.length; i++) {
            accept[i] = storages.get(i).receiveEnergy(energy, true);
        }
        int[] give = EnergySplit.waterFill(energy, accept);
        for (int i = 0; i < give.length; i++) {
            if (give[i] > 0) {
                energy -= storages.get(i).receiveEnergy(Math.min(give[i], energy), false);
            }
        }
    }

    // --- client ---

    void clientTick() {
        if (level == null) {
            return;
        }
        if ((level.getGameTime() + getBlockPos().asLong()) % SCAN_TICKS == 0 || clientBase != this) {
            clientBase = this;
            BlockPos.MutableBlockPos pos = getBlockPos().mutable().move(Direction.UP);
            while (!level.isOutsideBuildHeight(pos) && level.getBlockEntity(pos) instanceof WindTurbineBlockEntity segment) {
                segment.clientBase = this;
                pos.move(Direction.UP);
            }
        }
        // ease towards the wind's speed, so gusts and weather changes do not jump
        speed += (WindOutput.degreesPerTick(wind) - speed) * 0.05f;
        prevAngle = angle;
        angle += speed;
        if (angle >= 360.0f) {
            angle -= 360.0f;
            prevAngle -= 360.0f;
        }
    }

    /** Client: the rotor angle in degrees of this segment's column. */
    public float rotorAngle(float partialTick) {
        WindTurbineBlockEntity base = isBase() ? this : clientBase;
        if (base == null || base.isRemoved()) {
            return 0.0f;
        }
        return base.prevAngle + (base.angle - base.prevAngle) * partialTick;
    }

    // --- queries (base) ---

    public int capacity() {
        return Math.max(1, segments) * CAPACITY;
    }

    public int energy() {
        return energy;
    }

    public int segments() {
        return Math.max(1, segments);
    }

    public double lastPerTick() {
        return lastPerTick;
    }

    /** Average height factor of the column's segments. */
    public double averageHeightFactor() {
        return segments == 0 ? 0.0 : heightFactorSum / segments;
    }

    public double biomeFactor() {
        return biomeFactor;
    }

    public boolean openSky() {
        return openSky;
    }

    public float wind() {
        return wind;
    }

    /** Test hook: sets the stored energy. */
    public void setEnergy(int energy) {
        this.energy = Math.max(0, energy);
    }

    // --- saving and sync ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = Math.max(0, tag.getInt("energy"));
        if (tag.contains("wind")) {
            wind = tag.getFloat("wind");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("wind", wind);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
