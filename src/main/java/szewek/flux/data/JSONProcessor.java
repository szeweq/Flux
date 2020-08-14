package szewek.flux.data;

import com.google.gson.JsonObject;

import java.io.IOException;

interface JSONProcessor<T> {
	void process(T t, JsonObject json) throws IOException, RuntimeException;
}
