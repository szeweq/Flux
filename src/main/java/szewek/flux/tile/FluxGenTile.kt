package szewek.flux.tile

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.LockableTileEntity
import net.minecraft.util.Direction
import net.minecraft.util.IIntArray
import net.minecraft.util.NonNullList
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.energy.IEnergyStorage
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.ItemHandlerHelper
import szewek.flux.FTiles
import szewek.flux.container.FluxGenContainer
import szewek.flux.recipe.FluxGenRecipes.getCatalyst
import szewek.flux.recipe.FluxGenRecipes.getColdFluid
import szewek.flux.recipe.FluxGenRecipes.getHotFluid
import szewek.flux.recipe.FluxGenRecipes.isCatalyst
import szewek.flux.recipe.FluxGenRecipes.isColdFluid
import szewek.flux.recipe.FluxGenRecipes.isHotFluid

class FluxGenTile : LockableTileEntity(FTiles.FLUXGEN), IInventory, IItemHandler, IFluidHandler, ITickableTileEntity, INamedContainerProvider, IEnergyStorage {
    private val items = NonNullList.withSize(2, ItemStack.EMPTY)
    private val fluids = arrayOf(FluidStack.EMPTY, FluidStack.EMPTY)
    private var tickCount = 0
    private var energy = 0
    private var workTicks = 0
    private var maxWork = 0
    private var energyGen = 0
    private var workSpeed = 0
    private var isReady = false
    private var isDirty = false
    @JvmField
    var receivedRedstone = false
    private val fluxGenData: IIntArray = object : IIntArray {
        override fun get(i: Int): Int = when (i) {
            0 -> energy
            1 -> workTicks
            2 -> maxWork
            3 -> energyGen
            4 -> workSpeed
            else -> 0
        }

        override fun set(i: Int, v: Int) {
            when (i) {
                0 -> {
                    energy = v
                    workTicks = v
                    maxWork = v
                    energyGen = v
                    workSpeed = v
                }
                1 -> {
                    workTicks = v
                    maxWork = v
                    energyGen = v
                    workSpeed = v
                }
                2 -> {
                    maxWork = v
                    energyGen = v
                    workSpeed = v
                }
                3 -> {
                    energyGen = v
                    workSpeed = v
                }
                4 -> workSpeed = v
            }
        }

        override fun size() = 5
    }
    private val selfHandler = LazyOptional.of { this }
    override fun read(compound: CompoundNBT) {
        super.read(compound)
        energy = compound.getInt("E")
        if (energy >= maxEnergy) energy = maxEnergy
        workTicks = compound.getInt("WorkTicks")
        maxWork = compound.getInt("MaxWork")
        energyGen = compound.getInt("Gen")
        workSpeed = compound.getInt("WorkSpeed")
        ItemStackHelper.loadAllItems(compound, items)
    }

    override fun write(compound: CompoundNBT): CompoundNBT {
        super.write(compound)
        compound.putInt("E", energy)
        compound.putInt("WorkTicks", workTicks)
        compound.putInt("MaxWork", maxWork)
        compound.putInt("Gen", energyGen)
        compound.putInt("WorkSpeed", workSpeed)
        ItemStackHelper.saveAllItems(compound, items)
        return compound
    }

    override fun tick() {
        if (world == null || world!!.isRemote) return
        if (!isReady) {
            if (world!!.getRedstonePowerFromNeighbors(pos) > 0) receivedRedstone = true
            isReady = true
        }
        if (!receivedRedstone) {
            if (maxWork == 0 && ForgeHooks.getBurnTime(items[0]) > 0 || workTicks >= maxWork) {
                workTicks = 0
                maxWork = updateWork()
            } else if (energy + energyGen <= maxEnergy) {
                energy += energyGen
                workTicks += workSpeed
                if (maxWork <= workTicks) {
                    maxWork = 0
                    energyGen = 0
                }
            }
        }
        tickCount++
        if (tickCount > 3 && energy > 0) {
            tickCount = 0
            for (d in Direction.values()) {
                val bp = pos.offset(d, 1)
                val te = world!!.getTileEntity(bp)
                te?.getCapability(CapabilityEnergy.ENERGY, d.opposite)?.ifPresent { ie: IEnergyStorage -> sendEnergyTo(ie) }
            }
        }
        if (isDirty) markDirty()
    }

    private fun updateWork(): Int {
        val fuel = items[0]
        var f = ForgeHooks.getBurnTime(fuel)
        if (f == 0) return 0
        val catalyst = items[1]
        val genCat = getCatalyst(catalyst.item)
        val genHot = getHotFluid(fluids[0])
        val genCold = getColdFluid(fluids[1])
        energyGen = 40
        if (genCat.usage <= catalyst.count) {
            energyGen *= genCat.factor
            catalyst.grow(-genCat.usage)
        }
        if (genHot.usage <= fluids[0].amount) {
            f *= genHot.factor
            fluids[0].grow(-genHot.usage)
        }
        if (genCold.usage <= fluids[1].amount) {
            workSpeed = if (genCold.factor < genCat.factor) genCold.factor - genCat.factor else 1
            fluids[1].grow(-genCold.usage)
        } else {
            workSpeed = 1
        }
        fuel.grow(-1)
        isDirty = true
        return f
    }

    override fun <T> getCapability(cap: Capability<T>, dir: Direction?): LazyOptional<T>? {
        if (!removed) {
            if (cap === CapabilityEnergy.ENERGY || cap === CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return selfHandler.cast()
        }
        return super.getCapability(cap, dir)
    }

    override fun remove() {
        super.remove()
        selfHandler.invalidate()
    }

    override fun getEnergyStored() = energy
    override fun getMaxEnergyStored() = maxEnergy

    override fun canExtract() = true
    override fun canReceive() = false

    override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
        return 0
    }

    override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int {
        var r = maxExtract
        if (r > energy) r = energy
        if (!simulate) {
            energy -= r
            isDirty = true
        }
        return r
    }

    private fun sendEnergyTo(ie: IEnergyStorage) {
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

    override fun getSlots(): Int {
        return 2
    }

    override fun getSizeInventory(): Int {
        return 2
    }

    override fun isEmpty(): Boolean {
        return items[0].isEmpty && items[1].isEmpty
    }

    override fun getStackInSlot(i: Int): ItemStack {
        if (i < 0 || i >= items.size) throw RuntimeException("Getting slot " + i + " outside range [0," + items.size + ")")
        return items[i]
    }

    override fun decrStackSize(i: Int, count: Int): ItemStack {
        return if (i >= 0 && i <= items.size && count > 0 && !items[i].isEmpty) items[i].split(count) else ItemStack.EMPTY
    }

    override fun removeStackFromSlot(i: Int): ItemStack {
        if (i >= 0 && i <= items.size) {
            val stack = items[i]
            items[i] = ItemStack.EMPTY
            return stack
        }
        return ItemStack.EMPTY
    }

    override fun setInventorySlotContents(i: Int, stack: ItemStack) {
        if (i >= 0 && i <= items.size) {
            if (stack.count > 64) stack.count = 64
            items[i] = stack
        }
    }

    override fun getInventoryStackLimit(): Int {
        return 64
    }

    override fun isUsableByPlayer(player: PlayerEntity): Boolean {
        return player.world.getTileEntity(pos) === this && pos.distanceSq(player.positionVec, true) <= 64.0
    }

    override fun clear() {
        items.clear()
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (slot < 0 || slot >= items.size) throw RuntimeException("Getting slot " + slot + " outside range [0," + items.size + ")")
        if (stack.isEmpty) return ItemStack.EMPTY
        if (slot == 0 && ForgeHooks.getBurnTime(stack) == 0 || slot == 1 && !isCatalyst(stack.item)) {
            return stack
        }
        var l = stack.maxStackSize
        if (l > 64) l = 64
        val sc = stack.count
        val xis = items[slot]
        if (!xis.isEmpty) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, xis)) return stack
            l -= xis.count
        }
        if (0 >= l) return stack
        val rl = sc > l
        if (!simulate) {
            if (xis.isEmpty) items[slot] = if (rl) ItemHandlerHelper.copyStackWithSize(stack, l) else stack else xis.grow(if (rl) l else sc)
            isDirty = true
        }
        return if (rl) ItemHandlerHelper.copyStackWithSize(stack, sc - l) else ItemStack.EMPTY
    }

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        return ItemStack.EMPTY
    }

    override fun getSlotLimit(slot: Int) = 64

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
        return false
    }

    override fun getTanks() = 2

    override fun getFluidInTank(tank: Int): FluidStack = fluids[tank]

    override fun getTankCapacity(tank: Int) = fluidCap

    override fun isFluidValid(tank: Int, stack: FluidStack) = false

    override fun fill(resource: FluidStack, action: FluidAction): Int {
        if (resource.amount <= 0) return 0
        var s = -1
        if (isHotFluid(resource)) s = 0 else if (isColdFluid(resource)) s = 1
        if (s == -1 || !fluids[s].isFluidEqual(resource)) return 0
        var l = fluidCap - fluids[s].amount
        if (l > resource.amount) l = resource.amount
        if (l > 0 && action.execute()) {
            if (fluids[s].isEmpty) fluids[s] = resource.copy() else fluids[s].grow(l)
            isDirty = true
        }
        return l
    }

    override fun drain(resource: FluidStack, action: FluidAction): FluidStack {
        return FluidStack.EMPTY
    }

    override fun drain(maxDrain: Int, action: FluidAction): FluidStack {
        return FluidStack.EMPTY
    }

    override fun getDefaultName(): ITextComponent {
        return TranslationTextComponent("container.flux.fluxgen")
    }

    override fun createMenu(id: Int, playerInv: PlayerInventory): Container {
        return FluxGenContainer(id, playerInv, this, fluxGenData)
    }

    companion object {
        const val maxEnergy = 1000000
        const val sendAmount = 40000
        const val fluidCap = 4000
    }
}