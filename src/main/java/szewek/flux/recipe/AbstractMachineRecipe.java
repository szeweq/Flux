package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.RecipeMatcher;
import szewek.flux.util.IInventoryIO;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMachineRecipe implements IRecipe<IInventoryIO> {
	public final NonNullList<Ingredient> ingredients;
	public final ItemStack result;
	public final float experience;
	public final int processTime;
	private final FluxRecipeType<?> type;
	private final ResourceLocation id;
	private final String group;

	public AbstractMachineRecipe(FluxRecipeType<?> type, ResourceLocation id, String group, MachineRecipeSerializer.Builder builder) {
		this.type = type;
		this.id = id;
		this.group = group;
		ingredients = builder.ingredients;
		result = builder.result;
		experience = builder.experience;
		processTime = builder.process;
	}

	public IRecipeType<?> getType() {
		return type;
	}

	public ResourceLocation getId() {
		return id;
	}

	public String getGroup() {
		return group;
	}

	public ItemStack getRecipeOutput() {
		return result;
	}

	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return type.serializer;
	}

	public boolean matches(IInventoryIO inv, World worldIn) {
		ArrayList<ItemStack> filledInputs = new ArrayList<>();

		for (ItemStack stack : inv.getInputs()) {
			if (!stack.isEmpty()) filledInputs.add(stack);
		}

		int[] match = RecipeMatcher.findMatches(filledInputs, ingredients);
		return match != null;
	}

	public ItemStack getCraftingResult(IInventoryIO inv) {
		return result.copy();
	}

	public boolean canFit(int width, int height) {
		return ingredients.size() <= width * height;
	}

	public final void consumeItems(List<ItemStack> stacks) {
		ArrayList<ItemStack> filledInputs = new ArrayList<>();

		for (ItemStack stack : stacks) {
			if (!stack.isEmpty()) filledInputs.add(stack);
		}

		int[] match = RecipeMatcher.findMatches(filledInputs, ingredients);
		if (match != null) {
			for(int i = 0; i < match.length; ++i) {
				Ingredient ingredient = ingredients.get(match[i]);
				int count = ingredient instanceof CountedIngredient ? ((CountedIngredient) ingredient).count : 1;
				filledInputs.get(i).grow(-count);
			}
		}
	}
}
