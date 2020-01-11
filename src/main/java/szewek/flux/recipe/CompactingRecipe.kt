package szewek.flux.recipe

import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.util.ResourceLocation
import szewek.flux.FBlocks
import szewek.flux.FRecipes

class CompactingRecipe(idIn: ResourceLocation, groupIn: String, builder: MachineRecipeSerializer.Builder)
    : AbstractMachineRecipe(FRecipes.COMPACTING, idIn, groupIn, builder) {
    override fun getIcon(): ItemStack {
        return ItemStack(FBlocks.COMPACTOR)
    }

    override fun getSerializer(): IRecipeSerializer<*> = FRecipes.COMPACTING_SERIALIZER
}