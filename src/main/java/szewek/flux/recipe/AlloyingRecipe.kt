package szewek.flux.recipe

import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.util.ResourceLocation
import szewek.flux.FBlocks
import szewek.flux.FRecipes

class AlloyingRecipe(idIn: ResourceLocation, groupIn: String, builder: MachineRecipeSerializer.Builder)
    : AbstractMachineRecipe(FRecipes.ALLOYING, idIn, groupIn, builder) {
    override fun getIcon() = ItemStack(FBlocks.ALLOY_CASTER)

    override fun getSerializer(): IRecipeSerializer<*> = FRecipes.ALLOYING_SERIALIZER
}