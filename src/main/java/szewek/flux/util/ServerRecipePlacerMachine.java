package szewek.flux.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.ServerRecipePlacer;

public class ServerRecipePlacerMachine<C extends IInventory> extends ServerRecipePlacer<C> {
	private final int inputSize, outputSize;
	private boolean matches;

	public ServerRecipePlacerMachine(RecipeBookContainer<C> container, int inSize, int outSize) {
		super(container);
		inputSize = inSize;
		outputSize = outSize;
	}

	@Override
	protected void tryPlaceRecipe(IRecipe<C> recipe, boolean placeAll) {
		matches = recipeBookContainer.matches(recipe);
		int i = this.recipeItemHelper.getBiggestCraftableStack(recipe, null);
		if (matches) {
			int s = recipe.getIngredients().size();
			int r = inputSize;
			for (int j = 0; j < s; j++) {
				ItemStack stack = recipeBookContainer.getSlot(j).getStack();
				if (stack.isEmpty() || i <= stack.getCount()) --r;
			}
			if (r < s) return;
		}
		int j = getMaxAmount(placeAll, i, matches);
		IntList intList = new IntArrayList();
		if (recipeItemHelper.canCraft(recipe, intList, j)) {
			if (!matches) {
				for (int k = inputSize + outputSize - 1; k >= 0; k--)
					giveToPlayer(k);
			}
			consume(j, intList);
		}
	}

	@Override
	protected void clear() {
		for (int i = inputSize; i < inputSize + outputSize; i++) giveToPlayer(i);
		super.clear();
	}

	protected void consume(int amount, IntList intList) {
		IntIterator iterator = intList.iterator();
		int i = 0;
		while (iterator.hasNext() && i < inputSize) {
			Slot slot = recipeBookContainer.getSlot(i);
			ItemStack stack = RecipeItemHelper.unpack(iterator.nextInt());
			if (!stack.isEmpty()) {
				int m = Math.min(stack.getMaxStackSize(), amount);
				if (matches) m -= slot.getStack().getCount();
				while (m > 0) {
					consumeIngredient(slot, stack);
					--m;
				}
			}
		}
	}
}
