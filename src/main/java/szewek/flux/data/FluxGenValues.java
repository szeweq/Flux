package szewek.flux.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.MutableTriple;
import szewek.flux.recipe.FluxGenRecipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static szewek.flux.Flux.MODID;
import static szewek.flux.data.FluxData.LOGGER;

class FluxGenValues implements IFutureReloadListener {
	private static final ResourceLocation
			CATALYSTS = new ResourceLocation(MODID, "values/fluxgen/catalyst.json"),
			HOT_FLUIDS = new ResourceLocation(MODID, "values/fluxgen/hot.json"),
			COLD_FLUIDS = new ResourceLocation(MODID, "values/fluxgen/cold.json");

	FluxGenValues() {
	}

	@Override
	public CompletableFuture<Void> reload(IStage stage, IResourceManager rm, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor bgExec, Executor gameExec) {
		CompletableFuture<Collection<FluxGenRecipes.Entry>> catalystVals = parse(CATALYSTS, rm, bgExec);
		CompletableFuture<Collection<FluxGenRecipes.Entry>> hotVals = parse(HOT_FLUIDS, rm, bgExec);
		CompletableFuture<Collection<FluxGenRecipes.Entry>> coldVals = parse(COLD_FLUIDS, rm, bgExec);
		return catalystVals
				.thenCombine(hotVals, (c, hf) -> MutableTriple.of(c, hf, (Collection<FluxGenRecipes.Entry>) null))
				.thenCombine(coldVals, (t, cf) -> {
					t.setRight(cf);
					return t;
				})
				.thenCompose(stage::markCompleteAwaitingOthers)
				.thenAcceptAsync(FluxGenRecipes::collectValues, gameExec);
	}

	private static CompletableFuture<Collection<FluxGenRecipes.Entry>> parse(
			ResourceLocation loc, IResourceManager rm, Executor ex) {
		return FluxData.collectFromResources(ArrayList::new, rm, loc, ex, (vals, json) -> {
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
				vals.add(new FluxGenRecipes.Entry(key, usage, factor));
			}
		});
	}
}
