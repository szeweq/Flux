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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMachineRecipe implements IRecipe<IInventoryIO> {
	protected final IRecipeType<?> type;
	protected final ResourceLocation id;
	protected final String group;
	protected final NonNullList<Ingredient> ingredients;
	protected final ItemStack result;
	protected final float experience;
	protected final int processTime;
	protected final IntList itemCost;

	public AbstractMachineRecipe(IRecipeType<?> typeIn, ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		type = typeIn;
		id = idIn;
		group = groupIn;
		ingredients = builder.ingredients;
		result = builder.result;
		experience = builder.experience;
		processTime = builder.process;
		itemCost = builder.itemCost;
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
	public boolean matches(IInventoryIO inv, World worldIn) {
		List<ItemStack> filledInputs = new ArrayList<>();
		for (ItemStack stack : inv.getInputs()) {
			if (!stack.isEmpty()) {
				filledInputs.add(stack);
			}
		}
		int[] match = RecipeMatcher.findMatches(filledInputs, ingredients);
		if (match == null)
			return false;
		for (int i = 0; i < match.length; i++) {
			if (filledInputs.get(i).getCount() < getCostAt(match[i]))
				return false;
		}
		return true;
	}

	@Override
	public ItemStack getCraftingResult(IInventoryIO inv) {
		return result.copy();
	}

	@Override
	public boolean canFit(int width, int height) {
		return ingredients.size() <= width * height;
	}

	public float getExperience() {
		return experience;
	}

	public int getProcessTime() {
		return processTime;
	}

	public int getCostAt(int n) {
		if (n >= itemCost.size())
			return itemCost.getInt(itemCost.size() - 1);
		return itemCost.getInt(n);
	}
}
