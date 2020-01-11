package szewek.flux.util

import net.minecraft.item.ItemStack
import szewek.flux.item.MetalItem

enum class Metal(
        @JvmField val color: Int,
        @JvmField val metalName: String,
        @JvmField val harvestLevel: Int
) {
    IRON(0xE2C0BA, "iron", 1), GOLD(0xFFD700, "gold", 2), COPPER(0xE99868, "copper", 1), TIN(0xD0D3D6, "tin", 2);

    fun always() = true

    fun nonVanilla() = this != IRON && this != GOLD

    companion object {
        private fun getColor(stack: ItemStack): Int {
            val item = stack.item
            return if (item is MetalItem) {
                item.metal!!.color
            } else 0
        }

        @JvmStatic
        fun gritColors(stack: ItemStack, layer: Int): Int {
            return if (layer == 0) 0xFFFFFF else getColor(stack)
        }

        @JvmStatic
        fun ingotColors(stack: ItemStack, layer: Int): Int {
            return if (layer != 0) 0xFFFFFF else getColor(stack)
        }

        @JvmStatic
        fun itemColors(stack: ItemStack, layer: Int): Int {
            return getColor(stack)
        }
    }

}