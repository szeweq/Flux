package szewek.flux.tile

import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.energy.IEnergyStorage
import szewek.flux.energy.IEnergyReceiver

abstract class PoweredTile(tileEntityTypeIn: TileEntityType<*>) : TileEntity(tileEntityTypeIn), IEnergyReceiver, ITickableTileEntity {
    internal var energy = 0;
    private val maxEnergy = 500000;

    override fun read(compound: CompoundNBT) {
        super.read(compound)
        energy = compound.getInt("E")
    }

    override fun write(compound: CompoundNBT): CompoundNBT {
        super.write(compound)
        compound.putInt("E", energy)
        return compound
    }

    override fun getMaxEnergyStored(): Int = maxEnergy

    override fun getEnergyStored(): Int = energy

    override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
        var r = maxReceive
        if (r > maxEnergy - energy) r = maxEnergy - energy
        if (!simulate) {
            energy += r
        }
        return r
    }

    private val handler = LazyOptional.of<IEnergyStorage> { this }

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (!removed && cap == CapabilityEnergy.ENERGY) return handler.cast()
        return super.getCapability(cap, side)
    }
}