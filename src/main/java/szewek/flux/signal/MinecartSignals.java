package szewek.flux.signal;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import szewek.fl.signal.ISignalHandler;
import szewek.fl.signal.SignalCapability;
import szewek.fl.signal.SignalHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MinecartSignals extends SignalHandler implements ICapabilityProvider, INBTSerializable<INBT> {
	private boolean valid = true;
	private final LazyOptional<ISignalHandler> handler = LazyOptional.of(() -> this);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		return valid && cap == SignalCapability.SIGNAL_CAP ? handler.cast() : LazyOptional.empty();
	}

	@Override
	public INBT serializeNBT() {
		return SignalCapability.SIGNAL_CAP.writeNBT(this, null);
	}

	@Override
	public void deserializeNBT(INBT nbt) {
		SignalCapability.SIGNAL_CAP.readNBT(this, null, nbt);
	}

	public void invalidate() {
		valid = false;
	}
}
