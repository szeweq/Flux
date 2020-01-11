package szewek.flux.energy

import net.minecraft.block.AbstractFurnaceBlock
import net.minecraft.item.crafting.IRecipe
import net.minecraft.tileentity.AbstractFurnaceTileEntity

class FurnaceEnergy(private val furnace: AbstractFurnaceTileEntity) : EnergyCapable() {
    override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
        if (!canReceive() || maxReceive <= 0) return 0
        val w = furnace.world ?: return 0
        val data = furnace.furnaceData
        val burnTime = data[0] * USE
        if (burnTime >= CAP) return 0
        var r = CAP - burnTime
        if (r > maxReceive) r = maxReceive - maxReceive % USE
        if (!simulate && r / USE > 0) {
            val maxBurnTime = data[1] * USE
            if (maxBurnTime < CAP) data[1] = CAP / USE
            data[0] = (burnTime + r) / USE
            if (burnTime == 0) {
                val pos = furnace.pos
                w.setBlockState(pos, w.getBlockState(pos).with(AbstractFurnaceBlock.LIT, true), 3)
            }
            furnace.markDirty()
        }
        return r
    }

    override fun getEnergyStored() = Math.min(furnace.furnaceData[0] * USE, CAP)

    override fun getMaxEnergyStored() = CAP

    override fun canReceive(): Boolean {
        if (furnace.isRemoved) return false
        val w = furnace.world
        return if (w != null) {
            w.recipeManager
                    .getRecipe(furnace.recipeType, furnace, w)
                    .map { recipeIn: IRecipe<*>? -> furnace.canSmelt(recipeIn) }
                    .orElse(false)
        } else false
    }

    companion object {
        private const val CAP = 25000
        private const val USE = 20
    }

}