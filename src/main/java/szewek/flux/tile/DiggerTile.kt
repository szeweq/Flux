package szewek.flux.tile

import net.minecraft.inventory.IInventory
import net.minecraft.inventory.InventoryHelper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameters
import net.minecraftforge.common.Tags
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.InvWrapper
import szewek.flux.FTiles
import szewek.flux.block.DiggerBlock
import szewek.ktutils.getNeighborTile

class DiggerTile : PoweredTile(FTiles.DIGGER) {
    private var offsetX = 0
    private var offsetY = 0
    private var offsetZ = 0
    private var finished = false
    private var lastFlag = false

    override fun read(compound: CompoundNBT) {
        super.read(compound)
        offsetX = compound.getInt("OffX")
        offsetY = compound.getInt("OffY")
        offsetZ = compound.getInt("OffZ")
        finished = compound.getBoolean("Finished")
    }

    override fun write(compound: CompoundNBT): CompoundNBT {
        super.write(compound)
        compound.putInt("OffX", offsetX)
        compound.putInt("OffY", offsetY)
        compound.putInt("OffZ", offsetZ)
        compound.putBoolean("Finished", finished)
        return compound
    }

    override fun tick() {
        if (!world!!.isRemote) {
            val flag = energy >= 200 && !finished
            if (flag) {
                if (offsetY == 0 || (offsetX == 5 && offsetZ == 5)) {
                    offsetX = -5
                    offsetZ = -5
                    offsetY--
                } else if (offsetX == 5) {
                    offsetX = -5
                    offsetZ++
                } else {
                    offsetX++
                }
                val bp = pos.add(offsetX, offsetY, offsetZ)
                if (bp.y < 0) {
                    finished = true
                    return
                }
                val bs = world!!.getBlockState(bp)
                if (bs.block !in Tags.Blocks.DIRT && bs.block !in Tags.Blocks.STONE && bs.block !in Tags.Blocks.COBBLESTONE) {
                    val drops = bs.getDrops(LootContext.Builder(world as ServerWorld).withParameter(LootParameters.POSITION, pos).withParameter(LootParameters.TOOL, ItemStack.EMPTY))
                    val inv = mutableListOf<IItemHandler>()
                    if (drops.isNotEmpty()) {
                        world!!.removeBlock(bp, false)
                        for (dir in Direction.values()) {
                            if (dir == Direction.DOWN) continue
                            val te = getNeighborTile(dir)
                            if (te != null) {
                                lateinit var iih: IItemHandler
                                val il = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.opposite)
                                if (il.isPresent) il.ifPresent {
                                    iih = it
                                } else if (te is IInventory) {
                                    iih = InvWrapper(te)
                                } else continue
                                inv += iih
                            }
                        }

                        itemloop@ for (id in drops) {
                            var stack = id
                            for (iih in inv) {
                                val l = iih.slots
                                for (i in 0 until l) {
                                    if (iih.isItemValid(i, stack)) {
                                        stack = iih.insertItem(i, stack, false)
                                    }
                                }
                                if (stack.isEmpty) break@itemloop
                            }
                            InventoryHelper.spawnItemStack(world!!, pos.x.toDouble(), (pos.y + 1).toDouble(), pos.z.toDouble(), stack)
                        }
                    }
                }
                energy -= 200
            }
            if (flag != lastFlag) {
                world!!.setBlockState(pos, world!!.getBlockState(pos).with(DiggerBlock.LIT, flag), 3)
                lastFlag = flag
            }
        }
    }
}