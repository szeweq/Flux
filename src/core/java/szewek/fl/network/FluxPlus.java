package szewek.fl.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FluxPlus {
	private static final String HOST = "https://fluxplus.herokuapp.com/api";
	private static final Logger LOGGER = LogManager.getLogger("Flux+");
	static final Gson GSON = new GsonBuilder().setLenient().create();
	private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
	private static final Set<String> ACTIONS = new HashSet<>();

	private FluxPlus() {}

	private static APICall connect(String path) throws IOException {
		HttpURLConnection huc = (HttpURLConnection) new URL(HOST + path).openConnection();
		return new APICall(huc);
	}

	public static void putAction(final String type) {
		if (ACTIONS.contains(type)) return;
		EXEC.execute(() -> {
			try {
				boolean b = connect("/action?type=" + type)
						.response(Boolean.TYPE);
				if (b) ACTIONS.add(type);
				else LOGGER.warn("Action type {} is not acceptable", type);
			} catch (Exception e) {
				LOGGER.error("Exception while registering an action", e);
			}
		});
	}

	public static void sendItemMap(final Map<String, Object> fullMap) {
		EXEC.execute(() -> {
			try {
				connect("/items")
						.post(fullMap)
						.response(Boolean.TYPE);
			} catch (Exception e) {
				LOGGER.error("Exception while registering item map", e);
			}
		});
	}
	public static void sendSerializerNames(final Map<String, Map<String, String>> fullMap) {
		EXEC.execute(() -> {
			try {
				connect("/serializers")
						.post(fullMap)
						.response(Boolean.TYPE);
			} catch (Exception e) {
				LOGGER.error("Exception while registering serializer map", e);
			}
		});
	}
	public static void sendRecipeInfos(final Map<String, Object> fullMap) {
		EXEC.execute(() -> {
			try {
				connect("/recipes")
						.post(fullMap)
						.response(Boolean.TYPE);
			} catch (Exception e) {
				LOGGER.error("Exception while registering recipe information map", e);
			}
		});
	}
}
