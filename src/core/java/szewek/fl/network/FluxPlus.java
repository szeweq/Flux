package szewek.fl.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class FluxPlus {
	private static final String HOST = "https://fluxplus.herokuapp.com/api";
	private static final Logger LOGGER = LogManager.getLogger("Flux+");
	private static final Gson GSON = new GsonBuilder().setLenient().create();
	private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
	private static final Set<String> ACTIONS = new HashSet<>();
	public static final FluxPlus API = new FluxPlus();


	private FluxPlus() {}

	private CloseableHttpClient client() {
		return HttpClientBuilder.create().setUserAgent("FluxPlusClient/1.0").build();
	}

	private <T> T get(String path, Class<T> typ) throws IOException {
		try (CloseableHttpClient cli = client()) {
			HttpGet get = new HttpGet(HOST + path);
			return cli.execute(get, new JSONHandler<>(typ));
		}
	}
	private <I, O> O post(String path, I input, Class<O> outTyp) throws IOException {
		try (CloseableHttpClient cli = client()) {
			HttpPost post = new HttpPost(HOST + path);
			post.setEntity(new StringEntity(GSON.toJson(input, input.getClass())));
			post.setHeader("Content-Type", "application/json");
			return cli.execute(post, new JSONHandler<>(outTyp));
		}
	}

	public void putAction(final String type) {
		if (ACTIONS.contains(type)) return;
		EXEC.execute(() -> {
			try {
				boolean b = get("/action?type=" + type, Boolean.TYPE);
				if (b) ACTIONS.add(type);
				else LOGGER.warn("Action type {} is not acceptable", type);
			} catch (IOException e) {
				LOGGER.error("Exception while registering an action", e);
			}
		});
	}

	public void sendItemMap(final Map<String, Object> fullMap) {
		EXEC.execute(() -> {
			try {
				post("/items", fullMap, Boolean.TYPE);
			} catch (IOException e) {
				LOGGER.error("Exception while registering item map", e);
			}
		});
	}

	private static class JSONHandler<T> implements ResponseHandler<T> {
		private final Class<T> typ;

		private JSONHandler(Class<T> typ) {
			this.typ = typ;
		}

		@Override
		public T handleResponse(HttpResponse res) throws IOException {
			return GSON.fromJson(new InputStreamReader(res.getEntity().getContent()), typ);
		}
	}
}
