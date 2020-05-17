package szewek.flux.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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

import javax.annotation.Nullable;
import java.util.List;

public final class MachineRecipeSerializer<T extends AbstractMachineRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
	private static final String RESULT = "result";
	private final MachineRecipeSerializer.IFactory<T> factory;
	private final int defaultProcess;

	public MachineRecipeSerializer(MachineRecipeSerializer.IFactory<T> factory, int defaultProcess) {
		this.factory = factory;
		this.defaultProcess = defaultProcess;
	}

	@Nullable
	@Override
	public T read(ResourceLocation recipeId, JsonObject json) {
		if (!json.has(RESULT)) {
			throw new JsonSyntaxException("Missing result, expected to find a string or object");
		}
		boolean tagResult = false;
		Builder b = new Builder();
		if (json.get(RESULT).isJsonObject()) {
			tagResult = readResultObject(json, b);
			if (b.result == ItemStack.EMPTY) {
				return null;
			}
		} else {
			String result = JSONUtils.getString(json, RESULT);
			ResourceLocation location = new ResourceLocation(result);
			Item item = ForgeRegistries.ITEMS.getValue(location);
			if (item == null) {
				throw new IllegalStateException("Item: " + result + " does not exist");
			}
			b.result = new ItemStack(item);
		}

		try {
			readIngredients(json, b.ingredients);
		} catch (JsonSyntaxException e) {
			if (tagResult) {
				return null;
			} else {
				throw e;
			}
		}

		b.experience = JSONUtils.getFloat(json, "experience", 0.0F);
		b.process = JSONUtils.getInt(json, "processtime", defaultProcess);

		String group = JSONUtils.getString(json, "group", "");
		return factory.create(recipeId, group, b);
	}

	@Override
	public T read(ResourceLocation recipeId, PacketBuffer buffer) {
		String s = buffer.readString(32767);
		Builder b = new Builder();
		int size = buffer.readByte();

		for(int i = 0; i < size; ++i) {
			b.ingredients.add(Ingredient.read(buffer));
		}

		b.result = buffer.readItemStack();
		b.experience = buffer.readFloat();
		b.process = buffer.readVarInt();
		return factory.create(recipeId, s, b);
	}

	@Override
	public void write(PacketBuffer buffer, T recipe) {
		buffer.writeString(recipe.getGroup());
		buffer.writeByte(recipe.ingredients.size());

		for (Ingredient ingredient : recipe.ingredients) {
			ingredient.write(buffer);
		}

		buffer.writeItemStack(recipe.result);
		buffer.writeFloat(recipe.experience);
		buffer.writeVarInt(recipe.processTime);
	}

	private static boolean readResultObject(JsonObject json, Builder b) {
		JsonObject ro = JSONUtils.getJsonObject(json, RESULT);
		if (ro.has("item")) {
			b.result = ShapedRecipe.deserializeItem(ro);
		}
		if (ro.has("tag") && b.result == ItemStack.EMPTY) {
			b.result = RecipeTagCompat.findItemTag(ro);
			return true;
		}
		return false;
	}
	private static void readIngredients(JsonObject json, List<Ingredient> ings) {
		JsonArray arr = JSONUtils.getJsonArray(json, "ingredients");
		for (JsonElement jc : arr) {
			Ingredient ingredient = Ingredient.deserialize(jc);
			if (!ingredient.hasNoMatchingItems()) {
				ings.add(ingredient);
			}
		}
	}

	public interface IFactory<T extends AbstractMachineRecipe> {
		T create(ResourceLocation var1, String var2, MachineRecipeSerializer.Builder var3);
	}

	public static final class Builder {
		public final NonNullList<Ingredient> ingredients = NonNullList.create();
		public ItemStack result = ItemStack.EMPTY;
		public float experience;
		public int process;
	}
}
