package techguns.machine;

import net.neoforged.neoforge.energy.EnergyStorage;

/**
 * Machine power buffer (Techguns 2 {@code EnergyStoragePlus}): other mods can only insert, the machine drains it while
 * it works.
 */
public class MachineEnergy extends EnergyStorage {
    private final Runnable onChanged;

    public MachineEnergy(int capacity, Runnable onChanged) {
        super(capacity, capacity, 0);
        this.onChanged = onChanged;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        int received = super.receiveEnergy(toReceive, simulate);
        if (received > 0 && !simulate) {
            onChanged.run();
        }
        return received;
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

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
    }
}
