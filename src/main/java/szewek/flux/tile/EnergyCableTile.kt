package szewek.flux.tile

import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.energy.IEnergyStorage
import szewek.flux.FTiles
import java.util.*
import javax.annotation.Nonnull

class EnergyCableTile : TileEntity(FTiles.ENERGY_CABLE), ITickableTileEntity {
    private var energy = 0
    private var isDirty = false
    private val sides = arrayOfNulls<Sided>(6)
    override fun read(compound: CompoundNBT) {
        super.read(compound)
        energy = compound.getInt("E")
        if (energy >= maxEnergy) energy = maxEnergy
    }

    override fun write(compound: CompoundNBT): CompoundNBT {
        super.write(compound)
        compound.putInt("E", energy)
        return compound
    }

    override fun tick() {
        if (!world!!.isRemote) {
            val availableSides: MutableSet<Direction> = EnumSet.allOf(Direction::class.java)
            for (dir in Direction.values()) {
                val side = sides[dir.index]
                if (side!!.extracted || side.received) {
                    availableSides.remove(dir)
                    side.received = false
                    side.extracted = side.received
                }
            }
            for (dir in availableSides) {
                val te = world!!.getTileEntity(pos.offset(dir))
                te?.getCapability(CapabilityEnergy.ENERGY, dir.opposite)?.ifPresent { ie: IEnergyStorage ->
                    if (ie.canReceive()) {
                        var r = sendAmount
                        if (r >= energy) r = energy
                        r = ie.receiveEnergy(r, true)
                        if (r > 0) {
                            energy -= r
                            isDirty = true
                            ie.receiveEnergy(r, false)
                        }
                    }
                }
            }
        }
        if (isDirty) {
            markDirty()
            isDirty = false
        }
    }

    @Nonnull
    override fun <T> getCapability(@Nonnull cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (!removed) {
            if (cap === CapabilityEnergy.ENERGY && side != null) {
                return sides[side.index]!!.lazy.cast()
            }
        }
        return super.getCapability(cap, side)
    }

    inner class Sided : IEnergyStorage {
        var extracted = false
        var received = false
        var lazy: LazyOptional<IEnergyStorage> = LazyOptional.of<IEnergyStorage> { this }
        override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
            var r = maxReceive
            if (r > maxEnergy - energy) r = maxEnergy - energy
            if (!simulate) {
                energy += r
                received = true
            }
            return r
        }

        override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int {
            var r = maxExtract
            if (r > energy) r = energy
            if (!simulate) {
                energy -= r
                extracted = true
            }
            return r
        }

        override fun getEnergyStored() = energy

        override fun getMaxEnergyStored() = maxEnergy

        override fun canExtract() = true

        override fun canReceive() = true
    }

    companion object {
        private const val maxEnergy = 50000
        private const val sendAmount = 5000
    }

    init {
        for (i in 0..5) {
            sides[i] = Sided()
        }
    }
}