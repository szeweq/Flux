package szewek.flux.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class MachineRecipeSerializer<T extends AbstractMachineRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
	private final MachineRecipeSerializer.IFactory<T> factory;
	private final int defaultProcess;

	public MachineRecipeSerializer(MachineRecipeSerializer.IFactory<T> factory, int defaultProcess) {
		this.factory = factory;
		this.defaultProcess = defaultProcess;
	}

	public T read(ResourceLocation recipeId, JsonObject json) {
		String s = JSONUtils.getString(json, "group", "");
		JsonArray arr = JSONUtils.getJsonArray(json, "ingredients");
		MachineRecipeSerializer.Builder b = new MachineRecipeSerializer.Builder();
		for (JsonElement jc : arr) {
			Ingredient ingredient = Ingredient.deserialize(jc);
			if (!ingredient.hasNoMatchingItems()) {
				b.ingredients.add(ingredient);
			}
		}

		if (!json.has("result")) {
			throw new JsonSyntaxException("Missing result, expected to find a string or object");
		} else {
			if (json.get("result").isJsonObject()) {
				b.result = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
			} else {
				String s1 = JSONUtils.getString(json, "result");
				ResourceLocation location = new ResourceLocation(s1);
				Item item = ForgeRegistries.ITEMS.getValue(location);
				if (item == null) {
					throw new IllegalStateException("Item: " + s1 + " does not exist");
				}
				b.result = new ItemStack(item);
			}

			b.experience = JSONUtils.getFloat(json, "experience", 0.0F);
			b.process = JSONUtils.getInt(json, "processtime", this.defaultProcess);
			if (json.has("cost")) {
				JsonElement jc = json.get("cost");
				if (jc.isJsonPrimitive()) {
					b.costs = IntLists.singleton(jc.getAsInt());
				} else if (jc.isJsonArray()) {
					JsonArray ja = jc.getAsJsonArray();
					IntList cl = new IntArrayList(ja.size());
					for (JsonElement je : ja) {
						cl.add(je.getAsInt());
					}
					b.costs = cl;
				}
			} else b.costs = IntLists.singleton(1);

			return factory.create(recipeId, s, b);
		}
	}

	public T read(ResourceLocation recipeId, PacketBuffer buffer) {
		String s = buffer.readString(32767);
		MachineRecipeSerializer.Builder b = new MachineRecipeSerializer.Builder();
		int size = buffer.readByte();
		int var6 = 0;

		for(int var7 = size; var6 < var7; ++var6) {
			b.ingredients.add(Ingredient.read(buffer));
		}

		b.result = buffer.readItemStack();
		b.experience = buffer.readFloat();
		b.process = buffer.readVarInt();
		size = buffer.readByte();
		if (size > 0) {
			if (size == 1) {
				b.costs = IntLists.singleton(buffer.readVarInt());
			} else {
				IntArrayList itemCost = new IntArrayList(size);

				for(int var10 = 0; var10 < size; ++var10) {
					itemCost.add(buffer.readVarInt());
				}

				b.costs = itemCost;
			}
		}
		return factory.create(recipeId, s, b);
	}

	public void write(PacketBuffer buffer, T recipe) {
		buffer.writeString(recipe.getGroup());
		buffer.writeByte(recipe.ingredients.size());

		for (Ingredient ingredient : recipe.ingredients) {
			ingredient.write(buffer);
		}

		buffer.writeItemStack(recipe.result);
		buffer.writeFloat(recipe.experience);
		buffer.writeVarInt(recipe.processTime);
		int[] itemCost = recipe.getCosts();
		buffer.writeByte(itemCost.length);

		for (int n : itemCost) {
			buffer.writeVarInt(n);
		}

	}

	public interface IFactory<T extends AbstractMachineRecipe> {
		T create(ResourceLocation var1, String var2, MachineRecipeSerializer.Builder var3);
	}

	public static final class Builder {
		public NonNullList<Ingredient> ingredients = NonNullList.create();
		public ItemStack result = ItemStack.EMPTY;
		public float experience;
		public int process;
		public IntList costs;
	}
}
