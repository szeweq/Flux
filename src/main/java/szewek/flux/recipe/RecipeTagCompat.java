package szewek.flux.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import szewek.flux.FluxCfg;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class RecipeTagCompat {

	static ItemStack findItemTag(JsonObject json) {
		String tagName = JSONUtils.getString(json, "tag");
		Tag<Item> tag = ItemTags.getCollection().get(new ResourceLocation(tagName));
		if (tag == null || tag.getAllElements().isEmpty()) {
			return ItemStack.EMPTY;
		}
		Item foundItem = itemFromTagCompat(tag.getAllElements());
		if (foundItem == null) return ItemStack.EMPTY;
		return new ItemStack(foundItem, JSONUtils.getInt(json, "count", 1));
	}

	@Nullable
	static Item itemFromTagCompat(Collection<Item> items) {
		if (items.isEmpty()) return null;
		//noinspection unchecked
		final List<String> modCompat = (List<String>) FluxCfg.COMMON.preferModCompat.get();
		if (modCompat.contains("jaopca")) {
			// JAOPCA creates recipes so duplicates cannot be allowed
			return null;
		}
		if (modCompat.isEmpty() || items.size() == 1) {
			return items.iterator().next();
		}
		Item foundItem = null;
		int compatIndex = modCompat.size();
		for (Item item : items) {
			String ns = Objects.requireNonNull(item.getRegistryName()).getNamespace();
			int n = modCompat.indexOf(ns);
			if (n != -1 && n < compatIndex) {
				foundItem = item;
				compatIndex = n;
			}
			if (compatIndex == 0) break;
		}
		if (foundItem == null) foundItem = items.iterator().next();
		return foundItem;
	}

	private RecipeTagCompat() {}
}
