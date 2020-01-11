@file:JvmName("MenuFactory")
package szewek.flux.tile

import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.container.Container
import net.minecraft.util.IIntArray

typealias MenuFactory = (id: Int, player: PlayerInventory, inv: IInventory, data: IIntArray) -> Container
