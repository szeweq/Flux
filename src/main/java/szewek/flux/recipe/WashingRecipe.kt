package szewek.flux.recipe

import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.util.ResourceLocation
import szewek.flux.FBlocks
import szewek.flux.FRecipes

class WashingRecipe(idIn: ResourceLocation, groupIn: String, builder: MachineRecipeSerializer.Builder)
    : AbstractMachineRecipe(FRecipes.WASHING, idIn, groupIn, builder) {
    override fun getIcon() = ItemStack(FBlocks.WASHER)

    override fun getSerializer(): IRecipeSerializer<*> = FRecipes.WASHING_SERIALIZER
}