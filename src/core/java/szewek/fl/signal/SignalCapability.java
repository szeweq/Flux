package szewek.fl.signal;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.BitSet;

public final class SignalCapability {
	@CapabilityInject(ISignalHandler.class)
	public static Capability<ISignalHandler> SIGNAL_CAP = null;

	public static void register() {
		CapabilityManager.INSTANCE.register(ISignalHandler.class, new Capability.IStorage<ISignalHandler>() {
			@Override
			public INBT writeNBT(Capability<ISignalHandler> capability, ISignalHandler instance, Direction side) {
				if (instance instanceof SignalHandler) {
					long[] bits = ((SignalHandler) instance).bits.toLongArray();
					return new LongArrayNBT(bits);
				}
				return new LongArrayNBT(new long[0]);
			}

			@Override
			public void readNBT(Capability<ISignalHandler> capability, ISignalHandler instance, Direction side, INBT nbt) {
				if (!(instance instanceof SignalHandler)) {
					throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
				}
				long[] arr = ((LongArrayNBT) nbt).getAsLongArray();
				BitSet newBits = BitSet.valueOf(arr);
				BitSet bits = ((SignalHandler) instance).bits;
				bits.clear();
				bits.or(newBits);
			}
		}, SignalHandler::new);
	}

	private SignalCapability() {}
}
