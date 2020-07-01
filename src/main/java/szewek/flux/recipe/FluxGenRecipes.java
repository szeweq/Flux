package szewek.flux.recipe;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import szewek.fl.util.IntPair;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class FluxGenRecipes {
	private static final Logger LOGGER = LogManager.getLogger();
	/* IntPair values: l = factor; r = usage */
	public static final IntPair DEFAULT = IntPair.of(1, 0);
	private static final Map<Item, IntPair> catalysts = new ConcurrentHashMap<>();
	private static final Map<Fluid, IntPair> coldFluids = new ConcurrentHashMap<>();
	private static final Map<Fluid, IntPair> hotFluids = new ConcurrentHashMap<>();

	public static boolean isCatalyst(Item item) {
		return catalysts.containsKey(item);
	}

	public static IntPair getCatalyst(Item item) {
		return catalysts.getOrDefault(item, DEFAULT);
	}

	public static Set<Map.Entry<Item, IntPair>> allCatalysts() {
		return Collections.unmodifiableSet(catalysts.entrySet());
	}

	public static boolean isHotFluid(Fluid fluid) {
		return isFluid(fluid, hotFluids);
	}

	public static IntPair getHotFluid(Fluid fluid) {
		return getFluid(fluid, hotFluids);
	}

	public static Set<Map.Entry<Fluid, IntPair>> allHotFluids() {
		return Collections.unmodifiableSet(hotFluids.entrySet());
	}

	public static boolean isColdFluid(Fluid fluid) {
		return isFluid(fluid, coldFluids);
	}

	public static IntPair getColdFluid(Fluid fluid) {
		return getFluid(fluid, coldFluids);
	}

	public static Set<Map.Entry<Fluid, IntPair>> allColdFluids() {
		return Collections.unmodifiableSet(coldFluids.entrySet());
	}

	private static boolean isFluid(@Nullable Fluid fluid, Map<Fluid, IntPair> m) {
		return m.containsKey(fluid);
	}

	private static IntPair getFluid(@Nullable Fluid fluid, Map<Fluid, IntPair> m) {
		return m.getOrDefault(fluid, DEFAULT);
	}

	public static void add(Item item, int factor, int usage) {
		catalysts.put(item, IntPair.of(factor, usage));
	}

	public static void collectValues(Triple<Collection<Entry>, Collection<Entry>, Collection<Entry>> tr) {
		convertMap(catalysts, tr.getLeft(), ForgeRegistries.ITEMS, ItemTags.getCollection());
		convertMap(hotFluids, tr.getMiddle(), ForgeRegistries.FLUIDS, FluidTags.getCollection());
		convertMap(coldFluids, tr.getRight(), ForgeRegistries.FLUIDS, FluidTags.getCollection());
	}

	private FluxGenRecipes() {
	}

	private static <T extends IForgeRegistryEntry<T>> void convertMap(Map<T, IntPair> map, Collection<Entry> entries, IForgeRegistry<T> reg, TagCollection<T> tags) {
		map.clear();
		for (Entry e : entries) {
			if (!e.tag) {
				T t = reg.getValue(e.loc);
				if (t == null) {
					LOGGER.error("Couldn't find resource with name: {}", e.loc);
				} else {
					map.put(t, e.values);
				}
				continue;
			}
			Tag<T> tag = tags.get(e.loc);
			if (tag == null) {
				LOGGER.error("Couldn't find tag with name: {}", e.loc);
			} else {
				for (T t : tag.getAllElements()) {
					map.put(t, e.values);
				}
			}
		}
	}

	public static class Entry {
		public final ResourceLocation loc;
		public final boolean tag;
		public final IntPair values;

		public Entry(String key, int usage, int factor) {
			tag = key.charAt(0) == '#';
			loc = new ResourceLocation(tag ? key.substring(1): key);
			this.values = IntPair.of(factor, usage);
		}
	}
}
