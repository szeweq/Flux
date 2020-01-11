package szewek.flux.util

import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntIterator
import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.container.RecipeBookContainer
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.RecipeItemHelper
import net.minecraft.item.crafting.ServerRecipePlacer

class ServerRecipePlacerMachine<C : IInventory>(
        container: RecipeBookContainer<C>,
        private val inputSize: Int,
        private val outputSize: Int
) : ServerRecipePlacer<C>(container) {
    private var matches = false
    override fun tryPlaceRecipe(recipe: IRecipe<C>, placeAll: Boolean) {
        matches = recipeBookContainer.matches(recipe)
        val i = recipeItemHelper.getBiggestCraftableStack(recipe, null)
        if (matches) {
            val s = recipe.ingredients.size
            var r = inputSize
            for (j in 0 until s) {
                val stack = recipeBookContainer.getSlot(j).stack
                if (stack.isEmpty || i <= stack.count) --r
            }
            if (r < s) return
        }
        val j = getMaxAmount(placeAll, i, matches)
        val intList: IntList = IntArrayList()
        if (recipeItemHelper.canCraft(recipe, intList, j)) {
            if (!matches) {
                for (k in inputSize + outputSize - 1 downTo 0) giveToPlayer(k)
            }
            consume(j, intList)
        }
    }

    override fun clear() {
        for (i in inputSize until inputSize + outputSize) giveToPlayer(i)
        super.clear()
    }

    protected fun consume(amount: Int, intList: IntList) {
        val iterator: IntIterator = intList.iterator()
        val i = 0
        while (iterator.hasNext() && i < inputSize) {
            val slot = recipeBookContainer.getSlot(i)
            val stack = RecipeItemHelper.unpack(iterator.nextInt())
            if (!stack.isEmpty) {
                var m = Math.min(stack.maxStackSize, amount)
                if (matches) m -= slot.stack.count
                while (m > 0) {
                    consumeIngredient(slot, stack)
                    --m
                }
            }
        }
    }

}