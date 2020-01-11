package szewek.flux.recipe

import net.minecraft.block.Blocks
import net.minecraft.fluid.Fluids
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraftforge.fluids.FluidStack
import java.util.*

object FluxGenRecipes {
    private val catalysts: MutableMap<Item, Result> = HashMap()
    private val coldFluids: MutableMap<FluidStack, Result> = HashMap()
    private val hotFluids: MutableMap<FluidStack, Result> = HashMap()
    @JvmStatic
    fun isCatalyst(item: Item): Boolean {
        if (catalysts.containsKey(item)) return true
        return false
    }

    @JvmStatic
    fun getCatalyst(item: Item): Result {
        return catalysts.getOrDefault(item, Result.DEFAULT)
    }

    @JvmStatic
    fun isHotFluid(stack: FluidStack?): Boolean {
        return isFluid(stack, hotFluids)
    }

    @JvmStatic
    fun getHotFluid(stack: FluidStack?): Result {
        return getFluid(stack, hotFluids)
    }

    @JvmStatic
    fun isColdFluid(stack: FluidStack?): Boolean {
        return isFluid(stack, coldFluids)
    }

    @JvmStatic
    fun getColdFluid(stack: FluidStack?): Result {
        return getFluid(stack, coldFluids)
    }

    private fun isFluid(stack: FluidStack?, m: Map<FluidStack, Result?>): Boolean {
        if (stack == null) return false
        if (m.containsKey(stack)) return true
        val fl = stack.fluid
        for (fs in m.keys) {
            if (fs.rawFluid === fl) return true
        }
        return false
    }

    private fun getFluid(stack: FluidStack?, m: Map<FluidStack, Result?>): Result {
        if (stack == null) return Result.DEFAULT
        if (m.containsKey(stack)) return m[stack]?: Result.DEFAULT
        val fl = stack.fluid
        for (fs in m.keys) {
            if (fs.rawFluid === fl) return m[fs]?: Result.DEFAULT
        }
        return Result.DEFAULT
    }

    class Result internal constructor(@JvmField val factor: Int, @JvmField val usage: Int) {

        companion object {
            @JvmField val DEFAULT = Result(1, 0)
        }

    }

    init {
        catalysts[Items.FLINT] = Result(2, 2)
        catalysts[Items.REDSTONE] = Result(2, 1)
        catalysts[Blocks.REDSTONE_BLOCK.asItem()] = Result(10, 1)
        catalysts[Items.BLAZE_POWDER] = Result(4, 1)
        catalysts[Items.PRISMARINE_SHARD] = Result(3, 1)
        catalysts[Items.CONDUIT] = Result(30, 1)
        catalysts[Items.DRAGON_BREATH] = Result(60, 1)
        catalysts[Items.NETHER_STAR] = Result(100, 1)
        catalysts[Items.TOTEM_OF_UNDYING] = Result(200, 1)
        hotFluids[FluidStack(Fluids.LAVA, 0)] = Result(2, 200)
        coldFluids[FluidStack(Fluids.WATER, 0)] = Result(50, 200)
    }
}