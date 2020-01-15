package szewek.flux.recipe;

import it.unimi.dsi.fastutil.ints.IntList;
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

public abstract class AbstractMachineRecipe implements IRecipe<IInventoryIO> {
	public final NonNullList<Ingredient> ingredientsList;
	public final ItemStack result;
	public final float experience;
	public final int processTime;
	public final IntList itemCost;
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
		this.itemCost = builder.itemCost;
	}

	public IRecipeType<?> getType() {
		return this.type;
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public String getGroup() {
		return group;
	}

	public ItemStack getRecipeOutput() {
		return result;
	}

	public NonNullList<Ingredient> getIngredients() {
		return this.ingredientsList;
	}

	public boolean matches(IInventoryIO inv, World worldIn) {
		ArrayList<ItemStack> filledInputs = new ArrayList<>();

		for (ItemStack stack : inv.getInputs()) {
			if (!stack.isEmpty()) {
				filledInputs.add(stack);
			}
		}

		int[] match = RecipeMatcher.findMatches(filledInputs, this.ingredientsList);
		if (match != null) {
			for(int i = 0; i < match.length; ++i) {
				if (filledInputs.get(i).getCount() < this.getCostAt(match[i])) {
					return false;
				}
			}
			return true;
		} else return false;
	}

	public ItemStack getCraftingResult(IInventoryIO inv) {
		return this.result.copy();
	}

	public boolean canFit(int width, int height) {
		return ingredientsList.size() <= width * height;
	}

	public final int getCostAt(int n) {
		return this.itemCost.getInt(n >= itemCost.size() ? itemCost.size() - 1 : n);
	}


}
