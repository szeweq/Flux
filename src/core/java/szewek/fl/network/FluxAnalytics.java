package szewek.fl.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class for usage analytics.
 */
public final class FluxAnalytics {
	// THIS IS TEMPORARY! MIGHT USE SOMETHING LESS ANNOYING
	private static final String GA_URL = "https://www.google-analytics.com/collect";
	private static final String FORM_TYPE = "application/x-www-form-urlencoded;charset=utf-8";
	private static final String CLIENT_ID = UUID.randomUUID().toString();
	private static final Logger LOGGER = LogManager.getLogger("Flux+");
	static final Gson GSON = new GsonBuilder().setLenient().create();
	private static final ExecutorService EXEC = new ThreadPoolExecutor(0, 2, 30L, TimeUnit.SECONDS, new SynchronousQueue<>());

	private FluxAnalytics() {}

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

	private static String safeParam(String param) {
		if (param == null) {
			return "";
		}
		int x = param.indexOf('&');
		if (x != -1) {
			param = param.substring(0, x);
		}
		try {
			return URLEncoder.encode(param, "UTF-8");
		} catch (UnsupportedEncodingException ignored) {}
		return "";
	}

	public static void putView(String view) {
		String form = "t=pageview&dp=%2F";
		if (view != null && !view.isEmpty()) {
			form += safeParam(view);
		}
		send(form);
	}

	public static void putAction(final String cat, final String type) {
		send("t=event&ec=" + safeParam(cat) + "&ea=" + safeParam(type));
	}
}
