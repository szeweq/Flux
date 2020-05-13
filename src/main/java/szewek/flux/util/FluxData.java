package szewek.flux.util;

import com.google.gson.*;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import szewek.flux.recipe.FluxGenRecipes;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static szewek.flux.Flux.MODID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class FluxData {
	private static final Gson GSON = new GsonBuilder().setLenient().create();
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation
			CATALYSTS = new ResourceLocation(MODID, "values/fluxgen/catalyst.json"),
			HOT_FLUIDS = new ResourceLocation(MODID, "values/fluxgen/hot.json"),
			COLD_FLUIDS = new ResourceLocation(MODID, "values/fluxgen/cold.json"),
			GIFTS_LIST = new ResourceLocation(MODID, "gifts/global_list.json");

	public static void addReloadListeners(IReloadableResourceManager rm) {
		rm.addReloadListener(new FluxGenValues());
		rm.addReloadListener(new FluxGifts());
	}

	private static CompletableFuture<Collection<FluxGenRecipes.Entry>> parseFluxGenValues(
			IResourceManager rm, ResourceLocation loc, Executor ex) {
		return collectFromResourcesAsync(ArrayList::new, rm, loc, ex, (vals, json) -> {
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
		});
	}

	private static CompletableFuture<Set<ResourceLocation>> collectGiftLootTables(IResourceManager rm, Executor exec) {
		return collectFromResourcesAsync(HashSet::new, rm, GIFTS_LIST, exec, (set, json) -> {
			JsonArray entries = JSONUtils.getJsonArray(json, "entries");
			for (JsonElement el : entries) {
				set.add(new ResourceLocation(el.getAsString()));
			}
		});
	}

	private static <T> CompletableFuture<T> collectFromResourcesAsync(Supplier<T> sup, IResourceManager rm, ResourceLocation loc, Executor exec, JSONProcessor<T> jc) {
		return CompletableFuture.supplyAsync(() -> {
			T t = sup.get();
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

	private interface JSONProcessor<T> {
		void process(T t, JsonObject json) throws IOException, RuntimeException;
	}

	private static class FluxGenValues implements IFutureReloadListener {
		private FluxGenValues() {}

		@Override
		public CompletableFuture<Void> reload(IStage stage, IResourceManager rm, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor bgExec, Executor gameExec) {
			CompletableFuture<Collection<FluxGenRecipes.Entry>> catalystVals = parseFluxGenValues(rm, CATALYSTS, bgExec);
			CompletableFuture<Collection<FluxGenRecipes.Entry>> hotVals = parseFluxGenValues(rm, HOT_FLUIDS, bgExec);
			CompletableFuture<Collection<FluxGenRecipes.Entry>> coldVals = parseFluxGenValues(rm, COLD_FLUIDS, bgExec);
			return catalystVals
					.thenCombine(hotVals, (c, hf) -> MutableTriple.of(c, hf, (Collection<FluxGenRecipes.Entry>) null))
					.thenCombine(coldVals, (t, cf) -> {
						t.setRight(cf);
						return t;
					})
					.thenCompose(stage::markCompleteAwaitingOthers)
					.thenAcceptAsync(FluxGenRecipes::collectValues, gameExec);
		}
	}

	private static class FluxGifts implements IFutureReloadListener {
		private FluxGifts() {}

		@Override
		public CompletableFuture<Void> reload(IStage stage, IResourceManager rm, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor bgExec, Executor gameExec) {
			return collectGiftLootTables(rm, bgExec)
					.thenCompose(stage::markCompleteAwaitingOthers)
					.thenAcceptAsync(Gifts::saveGiftLootTables, gameExec);
		}
	}
}
