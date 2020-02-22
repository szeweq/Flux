package szewek.fl.network;

import com.google.common.io.ByteStreams;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class APICall {
	private final HttpURLConnection conn;

	APICall(HttpURLConnection conn) {
		this.conn = conn;
	}

	private void checkStatus() throws IOException {
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

	public APICall post(final Object obj) throws IOException {
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setDoOutput(true);
		final OutputStream out = conn.getOutputStream();
		FluxPlus.GSON.toJson(obj, new OutputStreamWriter(out, StandardCharsets.UTF_8));
		out.close();
		return this;
	}

	public <T> T response(final Class<T> type) throws IOException {
		checkStatus();
		final InputStream in = conn.getInputStream();
		T t = FluxPlus.GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
		in.close();
		return t;
	}
}
