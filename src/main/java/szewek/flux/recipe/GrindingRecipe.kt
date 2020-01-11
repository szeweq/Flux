package szewek.flux.recipe

import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.util.ResourceLocation
import szewek.flux.FBlocks
import szewek.flux.FRecipes

class GrindingRecipe(idIn: ResourceLocation, groupIn: String, builder: MachineRecipeSerializer.Builder)
    : AbstractMachineRecipe(FRecipes.GRINDING, idIn, groupIn, builder) {
    override fun getIcon(): ItemStack {
        return ItemStack(FBlocks.GRINDING_MILL)
    }

    override fun getSerializer(): IRecipeSerializer<*> = FRecipes.GRINDING_SERIALIZER
}