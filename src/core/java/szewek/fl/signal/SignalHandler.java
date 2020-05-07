package szewek.fl.signal;


import java.util.BitSet;

public class SignalHandler implements ISignalHandler {

	protected BitSet bits = new BitSet(256);

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
	}
}
