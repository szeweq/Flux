package szewek.ktutils

import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import kotlin.internal.InlineOnly

@InlineOnly
inline fun TileEntity.getNeighborTile(dir: Direction) = world!!.getTileEntity(pos.offset(dir))