package techguns.machine;

import net.neoforged.neoforge.energy.EnergyStorage;

/**
 * Machine power buffer (Techguns 2 {@code EnergyStoragePlus}): other mods can only insert, the machine drains it while
 * it works. Generators use an extract-only variant ({@link #generator}).
 */
public class MachineEnergy extends EnergyStorage {
    private final Runnable onChanged;

    public MachineEnergy(int capacity, Runnable onChanged) {
        this(capacity, capacity, 0, onChanged);
    }

    protected MachineEnergy(int capacity, int maxReceive, int maxExtract, Runnable onChanged) {
        super(capacity, maxReceive, maxExtract);
        this.onChanged = onChanged;
    }

    /** A generator buffer: nothing can insert, neighbours and cables take up to {@code maxExtract} per call. */
    public static MachineEnergy generator(int capacity, int maxExtract, Runnable onChanged) {
        return new MachineEnergy(capacity, 0, maxExtract, onChanged);
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        int received = super.receiveEnergy(toReceive, simulate);
        if (received > 0 && !simulate) {
            onChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        int extracted = super.extractEnergy(toExtract, simulate);
        if (extracted > 0 && !simulate) {
            onChanged.run();
        }
        return extracted;
    }

    /** Takes {@code amount} if all of it is stored; false (and nothing taken) otherwise. */
    public boolean consume(int amount) {
        if (energy < amount) {
            return false;
        }
        energy -= amount;
        onChanged.run();
        return true;
    }

    /** A generator made {@code amount} (capped at the capacity). */
    public void produce(int amount) {
        if (amount > 0) {
            energy = Math.min(capacity, energy + amount);
            onChanged.run();
        }
    }

    /** Free room in the buffer. */
    public int space() {
        return capacity - energy;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
    }
}
