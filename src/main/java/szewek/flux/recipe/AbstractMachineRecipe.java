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
import szewek.fl.network.FluxAnalytics;
import szewek.fl.recipe.CountedIngredient;
import szewek.fl.type.FluxRecipeType;
import szewek.flux.util.inventory.IInventoryIO;

import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class AbstractMachineRecipe implements IRecipe<IInventoryIO>, Consumer<Iterable<ItemStack>> {
	public final NonNullList<Ingredient> ingredients;
	public final ItemStack result;
	public final float experience;
	public final int processTime;
	private final FluxRecipeType<?> type;
	private final ResourceLocation id;
	private final String group;

	public AbstractMachineRecipe(FluxRecipeType<?> type, ResourceLocation id, MachineRecipeSerializer.Builder builder) {
		this.type = type;
		this.id = id;
		group = builder.group;
		ingredients = builder.buildIngredients();
		result = builder.result;
		experience = builder.experience;
		processTime = builder.process;
	}

	@Override
	public IRecipeType<?> getType() {
		return type;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return result;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return type.serializer;
	}

	@Override
	public boolean matches(IInventoryIO inv, World worldIn) {
		ArrayList<ItemStack> filledInputs = new ArrayList<>();

		int inputSize = inv.getIOSize().in;
		for (int i = 0; i < inputSize; i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				filledInputs.add(stack);
			}
		}

		int[] match = RecipeMatcher.findMatches(filledInputs, ingredients);
		return match != null;
	}

	@Override
	public ItemStack getCraftingResult(IInventoryIO inv) {
		return result.copy();
	}

	@Override
	public boolean canFit(int width, int height) {
		return ingredients.size() <= width * height;
	}

	@Override
	public final void accept(Iterable<ItemStack> stacks) {
		ArrayList<ItemStack> filledInputs = new ArrayList<>(ingredients.size());

		for (ItemStack stack : stacks) {
			if (!stack.isEmpty()) {
				filledInputs.add(stack);
			}
		}

		int[] match = RecipeMatcher.findMatches(filledInputs, ingredients);
		if (match != null) {
			FluxAnalytics.putView("flux/recipe/use/" + type.toString() + "/" + id.toString());
			for(int i = 0; i < match.length; ++i) {
				Ingredient ingredient = ingredients.get(match[i]);
				int count = ingredient instanceof CountedIngredient ? ((CountedIngredient) ingredient).getCount() : 1;
				filledInputs.get(i).grow(-count);
			}
		}
	}
}
