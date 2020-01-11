package szewek.flux.recipe

import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.IRecipeType
import net.minecraft.item.crafting.Ingredient
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.common.util.RecipeMatcher
import szewek.flux.util.IInventoryIO
import java.util.*

abstract class AbstractMachineRecipe(
        private val type: IRecipeType<*>,
        private val id: ResourceLocation,
        private val _group: String,
        builder: MachineRecipeSerializer.Builder
) : IRecipe<IInventoryIO> {
    @JvmField internal val ingredientsList: NonNullList<Ingredient> = builder.ingredients
    @JvmField internal val result: ItemStack = builder.result!!
    @JvmField val experience: Float = builder.experience
    @JvmField val processTime: Int = builder.process
    @JvmField internal val itemCost: IntList = builder.itemCost!!

    override fun getType(): IRecipeType<*> = type

    override fun getId(): ResourceLocation = id

    override fun getGroup(): String = _group

    override fun getRecipeOutput(): ItemStack = result

    override fun getIngredients(): NonNullList<Ingredient> = ingredientsList

    override fun matches(inv: IInventoryIO, worldIn: World): Boolean {
        val filledInputs = ArrayList<ItemStack>()
        for (stack in inv.inputs) {
            if (!stack.isEmpty) {
                filledInputs.add(stack)
            }
        }
        val match = RecipeMatcher.findMatches(filledInputs, ingredientsList) ?: return false
        for (i in match.indices) {
            if (filledInputs[i].count < getCostAt(match[i])) return false
        }
        return true
    }

    override fun getCraftingResult(inv: IInventoryIO): ItemStack = result.copy()

    override fun canFit(width: Int, height: Int) = ingredientsList.size <= width * height

    fun getCostAt(n: Int): Int {
        return itemCost.getInt(if (n >= itemCost.size) itemCost.size - 1 else n)
    }

}