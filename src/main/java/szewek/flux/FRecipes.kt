package szewek.flux

import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.item.crafting.IRecipeType
import net.minecraft.util.ResourceLocation
import net.minecraft.util.registry.Registry
import net.minecraftforge.registries.IForgeRegistry
import szewek.flux.recipe.*
import szewek.ktutils.at

object FRecipes {
    @JvmField var GRINDING = addType<GrindingRecipe>("grinding")
    @JvmField var ALLOYING = addType<AlloyingRecipe>("alloying")
    @JvmField var WASHING = addType<WashingRecipe>("washing")
    @JvmField var COMPACTING = addType<CompactingRecipe>("compacting")
    @JvmField var GRINDING_SERIALIZER = MachineRecipeSerializer(MachineRecipeSerializer.factory(::GrindingRecipe), 200)
    @JvmField var ALLOYING_SERIALIZER = MachineRecipeSerializer(MachineRecipeSerializer.factory(::AlloyingRecipe), 200)
    @JvmField var WASHING_SERIALIZER = MachineRecipeSerializer(MachineRecipeSerializer.factory(::WashingRecipe), 200)
    @JvmField var COMPACTING_SERIALIZER = MachineRecipeSerializer(MachineRecipeSerializer.factory(::CompactingRecipe), 200)

    @JvmStatic
    fun register(reg: IForgeRegistry<IRecipeSerializer<*>?>) {
        reg.registerAll(
                GRINDING_SERIALIZER.setRegistryName(FluxMod.MODID, "grinding"),
                ALLOYING_SERIALIZER.setRegistryName(FluxMod.MODID, "alloying"),
                WASHING_SERIALIZER.setRegistryName(FluxMod.MODID, "washing"),
                COMPACTING_SERIALIZER.setRegistryName(FluxMod.MODID, "compacting")
        )
    }

    private fun <T : IRecipe<*>> addType(key: String): IRecipeType<T> {
        return Registry.register(Registry.RECIPE_TYPE, FluxMod.MODID at key, object : IRecipeType<T> {
            override fun toString(): String {
                return key
            }
        })
    }
}