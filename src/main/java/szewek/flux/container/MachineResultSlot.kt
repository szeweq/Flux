package szewek.flux.container

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack
import szewek.flux.tile.AbstractMachineTile

class MachineResultSlot(private val player: PlayerEntity, inventoryIn: IInventory, slotIndex: Int, xPosition: Int, yPosition: Int) : Slot(inventoryIn, slotIndex, xPosition, yPosition) {
    private var removeCount = 0
    override fun isItemValid(stack: ItemStack): Boolean {
        return false
    }

    override fun decrStackSize(amount: Int): ItemStack {
        if (hasStack) {
            removeCount += Math.min(amount, stack.count)
        }
        return super.decrStackSize(amount)
    }

    override fun onTake(thePlayer: PlayerEntity, stack: ItemStack): ItemStack {
        onCrafting(stack)
        return super.onTake(thePlayer, stack)
    }

    override fun onCrafting(stack: ItemStack, amount: Int) {
        removeCount += amount
        onCrafting(stack)
    }

    override fun onCrafting(stack: ItemStack) {
        stack.onCrafting(player.world, player, removeCount)
        if (!player.world.isRemote && inventory is AbstractMachineTile) {
            inventory.updateRecipes(player)
        }
        removeCount = 0
    }

}