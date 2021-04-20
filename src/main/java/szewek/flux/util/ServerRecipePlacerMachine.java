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
import szewek.flux.util.inventory.IOSize;

public final class ServerRecipePlacerMachine<C extends IInventory> extends ServerRecipePlacer<C> {
	private boolean matches;
	private final IOSize ioSize;

	public ServerRecipePlacerMachine(RecipeBookContainer<C> container, IOSize ioSize) {
		super(container);
		this.ioSize = ioSize;
	}

	@Override
	protected void handleRecipeClicked(IRecipe<C> recipe, boolean placeAll) {
		matches = menu.recipeMatches(recipe);
		int i = stackedContents.getBiggestCraftableStack(recipe, null);
		int j;
		if (matches) {
			j = recipe.getIngredients().size();
			int r = ioSize.in;
			for (int k = 0; k < j; ++k) {
				ItemStack stack = menu.getSlot(k).getItem();
				if (stack.isEmpty() || i <= stack.getCount()) {
					--r;
				}
			}
			if (r < j) {
				return;
			}
		}

		j = getStackSize(placeAll, i, matches);
		IntList intList = new IntArrayList();
		if (stackedContents.canCraft(recipe, intList, j)) {
			if (!matches) {
				for(int n = ioSize.all - 1; n >= 0; --n) {
					moveItemToInventory(n);
				}
			}
			consume(j, intList);
		}

	}

	@Override
	protected void clearGrid() {
		int l = ioSize.all;
		for(int i = ioSize.in; i < l; ++i) {
			moveItemToInventory(i);
		}
		super.clearGrid();
	}

	protected void consume(int amount, IntList intList) {
		IntIterator iterator = intList.iterator();
		byte i = 0;
		while(iterator.hasNext() && i < ioSize.in) {
			Slot slot = menu.getSlot(i++);
			ItemStack stack = RecipeItemHelper.fromStackingIndex(iterator.nextInt());
			if (!stack.isEmpty()) {
				int m = Math.min(stack.getMaxStackSize(), amount);
				if (matches) {
					m -= slot.getItem().getCount();
				}
				while(m > 0) {
					moveItemToGrid(slot, stack);
					--m;
				}
			}
		}

	}
}
