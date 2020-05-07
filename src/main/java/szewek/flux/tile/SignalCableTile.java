package szewek.flux.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import szewek.fl.signal.ISignalHandler;
import szewek.fl.signal.SignalCapability;
import szewek.fl.util.SideCached;
import szewek.flux.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;

public class SignalCableTile extends AbstractCableTile {
	private final Side[] sides = new Side[6];
	private BitSet bits = new BitSet(256);
	private final SideCached<ISignalHandler> signalCache = new SideCached<>(dir -> {
		assert world != null;
		TileEntity te = world.getTileEntity(pos.offset(dir));
		if (te == null) {
			return LazyOptional.empty();
		}
		if (te instanceof SignalCableTile) {
			return ((SignalCableTile) te).getSide(dir.getOpposite());
		}
		return te.getCapability(SignalCapability.SIGNAL_CAP, dir.getOpposite());
	});

	public SignalCableTile() {
		super(F.T.SIGNAL_CABLE);
		for(int i = 0; i < 6; i++) {
			sides[i] = new Side(i);
		}
	}

	@Override
	protected void updateSide(Direction dir) {
		ISignalHandler ish = signalCache.getCached(dir);
		if (ish != null) {
			if (ish instanceof Side) {
				((Side) ish).addSignals(bits);
			} else {
				for (short ch = 0; ch < 256; ch++) {
					if (ish.allowsSignalInput(ch)) {
						ish.putSignal(ch, bits.get(ch));
					} else if (ish.allowsSignalOutput(ch)) {
						bits.set(ch, ish.getSignal(ch));
					}
				}
			}
		}
	}

	public LazyOptional<ISignalHandler> getSide(Direction dir) {
		return sides[dir.getIndex()].lazy;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!removed && cap == SignalCapability.SIGNAL_CAP && side != null) {
			return sides[side.getIndex()].lazy.cast();
		} else {
			return super.getCapability(cap, side);
		}
	}

	@Override
	public void remove() {
		super.remove();
		signalCache.clear();
		for (Side s : sides) {
			s.lazy.invalidate();
		}
	}

	public final class Side implements ISignalHandler, NonNullSupplier<ISignalHandler> {
		private final byte bit;
		private final LazyOptional<ISignalHandler> lazy = LazyOptional.of(this);

		private Side(int i) {
			bit = (byte) (1 << i);
		}

		@Override
		public boolean allowsSignalInput(short channel) {
			return true;
		}

		@Override
		public boolean allowsSignalOutput(short channel) {
			return true;
		}

		@Override
		public boolean getSignal(short channel) {
			return bits.get(channel);
		}

		@Override
		public void putSignal(short channel, boolean state) {
			sideFlag |= bit;
			bits.set(channel, state);
		}

		private void addSignals(BitSet bitset) {
			sideFlag |= bit;
			bits.or(bitset);
			bitset.or(bits);
		}

		@Nonnull
		@Override
		public ISignalHandler get() {
			return this;
		}
	}
}
