package szewek.flux.energy;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class EnergyCapable implements IEnergyReceiver, ICapabilityProvider {
	private final LazyOptional<IEnergyStorage> handler = LazyOptional.of(() -> this);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return cap == CapabilityEnergy.ENERGY ? handler.cast() : LazyOptional.empty();
	}
}
