package szewek.fl.network;

import com.google.common.io.ByteStreams;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * API Call for easy request building and response conversion.
 */
public class APICall {
	private final HttpURLConnection conn;

	APICall(HttpURLConnection conn) {
		this.conn = conn;
	}

	public static APICall to(String url) throws IOException {
		final HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();
		return new APICall(huc);
	}

	public void checkStatus() throws IOException {
		int status = conn.getResponseCode();
		if (status / 100 != 2) {
			InputStream err = conn.getErrorStream();
			ByteArrayOutputStream bs = new ByteArrayOutputStream(Math.max(32, err.available()));
			//noinspection UnstableApiUsage
			ByteStreams.copy(err, bs);
			err.close();
			throw new IOException("HTTP " + status + ": " + bs.toString("UTF-8"));
		}
	}

	private Writer preparePost(final String type) throws IOException {
		conn.setRequestMethod("POST");
		conn.setRequestProperty("User-Agent", "FL-Client/1.0");
		conn.setRequestProperty("Content-Type", type);
		conn.setDoOutput(true);
		final OutputStream out = conn.getOutputStream();
		return new OutputStreamWriter(new BufferedOutputStream(out), UTF_8);
	}

	public APICall post(final Object obj) throws IOException {
		final Writer w = preparePost("application/json;charset=utf-8");
		FluxPlus.GSON.toJson(obj, w);
		w.close();
		return this;
	}

	public APICall postString(final String s, final String type) throws IOException {
		final Writer w = preparePost(type);
		w.write(s);
		w.close();
		return this;
	}

	public Reader prepareResponse() throws IOException {
		checkStatus();
		final InputStream in = conn.getInputStream();
		return new InputStreamReader(new BufferedInputStream(in), UTF_8);
	}

	public <T> T response(final Class<T> type) throws IOException {
		final Reader r = prepareResponse();
		T t = FluxPlus.GSON.fromJson(r, type);
		r.close();
		return t;
	}
}
