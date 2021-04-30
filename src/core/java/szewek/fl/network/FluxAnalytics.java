package szewek.fl.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Class for usage analytics.
 */
public final class FluxAnalytics {
	// THIS IS TEMPORARY! MIGHT USE SOMETHING LESS ANNOYING
	private static final String GA_URL = "https://www.google-analytics.com/collect";
	private static final String FORM_TYPE = "application/x-www-form-urlencoded;charset=utf-8";
	private static final String USER_AGENT = makeUserAgent();
	private static final Logger LOGGER = LogManager.getLogger("Flux+");

	private static FluxAnalytics instance;

	private final URL url;

	private FluxAnalytics(URL url) {
		this.url = url;
	}

	public static FluxAnalytics get() {
		if (instance == null) {
			URL url = null;
			try {
				url = new URL(GA_URL);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			instance = new FluxAnalytics(url);
		}
		return instance;
	}

	private void process(final String form) {
		try {
			final HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("POST");
			huc.setRequestProperty("User-Agent", USER_AGENT + NetCommon.flVersion);
			huc.setRequestProperty("Content-Type", FORM_TYPE);
			huc.setDoOutput(true);

			final OutputStream out = huc.getOutputStream();
			final Writer w = new OutputStreamWriter(new BufferedOutputStream(out), UTF_8);
			w.write("v=1&tid=UA-177867488-1&cid=");
			w.write(NetCommon.SESSION_ID);
			w.write("&ul=");
			w.write(System.getProperty("user.language", "en"));
			w.write(form);
			w.close();

			NetCommon.handleResponse(huc);
		} catch (IOException e) {
			LOGGER.error("Exception while sending an event", e);
		}
	}


	private static void send(final String form) {
		NetCommon.DEFAULT_EXEC.execute(() -> get().process(form));
	}

	private static String makeUserAgent() {
		return NetCommon.VM_VENDOR + "/" + NetCommon.VM_VERSION + " FL/";
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

	static void putView(String playerId, String view) {
		String form = "&uid=" + playerId + "&t=pageview&dp=%2F";
		if (view != null && !view.isEmpty()) {
			form += safeParam(view);
		}
		send(form);
	}
}
