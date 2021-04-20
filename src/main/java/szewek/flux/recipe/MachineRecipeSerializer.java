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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class MachineRecipeSerializer<T extends AbstractMachineRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
	private static final String RESULT = "result";
	private final BiFunction<ResourceLocation, Builder, T> factory;
	private final int defaultProcess;
	private final Builder builder = new Builder();

	public MachineRecipeSerializer(BiFunction<ResourceLocation, Builder, T> factory, int defaultProcess) {
		this.factory = factory;
		this.defaultProcess = defaultProcess;
	}

	@Nullable
	@Override
	public T fromJson(ResourceLocation recipeId, JsonObject json) {
		if (!json.has(RESULT)) {
			throw new JsonSyntaxException("Missing result, expected to find a string or object");
		}
		boolean tagResult = false;
		builder.clear();
		Builder b = builder;
		if (json.get(RESULT).isJsonObject()) {
			tagResult = readResultObject(json, b);
			if (b.result == ItemStack.EMPTY) {
				return null;
			}
		} else {
			String result = JSONUtils.getAsString(json, RESULT);
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

		b.experience = JSONUtils.getAsFloat(json, "experience", 0.0F);
		b.process = JSONUtils.getAsInt(json, "processtime", defaultProcess);
		b.group = JSONUtils.getAsString(json, "group", "");

		return factory.apply(recipeId, b);
	}

	@Override
	public T fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
		Builder b = new Builder();
		b.group = buffer.readUtf(32767);
		int size = buffer.readByte();

		for(int i = 0; i < size; ++i) {
			b.ingredients.add(Ingredient.fromNetwork(buffer));
		}

		b.result = buffer.readItem();
		b.experience = buffer.readFloat();
		b.process = buffer.readVarInt();
		return factory.apply(recipeId, b);
	}

	@Override
	public void toNetwork(PacketBuffer buffer, T recipe) {
		buffer.writeUtf(recipe.getGroup());
		buffer.writeByte(recipe.ingredients.size());

		for (Ingredient ingredient : recipe.ingredients) {
			ingredient.toNetwork(buffer);
		}

		buffer.writeItem(recipe.result);
		buffer.writeFloat(recipe.experience);
		buffer.writeVarInt(recipe.processTime);
	}

	private static boolean readResultObject(JsonObject json, Builder b) {
		JsonObject ro = JSONUtils.getAsJsonObject(json, RESULT);
		if (ro.has("item")) {
			b.result = ShapedRecipe.itemFromJson(ro);
		}
		if (ro.has("tag") && b.result == ItemStack.EMPTY) {
			b.result = RecipeTagCompat.findItemTag(ro);
			return true;
		}
		return false;
	}
	private static void readIngredients(JsonObject json, List<Ingredient> ings) {
		JsonArray arr = JSONUtils.getAsJsonArray(json, "ingredients");
		for (JsonElement jc : arr) {
			Ingredient ingredient = Ingredient.fromJson(jc);
			if (!ingredient.isEmpty()) {
				ings.add(ingredient);
			}
		}
	}

	public static final class Builder {
		public final ArrayList<Ingredient> ingredients = new ArrayList<>();
		public ItemStack result = ItemStack.EMPTY;
		public String group = "";
		public float experience;
		public int process;

		public Builder withGroup(String group) {
			this.group = group;
			return this;
		}

		public NonNullList<Ingredient> buildIngredients() {
			List<Ingredient> icopy = new ArrayList<>(ingredients);
			return new NonNullList<>(icopy, Ingredient.EMPTY);
		}

		void clear() {
			ingredients.clear();
			result = ItemStack.EMPTY;
		}
	}
}
