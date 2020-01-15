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

	protected void tryPlaceRecipe(IRecipe recipe, boolean placeAll) {
		this.matches = this.recipeBookContainer.matches(recipe);
		int i = this.recipeItemHelper.getBiggestCraftableStack(recipe, null);
		int j;
		int k;
		if (this.matches) {
			j = recipe.getIngredients().size();
			int r = this.inputSize;
			k = 0;

			for(int var7 = j; k < var7; ++k) {
				Slot var10000 = this.recipeBookContainer.getSlot(k);
				ItemStack stack = var10000.getStack();
				if (stack.isEmpty() || i <= stack.getCount()) {
					--r;
				}
			}
			if (r < j) {
				return;
			}
		}

		j = this.getMaxAmount(placeAll, i, this.matches);
		IntList intList = new IntArrayList();
		if (this.recipeItemHelper.canCraft(recipe, intList, j)) {
			if (!this.matches) {
				for(int n = inputSize + outputSize - 1; n >= 0; --n) {
					this.giveToPlayer(n);
				}
			}
			this.consume(j, intList);
		}

	}

	protected void clear() {
		int l = inputSize + outputSize;
		for(int i = inputSize; i < l; ++i) {
			this.giveToPlayer(i);
		}
		super.clear();
	}

	protected final void consume(int amount, IntList intList) {
		IntIterator iterator = intList.iterator();
		byte i = 0;
		while(iterator.hasNext() && i < this.inputSize) {
			Slot slot = this.recipeBookContainer.getSlot(i++);
			ItemStack stack = RecipeItemHelper.unpack(iterator.nextInt());
			if (!stack.isEmpty()) {
				int m = Math.min(stack.getMaxStackSize(), amount);
				if (this.matches) {
					m -= slot.getStack().getCount();
				}
				while(m > 0) {
					this.consumeIngredient(slot, stack);
					--m;
				}
			}
		}

	}
}
