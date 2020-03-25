package szewek.fl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LogTimer {
	private static final Logger LOGGER = LogManager.getLogger();
	private final String name;
	private int count;
	private long sum;

	public LogTimer(String name) {
		this.name = name;
	}

	public long start() {
		return System.nanoTime();
	}

	public void stop(long t) {
		sum += System.nanoTime() - t;
		count++;

		if (count == 5) {
			LOGGER.debug("Timer [{}] mean: {} ns", name, (float) sum / 5F);
			sum = count = 0;
		}
	}

}
