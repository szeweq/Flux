package szewek.flux.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class FluxData {
	private static final Gson GSON = new GsonBuilder().setLenient().create();
	static final Logger LOGGER = LogManager.getLogger();
	private static final IFutureReloadListener
			FLUXGEN_VALUES = new FluxGenValues(),
			FLUX_GIFTS = new Gifts();

	public static void addReloadListeners(final AddReloadListenerEvent e) {
		e.addListener(FLUXGEN_VALUES);
		e.addListener(FLUX_GIFTS);
	}

	static <T> CompletableFuture<T> collectFromResources(Supplier<T> sup, IResourceManager rm, ResourceLocation loc, Executor exec, JSONProcessor<T> jc) {
		return CompletableFuture.supplyAsync(() -> {
			T t = sup.get();
			try {
				for (IResource res : rm.getAllResources(loc)) {
					try (
							InputStream input = res.getInputStream();
							Reader r = new BufferedReader(new InputStreamReader(input, UTF_8))
					) {
						JsonObject json = JSONUtils.fromJson(GSON, r, JsonObject.class, true);
						if (json == null) {
							continue;
						}
						jc.process(t, json);
					} catch (RuntimeException | IOException e) {
						LOGGER.error("Couldn't load values from {} in data pack {}", loc, res.getPackName(), e);
					} finally {
						IOUtils.closeQuietly(res);
					}
				}
			}  catch (IOException e) {
				LOGGER.error("Couldn't load any data from {}", loc, e);
			}
			return t;
		}, exec);
	}

	private FluxData() {}
}
