package szewek.fl;

import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import szewek.fl.network.FluxPlus;
import szewek.fl.util.ValueToIDMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mod(FL.ID)
public final class FL {
	public static final String ID = "fl";

	public FL() {
		MinecraftForge.EVENT_BUS.register(Events.class);
	}

	private static boolean unfamiliar(ResourceLocation loc) {
		final String ns = loc.getNamespace();
		return !"minecraft".equals(ns) && !"flux".equals(ns) && !"forge".equals(ns);
	}

	private static <T, K, V> Map<K, V> map(T t) {
		return new HashMap<>();
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModEvents {
		@SubscribeEvent
		public static void setup(final FMLCommonSetupEvent e) {
			FluxPlus.putAction("start");
			final Map<String, Map<String, String>> serMap = new HashMap<>();
			final Collection<IRecipeSerializer<?>> sers = ForgeRegistries.RECIPE_SERIALIZERS.getValues();
			for (IRecipeSerializer<?> ser : sers) {
				ResourceLocation loc = ser.getRegistryName();
				if (loc != null && unfamiliar(loc) && !loc.getPath().startsWith("craft")) {
					serMap.computeIfAbsent(loc.getNamespace(), FL::map)
							.put(loc.getPath(), ser.getClass().getName());
				}
			}
			FluxPlus.sendSerializerNames(serMap);
		}

		@SubscribeEvent
		public static void stop(final FMLServerStoppedEvent e) {
			FluxPlus.putAction("exit");
		}
	}

	static class Events {
		@SubscribeEvent
		public static void tagsLoaded(final TagsUpdatedEvent e) {
			final ValueToIDMap<String> itemIds = new ValueToIDMap<>();
			final Map<String, IntSet> tagToIds = new HashMap<>();
			Map<ResourceLocation, Tag<Item>> tagMap = e.getTagManager().getItems().getTagMap();
			for (Map.Entry<ResourceLocation, Tag<Item>> entry : tagMap.entrySet()) {
				final ResourceLocation tag = entry.getKey();
				final String ns = tag.getNamespace();
				final char charNs = "minecraft".equals(ns) ? '$' : "forge".equals(ns) ? '#' : 0;
				final String tagName = charNs != 0 ? charNs + tag.getPath() : tag.toString();
				for (Item item : entry.getValue().getAllElements()) {
					ResourceLocation rl = item.getRegistryName();
					if (rl != null && unfamiliar(rl)) {
						int id = itemIds.get(rl.toString());
						tagToIds.computeIfAbsent(tagName, s -> new IntOpenHashSet()).add(id);
					}
				}
			}
			Map<String, Object> collected = new HashMap<>();
			collected.put("items", itemIds.values());
			collected.put("tags", tagToIds);
			FluxPlus.sendItemMap(collected);
		}

		@SubscribeEvent
		public static void recipesLoaded(final RecipesUpdatedEvent e) {
			final Map<String, Object> recipeInfos = new HashMap<>();
			final ValueToIDMap<String> typeIds = new ValueToIDMap<>();
			final ValueToIDMap<String> itemIds = new ValueToIDMap<>();
			final Map<String, Map<String, Object>> namespaces = new HashMap<>();
			final Collection<IRecipe<?>> recipes = e.getRecipeManager().getRecipes();
			for (IRecipe<?> r : recipes) {
				ResourceLocation id = r.getId();
				if (unfamiliar(id)) {
					ResourceLocation serName = r.getSerializer().getRegistryName();
					if (serName == null || serName.getPath().startsWith("craft")) continue;
					ItemStack stack = r.getRecipeOutput();
					ResourceLocation itemLoc = stack.getItem().getRegistryName();
					if (itemLoc == null) continue;
					int[] info = new int[4];
					info[0] = typeIds.get(serName.toString());
					info[1] = r.getIngredients().size();
					info[2] = itemIds.get(itemLoc.toString());
					info[3] = stack.getCount();
					namespaces.computeIfAbsent(id.getNamespace(), FL::map).put(id.getPath(), info);
				}
			}
			recipeInfos.put("$types", typeIds.values());
			recipeInfos.put("$items", itemIds.values());
			recipeInfos.putAll(namespaces);
			FluxPlus.sendRecipeInfos(recipeInfos);
		}

		@SubscribeEvent
		public static void playerLogin(final PlayerEvent.PlayerLoggedInEvent e) {
			FluxPlus.putAction("login");
		}
	}
}
