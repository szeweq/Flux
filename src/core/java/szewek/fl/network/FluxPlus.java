package szewek.fl.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class interacting with Flux+ API.
 */
public final class FluxPlus {
	// THIS IS TEMPORARY! MIGHT USE SOMETHING LESS ANNOYING
	private static final String GA_URL = "https://www.google-analytics.com/collect";
	private static final String FORM_TYPE = "application/x-www-form-urlencoded;charset=utf-8";
	private static final String CLIENT_ID = UUID.randomUUID().toString();
	private static final Logger LOGGER = LogManager.getLogger("Flux+");
	static final Gson GSON = new GsonBuilder().setLenient().create();
	private static final ExecutorService EXEC = new ThreadPoolExecutor(0, 2, 30L, TimeUnit.SECONDS, new SynchronousQueue<>());

	private FluxPlus() {}

	public static void init() {
		send("t=pageview&dp=%2F");
	}

	private static void send(final String form) {
		EXEC.execute(() -> {
			try {
				APICall.to(GA_URL)
						.postString("v=1&tid=UA-177867488-1&cid=" + CLIENT_ID + "&" + form, FORM_TYPE)
						.checkStatus();
			} catch (IOException e) {
				LOGGER.error("Exception while sending an event", e);
			}
		});
	}

	public static void putAction(final String cat, final String type) {
		send("t=event&ec=" + cat + "&ea=" + type);
	}
}
