package szewek.fl.network;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class interacting with Flux+ API.
 */
public final class FluxPlus {
	private static final String GA_URL = "https://www.google-analytics.com/collect";
	private static final String CLIENT_ID = UUID.randomUUID().toString();
	private static final String HOST = "https://fluxplus.herokuapp.com/api";
	private static final Logger LOGGER = LogManager.getLogger("Flux+");
	static final Gson GSON = new GsonBuilder().setLenient().create();
	private static final ExecutorService EXEC = new ThreadPoolExecutor(0, 2, 30L, TimeUnit.SECONDS, new SynchronousQueue<>());

	private FluxPlus() {}

	private static APICall connect(String path) throws IOException {
		final HttpURLConnection huc = (HttpURLConnection) new URL(path).openConnection();
		return new APICall(huc);
	}

	private static void sendEvent(final String cat, final String name) {
		try {
			connect(GA_URL)
					.postString("v=1&t=event&tid=UA-177867488-1&cid=" + CLIENT_ID + "&ec=" + cat + "&ea=" + name, "application/x-www-form-urlencoded;charset=utf-8")
					.voidResponse();
		} catch (IOException e) {
			LOGGER.error("Exception while sending an event", e);
		}
	}

	public static void putAction(final String cat, final String type) {
		EXEC.execute(() -> sendEvent(cat, type));
	}

	public static void reportRecipeCompatError(final String recipeType, final String className, final String msg) {
		EXEC.execute(() -> {
			try {
				Map<String, String> m = ImmutableMap.of("recipeType", recipeType, "class", className, "msg", msg);
				connect(HOST + "/report/recipeCompat").post(m).response(Boolean.TYPE);
			} catch (Exception e) {
				LOGGER.error("Exception while sending recipe compat error", e);
			}
		});
	}


}
