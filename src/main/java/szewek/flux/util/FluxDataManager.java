package szewek.flux.util;

import com.google.gson.*;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import szewek.flux.recipe.FluxGenRecipes;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static szewek.flux.FluxMod.MODID;

// EXPERIMENTAL
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluxDataManager implements IFutureReloadListener {
	private static final Gson GSON = new GsonBuilder().setLenient().create();
	private static final Logger LOGGER = LogManager.getLogger("FluxDataManager");
	private static final ResourceLocation
			CATALYSTS = new ResourceLocation(MODID, "values/fluxgen/catalyst.json"),
			HOT_FLUIDS = new ResourceLocation(MODID, "values/fluxgen/hot.json"),
			COLD_FLUIDS = new ResourceLocation(MODID, "values/fluxgen/cold.json");

	@Override
	public CompletableFuture<Void> reload(IStage stage, IResourceManager rm, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor bgExec, Executor gameExec) {
		CompletableFuture<Collection<FluxGenRecipes.Entry>> catalystVals = parseFluxGenValues(rm, CATALYSTS, bgExec);
		CompletableFuture<Collection<FluxGenRecipes.Entry>> hotVals = parseFluxGenValues(rm, HOT_FLUIDS, bgExec);
		CompletableFuture<Collection<FluxGenRecipes.Entry>> coldVals = parseFluxGenValues(rm, COLD_FLUIDS, bgExec);
		return catalystVals.thenCombine(hotVals.thenCombine(coldVals, Pair::of), (m, p) -> Triple.of(m, p.getLeft(), p.getRight()))
				.thenCompose(stage::markCompleteAwaitingOthers)
				.thenAcceptAsync(FluxGenRecipes::collectValues, gameExec);
	}

	private <T extends IForgeRegistryEntry<T>> CompletableFuture<Collection<FluxGenRecipes.Entry>> parseFluxGenValues(
			IResourceManager rm, ResourceLocation loc, Executor ex) {
		return CompletableFuture.supplyAsync(() -> {
			List<FluxGenRecipes.Entry> vals = new ArrayList<>();
			try {
				for (IResource res : rm.getAllResources(loc)) {
					try (
						InputStream input = res.getInputStream();
						Reader r = new BufferedReader(new InputStreamReader(input, UTF_8))
					) {
						JsonObject json = JSONUtils.fromJson(GSON, r, JsonObject.class);
						if (json == null) {
							continue;
						}
						JsonObject values = JSONUtils.getJsonObject(json, "values");
						for (Map.Entry<String, JsonElement> e : values.entrySet()) {
							String key = e.getKey();
							JsonElement el = e.getValue();
							int factor = 0, usage = 0;
							if (JSONUtils.isNumber(el)) {
								factor = el.getAsInt();
								usage = 1;
							} else if (el.isJsonArray()) {
								JsonArray ja = el.getAsJsonArray();
								if (ja.size() == 2) {
									factor = JSONUtils.getInt(ja.get(0), key + "[0]");
									usage = JSONUtils.getInt(ja.get(1), key + "[1]");
								} else {
									LOGGER.warn("Ignoring array with size other than 2 in {}", key);
									continue;
								}
							}
							if (factor <= 0 || usage <= 0) {
								LOGGER.warn("Factor and usage must be bigger than 0. Skipping {}", key);
								continue;
							}
							vals.add(FluxGenRecipes.Entry.from(key, usage, factor));
						}
					} catch (RuntimeException | IOException e) {
						LOGGER.error("Couldn't load values from {} in data pack {}", loc, res.getPackName(), e);
					} finally {
						IOUtils.closeQuietly(res);
					}
				}
			} catch (IOException e) {
				LOGGER.error("Couldn't load any data from {}", loc, e);
			}
			return vals;
		}, ex);
	}

}
