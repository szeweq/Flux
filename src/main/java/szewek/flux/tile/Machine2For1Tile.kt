package szewek.flux.tile

import net.minecraft.item.crafting.IRecipeType
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.Direction
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import szewek.flux.FTiles
import szewek.flux.FTiles.TileType
import szewek.flux.recipe.AbstractMachineRecipe
import szewek.flux.recipe.AlloyingRecipe
import java.util.function.Function

class Machine2For1Tile(
        typeIn: TileEntityType<*>?,
        recipeTypeIn: IRecipeType<out AbstractMachineRecipe?>?,
        factory: MenuFactory,
        private val titleId: String
) : AbstractMachineTile(typeIn!!, recipeTypeIn!!, factory, 2, 1) {
    override fun getSlotsForFace(side: Direction) = when (side) {
        Direction.UP -> SLOTS_UP
        Direction.DOWN -> SLOTS_DOWN
        else -> SLOTS_SIDE
    }

    override fun getDefaultName() = TranslationTextComponent(titleId)

    companion object {
        private val SLOTS_UP = intArrayOf(0)
        private val SLOTS_SIDE = intArrayOf(1)
        private val SLOTS_DOWN = intArrayOf(2)

        @JvmStatic
        fun make(recipeType: IRecipeType<out AbstractMachineRecipe?>?, factory: MenuFactory, titleName: String): (TileType<Machine2For1Tile>) -> Machine2For1Tile {
            val titleId = "container.flux.$titleName"
            return { type -> Machine2For1Tile(type, recipeType, factory, titleId) }
        }
    }

}