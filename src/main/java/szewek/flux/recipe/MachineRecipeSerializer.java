package szewek.flux.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class MachineRecipeSerializer<T extends AbstractMachineRecipe> extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
	private static final IntList DEFAULT_COST = IntLists.singleton(1);

	private final IFactory<T> factory;
	private final int defaultProcess;

	public MachineRecipeSerializer(IFactory<T> factory, int defaultProcess) {
		this.factory = factory;
		this.defaultProcess = defaultProcess;
	}


	@Override
	public T read(ResourceLocation recipeId, JsonObject json) {
		String s = JSONUtils.getString(json, "group", "");
		JsonArray arr = JSONUtils.getJsonArray(json, "ingredients");
		Builder b = new Builder();
		b.ingredients = NonNullList.create();
		for (JsonElement elem : arr) {
			Ingredient ingredient = Ingredient.deserialize(elem);
			if (!ingredient.hasNoMatchingItems())
				b.ingredients.add(ingredient);
		}
		if (!json.has("result")) throw new com.google.gson.JsonSyntaxException("Missing result, expected to find a string or object");
		if (json.get("result").isJsonObject()) b.result = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
		else {
			String s1 = JSONUtils.getString(json, "result");
			ResourceLocation location = new ResourceLocation(s1);
			Item item = ForgeRegistries.ITEMS.getValue(location);
			if (item == null) throw new IllegalStateException("Item: " + s1 + " does not exist");
			b.result = new ItemStack(item);
		}

		b.experience = JSONUtils.getFloat(json, "experience", 0.0F);
		b.process = JSONUtils.getInt(json, "processtime", defaultProcess);
		if (json.has("cost")) {
			JsonElement jc = json.get("cost");
			if (jc.isJsonPrimitive()) {
				b.itemCost = IntLists.singleton(jc.getAsInt());
			} else if (jc.isJsonArray()) {
				JsonArray ja = jc.getAsJsonArray();
				IntList cl = new IntArrayList(ja.size());
				for (JsonElement je : ja) {
					cl.add(je.getAsInt());
				}
				b.itemCost = cl;
			}
		} else {
			b.itemCost = DEFAULT_COST;
		}
		return factory.create(recipeId, s, b);
	}

	@Nullable
	@Override
	public T read(ResourceLocation recipeId, PacketBuffer buffer) {
		String s = buffer.readString(32767);
		Builder b = new Builder();
		int size = buffer.readByte();
		b.ingredients = NonNullList.create();
		for (int i = 0; i < size; i++) {
			b.ingredients.add(Ingredient.read(buffer));
		}
		b.result = buffer.readItemStack();
		b.experience = buffer.readFloat();
		b.process = buffer.readVarInt();
		size = buffer.readByte();
		if (size > 0) {
			if (size == 1) b.itemCost = IntLists.singleton(buffer.readVarInt());
			else {
				b.itemCost = new IntArrayList(size);
				for (int i = 0; i < size; i++) {
					b.itemCost.add(buffer.readVarInt());
				}
			}
		}
		return factory.create(recipeId, s, b);
	}

	@Override
	public void write(PacketBuffer buffer, T recipe) {
		buffer.writeString(recipe.group);
		buffer.writeByte(recipe.ingredients.size());
		for (Ingredient ingredient : recipe.ingredients)
			ingredient.write(buffer);
		buffer.writeItemStack(recipe.result);
		buffer.writeFloat(recipe.experience);
		buffer.writeVarInt(recipe.processTime);
		buffer.writeByte(recipe.itemCost.size());
		for (int cost : recipe.itemCost) {
			buffer.writeVarInt(cost);
		}
	}

	public interface IFactory<T extends AbstractMachineRecipe> {
		T create(ResourceLocation id, String group, Builder builder);
	}

	public static class Builder {
		NonNullList<Ingredient> ingredients;
		ItemStack result;
		float experience;
		int process;
		IntList itemCost;
	}
}
