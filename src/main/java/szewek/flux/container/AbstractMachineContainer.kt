package szewek.flux.container

import com.google.common.collect.Lists
import net.minecraft.client.util.RecipeBookCategories
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.IRecipeHelperPopulator
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.ContainerType
import net.minecraft.inventory.container.RecipeBookContainer
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.IRecipeType
import net.minecraft.item.crafting.RecipeItemHelper
import net.minecraft.util.IIntArray
import net.minecraft.util.IntArray
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import szewek.flux.recipe.AbstractMachineRecipe
import szewek.flux.tile.AbstractMachineTile
import szewek.flux.util.ServerRecipePlacerMachine

abstract class AbstractMachineContainer protected constructor(
        containerTypeIn: ContainerType<*>,
        val recipeType: IRecipeType<out AbstractMachineRecipe?>,
        id: Int,
        playerInventoryIn: PlayerInventory,
        private val inputSize: Int,
        private val outputSize: Int,
        machineInventoryIn: IInventory = Inventory(inputSize + outputSize),
        dataIn: IIntArray = IntArray(4)
) : RecipeBookContainer<IInventory>(containerTypeIn, id) {
    protected val machineInventory: IInventory
    private val data: IIntArray
    protected val world: World

    protected abstract fun initSlots(playerInventory: PlayerInventory)
    protected fun initPlayerSlotsAt(playerInventory: PlayerInventory, x: Int, y: Int) {
        var x = x
        var y = y
        val xBase = x
        for (i in 0..2) {
            for (j in 0..8) {
                addSlot(Slot(playerInventory, 9 * i + j + 9, x, y))
                x += 18
            }
            x = xBase
            y += 18
        }
        y += 4
        for (i in 0..8) {
            addSlot(Slot(playerInventory, i, x, y))
            x += 18
        }
    }

    override fun func_201771_a(helper: RecipeItemHelper) {
        if (machineInventory is IRecipeHelperPopulator) {
            (machineInventory as IRecipeHelperPopulator).fillStackedContents(helper)
        }
    }

    override fun clear() {
        machineInventory.clear()
    }

    override fun func_217056_a(placeAll: Boolean, recipe: IRecipe<*>?, player: ServerPlayerEntity) {
        ServerRecipePlacerMachine(this, inputSize, outputSize).place(player, recipe as IRecipe<IInventory>, placeAll)
    }

    override fun matches(recipeIn: IRecipe<in IInventory?>): Boolean {
        return recipeIn.getType() === recipeType && recipeIn.matches(machineInventory, world)
    }

    override fun getOutputSlot(): Int = inputSize

    override fun getWidth(): Int = inputSize

    override fun getHeight(): Int = 1

    @OnlyIn(Dist.CLIENT)
    override fun getSize(): Int = inputSize + 1

    override fun canInteractWith(playerIn: PlayerEntity): Boolean {
        return machineInventory.isUsableByPlayer(playerIn)
    }

    override fun transferStackInSlot(playerIn: PlayerEntity, index: Int): ItemStack {
        var stack = ItemStack.EMPTY
        val slot = inventorySlots[index]
        if (slot != null && slot.hasStack) {
            val slotStack = slot.stack
            stack = slotStack.copy()
            var s = inputSize + outputSize
            var e = s + 36
            if (index >= s) {
                e = s
                s = 0
            }
            if (!mergeItemStack(slotStack, s, e, false)) {
                return ItemStack.EMPTY
            }
            if (index >= inputSize && index < inputSize + outputSize) {
                slot.onSlotChange(slotStack, stack)
            }
            if (slotStack.isEmpty) slot.putStack(ItemStack.EMPTY) else slot.onSlotChanged()
            if (slotStack.count == stack.count) return ItemStack.EMPTY
            slot.onTake(playerIn, slotStack)
        }
        return stack
    }

    override fun getRecipeBookCategories(): List<RecipeBookCategories> {
        return Lists.newArrayList(RecipeBookCategories.SEARCH, RecipeBookCategories.EQUIPMENT, RecipeBookCategories.BUILDING_BLOCKS, RecipeBookCategories.MISC, RecipeBookCategories.REDSTONE)
    }

    @OnlyIn(Dist.CLIENT)
    fun processScaled(): Int {
        val i = data[1]
        val j = data[2]
        return if (j != 0 && i != 0) i * 24 / j else 0
    }

    @OnlyIn(Dist.CLIENT)
    fun energyScaled(): Int = data[0] * 54 / AbstractMachineTile.maxEnergy

    @OnlyIn(Dist.CLIENT)
    fun energyText(): String = data[0].toString() + " / " + AbstractMachineTile.maxEnergy + " F"

    init {
        Container.assertInventorySize(machineInventoryIn, inputSize + outputSize)
        Container.assertIntArraySize(dataIn, 4)
        machineInventory = machineInventoryIn
        data = dataIn
        world = playerInventoryIn.player.world
        initSlots(playerInventoryIn)
        trackIntArray(dataIn)
    }
}