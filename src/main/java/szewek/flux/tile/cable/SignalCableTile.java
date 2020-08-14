package szewek.flux.tile.cable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import szewek.fl.signal.ISignalHandler;
import szewek.fl.signal.SignalCapability;
import szewek.fl.util.SideCached;
import szewek.flux.F;

import javax.annotation.Nonnull;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

public final class SignalCableTile extends AbstractCableTile<ISignalHandler> {
	private final BitSet bits = new BitSet(256);
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
		super(F.T.SIGNAL_CABLE, SignalCapability.SIGNAL_CAP);
		for(int i = 0; i < 6; i++) {
			sides[i] = new SignalCableTile.Side(i, sideFlag, bits);
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

	@Override
	public void remove() {
		super.remove();
		signalCache.clear();
	}

	public static final class Side extends AbstractSide<ISignalHandler> implements ISignalHandler {
		private final BitSet bits;

		private Side(int i, AtomicInteger sf, BitSet bits) {
			super(i, sf);
			this.bits = bits;
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
			bits.set(channel, state);
			update();
		}

		private void addSignals(BitSet bitset) {
			bits.or(bitset);
			bitset.or(bits);
			update();
		}

		@Nonnull
		@Override
		public ISignalHandler get() {
			return this;
		}
	}
}
