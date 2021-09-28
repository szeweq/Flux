package szewek.fl.energy;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Class for implementing Capability Provider for energy transfer
 */
public abstract class EnergyCapable implements IEnergyReceiver, ICapabilityProvider {
	private final LazyOptional<IEnergyStorage> handler = LazyOptional.of(() -> this);

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == CapabilityEnergy.ENERGY ? handler.cast() : LazyOptional.empty();
	}
}
