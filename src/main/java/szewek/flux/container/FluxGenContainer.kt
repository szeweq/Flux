package szewek.flux.container

import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer
import net.minecraft.util.IIntArray
import net.minecraft.util.IntArray
import net.minecraftforge.common.ForgeHooks
import szewek.flux.FContainers
import szewek.flux.recipe.FluxGenRecipes.isCatalyst
import szewek.flux.tile.FluxGenTile

class FluxGenContainer(i: Int, pinv: PlayerInventory, private val fluxGenInv: IInventory, private val extraData: IIntArray) : Container(FContainers.FLUXGEN, i) {

    constructor(i: Int, pinv: PlayerInventory, data: PacketBuffer?) : this(i, pinv, Inventory(2), IntArray(5)) {}

    val workFill: Float
        get() {
            val maxWork = extraData[2]
            return if (maxWork == 0) 0F else extraData[1].toFloat() / maxWork.toFloat()
        }

    val energyFill: Float
        get() = extraData[0].toFloat() / FluxGenTile.maxEnergy.toFloat()

    fun energyText(): String {
        return extraData[0].toString() + " / " + FluxGenTile.maxEnergy + " F"
    }

    fun genText(): String {
        return I18n.format("flux.gen", extraData[3])
    }

    override fun canInteractWith(player: PlayerEntity): Boolean {
        return fluxGenInv.isUsableByPlayer(player)
    }

    override fun transferStackInSlot(p: PlayerEntity, index: Int): ItemStack {
        var nis = ItemStack.EMPTY
        val sl = inventorySlots[index]
        if (sl != null && sl.hasStack) {
            val bis = sl.stack
            nis = bis.copy()
            var s = 2
            var e = 38
            if (index > 1) {
                if (ForgeHooks.getBurnTime(bis) > 0) {
                    s = 0
                    e = 1
                } else if (isCatalyst(bis.item)) {
                    s = 1
                    e = 2
                } else if (index < 29) {
                    s = 29
                } else if (index < 38) {
                    e = 29
                }
            }
            if (!mergeItemStack(bis, s, e, false)) {
                return ItemStack.EMPTY
            }
            if (nis.isEmpty) {
                sl.putStack(ItemStack.EMPTY)
            } else {
                sl.onSlotChanged()
            }
            if (nis.count == bis.count) {
                return ItemStack.EMPTY
            }
            sl.onTake(p, nis)
        }
        return nis
    }

    init {
        addSlot(Slot(fluxGenInv, 0, 67, 35))
        addSlot(Slot(fluxGenInv, 1, 93, 35))
        var xBase: Int
        var yBase = 84
        for (y in 0..2) {
            xBase = 8
            for (x in 0..8) {
                addSlot(Slot(pinv, x + 9 * y + 9, xBase, yBase))
                xBase += 18
            }
            yBase += 18
        }
        xBase = 8
        yBase += 4
        for (w in 0..8) {
            addSlot(Slot(pinv, w, xBase, yBase))
            xBase += 18
        }
        trackIntArray(extraData)
    }
}