package szewek.flux.tile

import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.entity.item.ExperienceOrbEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IRecipeHelperPopulator
import net.minecraft.inventory.IRecipeHolder
import net.minecraft.inventory.ISidedInventory
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.inventory.container.Container
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.IRecipeType
import net.minecraft.item.crafting.RecipeItemHelper
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.LockableTileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.Direction
import net.minecraft.util.IIntArray
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.energy.IEnergyStorage
import szewek.flux.block.MachineBlock
import szewek.flux.energy.IEnergyReceiver
import szewek.flux.recipe.AbstractMachineRecipe
import szewek.flux.util.IInventoryIO
import szewek.ktutils.*
import java.util.*
import java.util.stream.Collectors

abstract class AbstractMachineTile protected constructor(
        typeIn: TileEntityType<*>,
        @JvmField private val recipeType: IRecipeType<out AbstractMachineRecipe>,
        @JvmField private val menuFactory: MenuFactory,
        @JvmField private val inputSize: Int,
        @JvmField private val outputSize: Int
) : LockableTileEntity(typeIn), IEnergyReceiver, ISidedInventory, IInventoryIO, IRecipeHolder, IRecipeHelperPopulator, ITickableTileEntity {
    @JvmField protected var energy = 0
    @JvmField protected var process = 0
    @JvmField protected var processTotal = 0
    @JvmField protected var energyUse = 40
    @JvmField protected var isDirty = false
    @JvmField protected var items: NonNullList<ItemStack> = NonNullList.withSize(inputSize + outputSize, ItemStack.EMPTY)
    private val recipesCount: Object2IntMap<ResourceLocation> = Object2IntOpenHashMap()
    @JvmField protected val machineData: IIntArray = object : IIntArray {
        override fun get(index: Int): Int = when (index) {
            0 -> energy
            1 -> process
            2 -> processTotal
            3 -> energyUse
            else -> 0
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                0 -> energy = value
                1 -> process = value
                2 -> processTotal = value
                3 -> energyUse = value
            }
        }

        override fun size() = 4
    }

    override fun read(compound: CompoundNBT) {
        super.read(compound)
        items = NonNullList.withSize(inputSize + outputSize, ItemStack.EMPTY)
        ItemStackHelper.loadAllItems(compound, items)
        energy = compound.getInt("E")
        if (energy >= maxEnergy) energy = maxEnergy
        process = compound.getInt("Process")
        processTotal = compound.getInt("Total")
        val i = compound.getShort("RSize").toInt()
        for (j in 0 until i) {
            val location = ResourceLocation(compound.getString("RLoc$j"))
            val c = compound.getInt("RCount$j")
            recipesCount[location] = c
        }
    }

    override fun write(compound: CompoundNBT): CompoundNBT {
        super.write(compound)
        compound.putInt("E", energy)
        compound.putInt("Process", process)
        compound.putInt("Total", processTotal)
        ItemStackHelper.saveAllItems(compound, items)
        compound.putShort("RSize", recipesCount.size.toShort())
        var i = 0
        for ((key, value) in recipesCount.object2IntEntrySet()) {
            compound.putString("RLoc$i", key.toString())
            compound.putInt("RCount$i", value!!)
            ++i
        }
        return compound
    }

    private val isPowered: Boolean
        get() = energy >= energyUse

    override fun tick() {
        val workState = isWorking
        if (world != null && !world!!.isRemote) {
            var inputEmpty = true
            for (inputStack in inputs) {
                if (!inputStack.isEmpty) {
                    inputEmpty = false
                    break
                }
            }
            if (isPowered && !inputEmpty) {
                val recipe = world!!.recipeManager.getRecipe(recipeType, this, world!!).orElse(null)
                if (recipe != null && canProcess(recipe)) {
                    energy -= energyUse
                    if (process++ == processTotal) {
                        process = 0
                        processTotal = recipe.processTime
                        produceResult(recipe)
                        isDirty = true
                    }
                } else {
                    process = 0
                }
            }
        }
        if (workState != isWorking) {
            world!!.setBlockState(pos, world!!.getBlockState(pos).with(MachineBlock.LIT, isWorking), 3)
        }
        if (isDirty) {
            markDirty()
            isDirty = false
        }
    }

    private val isWorking: Boolean
        get() = process > 0

    protected fun canProcess(recipe: AbstractMachineRecipe?): Boolean {
        if (recipe != null) {
            val result = recipe.recipeOutput
            for (outputStack in outputs) {
                if (outputStack.isEmpty) return true
                if (!outputStack.isItemEqual(result)) return false
                val minStackSize = 64.coerceAtMost(outputStack.maxStackSize).coerceAtMost(result.maxStackSize)
                if (outputStack.count + result.count <= minStackSize) return true
            }
        }
        return false
    }

    protected fun produceResult(recipe: AbstractMachineRecipe?) {
        if (recipe != null && canProcess(recipe)) {
            val result = recipe.recipeOutput
            val outputs = outputs
            for (i in outputs.indices) {
                val outputStack = outputs[i]
                if (outputStack.isEmpty) {
                    val copyResult = result.copy()
                    outputs[i] = copyResult
                    break
                } else if (outputStack.item === result.item) {
                    outputStack += result.count
                    break
                }
            }
            val inputs = inputs
            for (i in inputs.indices) {
                inputs[i] -= recipe.getCostAt(i)
            }
            recipeUsed = recipe
        }
    }

    private val energyHandler = LazyOptional.of<IEnergyStorage> { this }
    override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T>? {
        return if (!removed && cap === CapabilityEnergy.ENERGY) energyHandler.cast() else super.getCapability(cap, side)
    }

    override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
        var r = maxReceive
        if (r > maxEnergy - energy) r = maxEnergy - energy
        if (!simulate) {
            energy += r
            isDirty = true
        }
        return r
    }

    override fun getEnergyStored() = energy

    override fun getMaxEnergyStored() = maxEnergy

    override fun canReceive(): Boolean = true

    override fun getInputs() = items.subList(0, inputSize)
    override fun getOutputs(): MutableList<ItemStack> {
        val size = items.size
        return items.subList(size - outputSize, size)
    }

    override fun getSizeInventory(): Int = items.size

    override fun canInsertItem(index: Int, itemStackIn: ItemStack, direction: Direction?): Boolean {
        return isItemValidForSlot(index, itemStackIn)
    }

    override fun canExtractItem(index: Int, stack: ItemStack, direction: Direction): Boolean {
        return true
    }

    override fun isEmpty(): Boolean {
        for (item in items) {
            if (!item.isEmpty) {
                return false
            }
        }
        return true
    }

    override fun getStackInSlot(index: Int): ItemStack {
        return items[index]
    }

    override fun decrStackSize(index: Int, count: Int): ItemStack {
        return ItemStackHelper.getAndSplit(items, index, count)
    }

    override fun removeStackFromSlot(index: Int): ItemStack {
        return ItemStackHelper.getAndRemove(items, index)
    }

    override fun setInventorySlotContents(index: Int, stack: ItemStack) {
        val inputStack = items[index]
        val same = !stack.isEmpty && stack.isItemEqual(inputStack) && ItemStack.areItemStackTagsEqual(stack, inputStack)
        items[index] = stack
        if (stack.count > 64) stack.count = 64
        if (index < inputSize && !same) {
            processTotal = world!!.recipeManager.getRecipe(recipeType, this, world!!).map { obj: AbstractMachineRecipe -> obj.processTime }.orElse(200)
            process = 0
            markDirty()
        }
    }

    override fun isUsableByPlayer(player: PlayerEntity): Boolean {
        return player.world.getTileEntity(pos) === this && pos.distanceSq(player.positionVec, true) <= 64.0
    }

    override fun isItemValidForSlot(index: Int, stack: ItemStack): Boolean {
        return index < inputSize
    }

    override fun clear() {
        items.clear()
    }

    override fun setRecipeUsed(recipe: IRecipe<*>?) {
        if (recipe != null) {
            recipesCount.compute(recipe.id) { _, num ->
                1 + (num?: 0)
            }
        }
    }

    override fun getRecipeUsed(): IRecipe<*>? = null

    override fun onCrafting(player: PlayerEntity) {}
    override fun fillStackedContents(helper: RecipeItemHelper) {
        for (item in items) {
            helper.accountStack(item)
        }
    }

    fun updateRecipes(player: PlayerEntity) {
        val recipeManager = player.world.recipeManager
        val recipes = recipesCount.object2IntEntrySet().stream()
                .map { e: Object2IntMap.Entry<ResourceLocation> ->
                    val recipe = recipeManager.getRecipe(e.key).orElse(null)
                    if (recipe != null) {
                        collectExperience(player, e.intValue, (recipe as AbstractMachineRecipe).experience)
                    }
                    recipe
                }
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        player.unlockRecipes(recipes)
        recipesCount.clear()
    }

    override fun createMenu(id: Int, player: PlayerInventory): Container {
        return menuFactory(id, player, this, machineData)
    }

    override fun remove() {
        super.remove()
        energyHandler.invalidate()
    }

    companion object {
        const val maxEnergy = 1000000
        private fun collectExperience(player: PlayerEntity, c: Int, xp: Float) {
            var count = c
            if (xp == 0f) {
                count = 0
            } else if (xp < 1) {
                val f = count.toFloat() * xp
                var i = MathHelper.floor(f)
                if (i < MathHelper.ceil(f) && Math.random() < (f - i.toFloat()).toDouble()) {
                    ++i
                }
                count = i
            }
            while (count > 0) {
                val x = ExperienceOrbEntity.getXPSplit(count)
                count -= x
                player.world.addEntity(ExperienceOrbEntity(
                        player.world, player.posX, player.posY + 0.5, player.posZ + 0.5, x
                ))
            }
        }
    }
}