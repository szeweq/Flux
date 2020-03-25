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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

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

	public static boolean isHotFluid(Fluid fluid) {
		return isFluid(fluid, hotFluids);
	}

	public static IntPair getHotFluid(Fluid fluid) {
		return getFluid(fluid, hotFluids);
	}

	public static boolean isColdFluid(Fluid fluid) {
		return isFluid(fluid, coldFluids);
	}

	public static IntPair getColdFluid(Fluid fluid) {
		return getFluid(fluid, coldFluids);
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

	private static <T extends IForgeRegistryEntry<T>> void convertMap(Map<T, IntPair> map, Collection<Entry> col, IForgeRegistry<T> reg, TagCollection<T> tags) {
		for (Entry e : col) {
			for (T c : e.type.resolve(e.loc, reg::getValue, tags::get)) {
				map.put(c, e.values);
			}
		}
	}

	public enum EntryType {
		TAG, SINGLE;

		private <T> Collection<T> resolve(ResourceLocation loc, Function<ResourceLocation, T> reg, Function<ResourceLocation, Tag<T>> tags) {
			Collection<T> col = Collections.emptySet();
			if (this == TAG) {
				Tag<T> tag = tags.apply(loc);
				if (tag == null) {
					LOGGER.error("Couldn't find tag with name: {}", loc);
				} else {
					col = tag.getAllElements();
				}
			} else {
				T t = reg.apply(loc);
				if (t == null) {
					LOGGER.error("Couldn't find resource with name: {}", loc);
				} else {
					col = Collections.singleton(t);
				}
			}
			return col;
		}
	}

	public static class Entry {
		public final ResourceLocation loc;
		public final EntryType type;
		public final IntPair values;

		public Entry(ResourceLocation loc, EntryType type, int usage, int factor) {
			this.loc = loc;
			this.type = type;
			this.values = IntPair.of(factor, usage);
		}

		public static Entry from(String key, int usage, int factor) {
			EntryType type;
			ResourceLocation loc;
			if (key.charAt(0) == '#') {
				type = EntryType.TAG;
				loc = new ResourceLocation(key.substring(1));
			} else {
				type = EntryType.SINGLE;
				loc = new ResourceLocation(key);
			}
			return new Entry(loc, type, usage, factor);
		}
	}
}
