package szewek.fl.signal;


/**
 * A signal handler. Channel numbers must be between 0 and 255.
 */
public interface ISignalHandler {

	boolean allowsSignalInput(short channel);

	boolean allowsSignalOutput(short channel);

	boolean getSignal(short channel);

	void putSignal(short channel, boolean state);

	default void setSignal(short channel) {
		putSignal(channel, true);
	}

	default void clearSignal(short channel) {
		putSignal(channel, false);
	}

	default boolean swapSignal(short channel, boolean state) {
		boolean b = getSignal(channel);
		putSignal(channel, state);
		return b;
	}
}
