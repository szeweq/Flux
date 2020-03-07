package szewek.fl.network;

import com.google.common.io.ByteStreams;

import java.io.*;
import java.net.HttpURLConnection;

import static java.nio.charset.StandardCharsets.UTF_8;

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
		conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
		conn.setDoOutput(true);
		final OutputStream out = conn.getOutputStream();
		final OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(out), UTF_8);
		FluxPlus.GSON.toJson(obj, writer);
		writer.close();
		return this;
	}

	public <T> T response(final Class<T> type) throws IOException {
		checkStatus();
		final InputStream in = conn.getInputStream();
		final InputStreamReader reader = new InputStreamReader(new BufferedInputStream(in), UTF_8);
		T t = FluxPlus.GSON.fromJson(reader, type);
		reader.close();
		return t;
	}
}
