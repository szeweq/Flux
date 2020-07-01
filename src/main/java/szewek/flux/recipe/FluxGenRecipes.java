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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class FluxGenRecipes {
	private static final Logger LOGGER = LogManager.getLogger();
	/* IntPair values: l = factor; r = usage */
	public static final IntPair DEFAULT = IntPair.of(1, 0);
	public static final Values<Item> CATALYSTS = new Values<>();
	public static final Values<Fluid> HOT_FLUIDS = new Values<>();
	public static final Values<Fluid> COLD_FLUIDS = new Values<>();

	public static void collectValues(Triple<Collection<Entry>, Collection<Entry>, Collection<Entry>> tr) {
		CATALYSTS.convert(tr.getLeft(), ForgeRegistries.ITEMS, ItemTags.getCollection());
		HOT_FLUIDS.convert(tr.getMiddle(), ForgeRegistries.FLUIDS, FluidTags.getCollection());
		COLD_FLUIDS.convert(tr.getRight(), ForgeRegistries.FLUIDS, FluidTags.getCollection());
	}

	private FluxGenRecipes() {}

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

	public static class Values<T extends IForgeRegistryEntry<T>> {
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

		void convert(Collection<Entry> entries, IForgeRegistry<T> reg, TagCollection<T> tags) {
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
	}
}
