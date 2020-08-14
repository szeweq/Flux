package szewek.flux.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import szewek.flux.util.Gifts;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static szewek.flux.Flux.MODID;

class FluxGifts implements IFutureReloadListener {
	private static final ResourceLocation GIFTS_LIST = new ResourceLocation(MODID, "gifts/global_list.json");

	FluxGifts() {
	}

	@Override
	public CompletableFuture<Void> reload(IStage stage, IResourceManager rm, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor bgExec, Executor gameExec) {
		return collectGiftLootTables(rm, bgExec)
				.thenCompose(stage::markCompleteAwaitingOthers)
				.thenAcceptAsync(Gifts::saveGiftLootTables, gameExec);
	}

	private static CompletableFuture<Set<ResourceLocation>> collectGiftLootTables(IResourceManager rm, Executor exec) {
		return FluxData.collectFromResources(HashSet::new, rm, GIFTS_LIST, exec, (set, json) -> {
			JsonArray entries = JSONUtils.getJsonArray(json, "entries");
			for (JsonElement el : entries) {
				set.add(new ResourceLocation(el.getAsString()));
			}
		});
	}
}
