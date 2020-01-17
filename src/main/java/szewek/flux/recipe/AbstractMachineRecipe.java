package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
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
	public final NonNullList<Ingredient> ingredientsList;
	public final ItemStack result;
	public final float experience;
	public final int processTime;
	private final int[] itemCost;
	private final IRecipeType<?> type;
	private final ResourceLocation id;
	private final String group;

	public AbstractMachineRecipe(IRecipeType<?> type, ResourceLocation id, String group, MachineRecipeSerializer.Builder builder) {
		this.type = type;
		this.id = id;
		this.group = group;
		this.ingredientsList = builder.ingredients;
		this.result = builder.result;
		this.experience = builder.experience;
		this.processTime = builder.process;
		this.itemCost = builder.itemCost.toIntArray();
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
		return ingredientsList;
	}

	public int[] getItemCost() {
		return itemCost;
	}

	public boolean matches(IInventoryIO inv, World worldIn) {
		ArrayList<ItemStack> filledInputs = new ArrayList<>();

		for (ItemStack stack : inv.getInputs()) {
			if (!stack.isEmpty()) filledInputs.add(stack);
		}

		int[] match = RecipeMatcher.findMatches(filledInputs, ingredientsList);
		if (match != null) {
			for(int i = 0; i < match.length; ++i) {
				if (filledInputs.get(i).getCount() < getCostAt(match[i])) {
					return false;
				}
			}
			return true;
		} else return false;
	}

	public ItemStack getCraftingResult(IInventoryIO inv) {
		return result.copy();
	}

	public boolean canFit(int width, int height) {
		return ingredientsList.size() <= width * height;
	}

	public final int getCostAt(int n) {
		return itemCost[n >= itemCost.length ? itemCost.length - 1 : n];
	}

	public final void consumeItems(List<ItemStack> stacks) {
		ArrayList<ItemStack> filledInputs = new ArrayList<>();

		for (ItemStack stack : stacks) {
			if (!stack.isEmpty()) filledInputs.add(stack);
		}

		int[] match = RecipeMatcher.findMatches(filledInputs, ingredientsList);
		if (match != null) {
			for(int i = 0; i < match.length; ++i) {
				filledInputs.get(i).grow(-getCostAt(match[i]));
			}
		}
	}
}
