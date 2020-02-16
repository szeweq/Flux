package szewek.fl.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FluxPlus {
	private static final String HOST = "http://localhost:3000/api"; //"https://fluxplus.herokuapp.com/api";
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
	private <O> O post(String path, Object input, Class<O> outTyp) throws IOException {
		try (CloseableHttpClient cli = client()) {
			HttpPost post = new HttpPost(HOST + path);
			final String json = GSON.toJson(input, input.getClass());
			post.setEntity(new GzipCompressingEntity(new StringEntity(json, ContentType.APPLICATION_JSON)));
			return cli.execute(post, new JSONHandler<>(outTyp));
		}
	}

	public static void putAction(final String type) {
		if (ACTIONS.contains(type)) return;
		EXEC.execute(() -> {
			try {
				boolean b = API.get("/action?type=" + type, Boolean.TYPE);
				if (b) ACTIONS.add(type);
				else LOGGER.warn("Action type {} is not acceptable", type);
			} catch (IOException e) {
				LOGGER.error("Exception while registering an action", e);
			}
		});
	}

	public static void sendItemMap(final Map<String, Object> fullMap) {
		EXEC.execute(() -> {
			try {
				API.post("/items", fullMap, Boolean.TYPE);
			} catch (IOException e) {
				LOGGER.error("Exception while registering item map", e);
			}
		});
	}
	public static void sendSerializerNames(final Map<String, Map<String, String>> fullMap) {
		EXEC.execute(() -> {
			try {
				API.post("/serializers", fullMap, Boolean.TYPE);
			} catch (IOException e) {
				LOGGER.error("Exception while registering serializer map", e);
			}
		});
	}
	public static void sendRecipeInfos(final Map<String, Object> fullMap) {
		EXEC.execute(() -> {
			try {
				API.post("/recipes", fullMap, Boolean.TYPE);
			} catch (IOException e) {
				LOGGER.error("Exception while registering recipe information map", e);
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
