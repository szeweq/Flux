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

public final class ServerRecipePlacerMachine<C extends IInventory> extends ServerRecipePlacer<C> {
	private boolean matches;
	private final int inputSize;
	private final int outputSize;

	public ServerRecipePlacerMachine(RecipeBookContainer<C> container, int inputSize, int outputSize) {
		super(container);
		this.inputSize = inputSize;
		this.outputSize = outputSize;
	}

	@Override
	protected void tryPlaceRecipe(IRecipe<C> recipe, boolean placeAll) {
		matches = recipeBookContainer.matches(recipe);
		int i = recipeItemHelper.getBiggestCraftableStack(recipe, null);
		int j;
		if (matches) {
			j = recipe.getIngredients().size();
			int r = inputSize;
			for (int k = 0; k < j; ++k) {
				ItemStack stack = recipeBookContainer.getSlot(k).getStack();
				if (stack.isEmpty() || i <= stack.getCount()) {
					--r;
				}
			}
			if (r < j) return;
		}

		j = getMaxAmount(placeAll, i, matches);
		IntList intList = new IntArrayList();
		if (recipeItemHelper.canCraft(recipe, intList, j)) {
			if (!matches) {
				for(int n = inputSize + outputSize - 1; n >= 0; --n) {
					giveToPlayer(n);
				}
			}
			consume(j, intList);
		}

	}

	@Override
	protected void clear() {
		int l = inputSize + outputSize;
		for(int i = inputSize; i < l; ++i) {
			giveToPlayer(i);
		}
		super.clear();
	}

	protected final void consume(int amount, IntList intList) {
		IntIterator iterator = intList.iterator();
		byte i = 0;
		while(iterator.hasNext() && i < inputSize) {
			Slot slot = recipeBookContainer.getSlot(i++);
			ItemStack stack = RecipeItemHelper.unpack(iterator.nextInt());
			if (!stack.isEmpty()) {
				int m = Math.min(stack.getMaxStackSize(), amount);
				if (matches) {
					m -= slot.getStack().getCount();
				}
				while(m > 0) {
					consumeIngredient(slot, stack);
					--m;
				}
			}
		}

	}
}
