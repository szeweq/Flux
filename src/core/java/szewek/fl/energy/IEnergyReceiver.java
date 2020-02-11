package szewek.fl.energy;

import net.minecraftforge.energy.IEnergyStorage;

public interface IEnergyReceiver extends IEnergyStorage {
	@Override
	default boolean canExtract() {
		return false;
	}

	@Override
	default boolean canReceive() {
		return true;
	}

	@Override
	default int extractEnergy(int maxExtract, boolean simulate) {
		return 0;
	}
}
