package szewek.fl;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import szewek.fl.network.FluxPlus;

import java.util.HashMap;
import java.util.Map;

@Mod(FL.ID)
public final class FL {
	public static final String ID = "fl";

	public FL() {
		MinecraftForge.EVENT_BUS.addListener(FL::tagsLoaded);
	}

	@SubscribeEvent
	public static void setup(final FMLCommonSetupEvent e) {
		FluxPlus.API.putAction("start");
	}

	@SubscribeEvent
	public static void stop(final FMLServerStoppingEvent e) {
		FluxPlus.API.putAction("exit");
	}

	private static void tagsLoaded(final TagsUpdatedEvent e) {

		final Object2IntSortedMap<String> itemIds = new Object2IntLinkedOpenHashMap<>();
		final Map<String, IntSet> tagToIds = new HashMap<>();
		Map<ResourceLocation, Tag<Item>> tagMap = e.getTagManager().getItems().getTagMap();
		for (Map.Entry<ResourceLocation, Tag<Item>> entry : tagMap.entrySet()) {
			final ResourceLocation tag = entry.getKey();
			final String ns = tag.getNamespace();
			final char charNs = "minecraft".equals(ns) ? '$' : "forge".equals(ns) ? '#' : 0;
			final String tagName = charNs != 0 ? charNs + tag.getPath() : tag.toString();
			for (Item item : entry.getValue().getAllElements()) {
				ResourceLocation rl = item.getRegistryName();
				if (rl == null) continue;
				//String ins = rl.getNamespace();
				//if (!"minecraft".equals(ins) && !"flux".equals(ins)) {
					int id = itemIds.computeIntIfAbsent(rl.toString(), k -> itemIds.size());
					tagToIds.computeIfAbsent(tagName, s -> new IntOpenHashSet()).add(id);
				//}
			}
		}
		Map<String, Object> collected = new HashMap<>();
		collected.put("items", itemIds.keySet());
		collected.put("tags", tagToIds);
		FluxPlus.API.sendItemMap(collected);
	}
}
