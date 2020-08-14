package szewek.flux.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import szewek.fl.util.IntPair;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

import static szewek.flux.Flux.MODID;

public class FluxGenValues implements IFutureReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation
			CATALYST_JSON = new ResourceLocation(MODID, "values/fluxgen/catalyst.json"),
			HOT_JSON = new ResourceLocation(MODID, "values/fluxgen/hot.json"),
			COLD_JSON = new ResourceLocation(MODID, "values/fluxgen/cold.json");

	/* IntPair values: l = factor; r = usage */
	public static final IntPair DEFAULT = IntPair.of(1, 0);
	public static final ValMap<Item> CATALYSTS = new ValMap<>();
	public static final ValMap<Fluid> HOT_FLUIDS = new ValMap<>();
	public static final ValMap<Fluid> COLD_FLUIDS = new ValMap<>();

	FluxGenValues() {
	}

	@Override
	public CompletableFuture<Void> reload(IStage stage, IResourceManager rm, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor bgExec, Executor gameExec) {
		CompletableFuture<Collection<Entry>> catalystVals = parse(CATALYST_JSON, rm, bgExec);
		CompletableFuture<Collection<Entry>> hotVals = parse(HOT_JSON, rm, bgExec);
		CompletableFuture<Collection<Entry>> coldVals = parse(COLD_JSON, rm, bgExec);
		return catalystVals
				.thenCombine(hotVals, (c, hf) -> MutableTriple.of(c, hf, (Collection<Entry>) null))
				.thenCombine(coldVals, (t, cf) -> {
					t.setRight(cf);
					return t;
				})
				.thenCompose(stage::markCompleteAwaitingOthers)
				.thenAcceptAsync(FluxGenValues::collectValues, gameExec);
	}

	private static CompletableFuture<Collection<Entry>> parse(
			ResourceLocation loc, IResourceManager rm, Executor ex) {
		return FluxData.collectFromResources(ArrayList::new, rm, loc, ex, (vals, json) -> {
			JsonObject values = JSONUtils.getJsonObject(json, "values");
			for (Map.Entry<String, JsonElement> e : values.entrySet()) {
				String key = e.getKey();
				IntPair pair = pairFromJSON(e.getValue(), key);
				if (pair.l <= 0 || pair.r <= 0) {
					LOGGER.warn("Factor and usage must be bigger than 0. Skipping {}", key);
					continue;
				}
				vals.add(new Entry(key, pair));
			}
		});
	}

	private static IntPair pairFromJSON(JsonElement el, String key) {
		if (JSONUtils.isNumber(el)) {
			return IntPair.of(el.getAsInt(), 0);
		} else if (el.isJsonArray()) {
			JsonArray ja = el.getAsJsonArray();
			if (ja.size() == 2) {
				int factor = JSONUtils.getInt(ja.get(0), key + "[0]");
				int usage = JSONUtils.getInt(ja.get(1), key + "[1]");
				return IntPair.of(factor, usage);
			}
			LOGGER.warn("Ignoring array with size other than 2 in {}", key);
		}
		return IntPair.ZERO;
	}

	private static void collectValues(Triple<Collection<Entry>, Collection<Entry>, Collection<Entry>> tr) {
		CATALYSTS.convert(tr.getLeft(), ForgeRegistries.ITEMS, ItemTags.getCollection());
		HOT_FLUIDS.convert(tr.getMiddle(), ForgeRegistries.FLUIDS, FluidTags.getCollection());
		COLD_FLUIDS.convert(tr.getRight(), ForgeRegistries.FLUIDS, FluidTags.getCollection());
	}

	public static class ValMap<T extends IForgeRegistryEntry<T>> {
		private final Map<T, IntPair> map = new HashMap<>();

		public boolean has(T t) {
			return map.containsKey(t);
		}

		public IntPair get(T t) {
			return map.getOrDefault(t, DEFAULT);
		}

		public Map<T, IntPair> all() {
			return Collections.unmodifiableMap(map);
		}

		void convert(Collection<FluxGenValues.Entry> entries, IForgeRegistry<T> reg, TagCollection<T> tags) {
			map.clear();
			for (FluxGenValues.Entry e : entries) {
				if (!e.conv.convert(map::put, reg, tags)) {
					LOGGER.error("Couldn't find tag/resource with name: {}", e.loc);
				}
			}
		}
	}

	static class Entry {
		final IntPair values;
		final ResourceLocation loc;
		final EntryConverter conv;

		Entry(String key, IntPair values) {
			this.values = values;
			boolean tag = key.charAt(0) == '#';
			loc = new ResourceLocation(tag ? key.substring(1): key);
			conv = tag ? this::tag : this::item;
		}

		private <T extends IForgeRegistryEntry<T>> boolean tag(BiConsumer<T, IntPair> fn, IForgeRegistry<T> reg, TagCollection<T> tags) {
			ITag<T> tag = tags.get(this.loc);
			if (tag == null) {
				return false;
			}
			for (T t : tag.getAllElements()) {
				fn.accept(t, this.values);
			}
			return true;
		}

		private <T extends IForgeRegistryEntry<T>> boolean item(BiConsumer<T, IntPair> fn, IForgeRegistry<T> reg, TagCollection<T> tags) {
			T t = reg.getValue(this.loc);
			if (t == null) {
				return false;
			}
			fn.accept(t, this.values);
			return true;
		}
	}

	interface EntryConverter {
		<T extends IForgeRegistryEntry<T>> boolean convert(BiConsumer<T, IntPair> fn, IForgeRegistry<T> reg, TagCollection<T> tags);
	}
}
