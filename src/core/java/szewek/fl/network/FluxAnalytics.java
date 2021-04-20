package szewek.fl.network;

import com.google.common.io.ByteStreams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Class for usage analytics.
 */
public final class FluxAnalytics {
	// THIS IS TEMPORARY! MIGHT USE SOMETHING LESS ANNOYING
	private static final String GA_URL = "https://www.google-analytics.com/collect";
	private static final String FORM_TYPE = "application/x-www-form-urlencoded;charset=utf-8";
	private static final String USER_AGENT = makeUserAgent();
	private static final String CLIENT_ID = UUID.randomUUID().toString();
	private static final Logger LOGGER = LogManager.getLogger("Flux+");
	private static final ExecutorService DEFAULT_EXEC = new ThreadPoolExecutor(0, 2, 30L, TimeUnit.SECONDS, new SynchronousQueue<>());

	private static FluxAnalytics instance;

	private final URL url;
	private String version = "1.0-pre0";
	private String playerID = null;

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

	public void updateVersion(String newVersion) {
		version = newVersion;
	}

	public void updatePlayerID(String newPlayerID) {
		playerID = newPlayerID;
	}

	private void process(final String form) {
		try {
			final HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("POST");
			huc.setRequestProperty("User-Agent", USER_AGENT + version);
			huc.setRequestProperty("Content-Type", FORM_TYPE);
			huc.setDoOutput(true);

			final OutputStream out = huc.getOutputStream();
			final Writer w = new OutputStreamWriter(new BufferedOutputStream(out), UTF_8);
			w.write("v=1&tid=UA-177867488-1&cid=");
			w.write(CLIENT_ID);
			if (playerID != null) {
				w.write("&uid=");
				w.write(playerID);
			}
			w.write("&ul=");
			w.write(System.getProperty("user.language", "en"));
			w.write(form);
			w.close();

			int status = huc.getResponseCode();
			if (status / 100 != 2) {
				InputStream err = huc.getErrorStream();
				ByteArrayOutputStream bs = new ByteArrayOutputStream(Math.max(32, err.available()));
				//noinspection UnstableApiUsage
				ByteStreams.copy(err, bs);
				err.close();
				throw new IOException("HTTP " + status + ": " + bs.toString("UTF-8"));
			}
		} catch (IOException e) {
			LOGGER.error("Exception while sending an event", e);
		}
	}


	private static void send(Executor exec, final String form) {
		exec.execute(() -> get().process(form));
	}

	private static String makeUserAgent() {
		String version = System.getProperty("java.version");
		String vendor = System.getProperty("java.vm.vendor");
		String rtVersion = System.getProperty("java.vm.version");
		return "Java/" + version + " " + vendor + "/" + rtVersion + " FL/";
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
		putView(DEFAULT_EXEC, view);
	}

	public static void putView(Executor exec, String view) {
		String form = "&t=pageview&dp=%2F";
		if (view != null && !view.isEmpty()) {
			form += safeParam(view);
		}
		send(exec, form);
	}
}
