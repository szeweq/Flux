package szewek.flux.block

import net.minecraft.block.BlockState
import net.minecraft.world.IBlockReader
import szewek.flux.tile.EnergyCableTile

class EnergyCableBlock(properties: Properties) : AbstractCableBlock(properties) {
    override fun createTileEntity(state: BlockState, world: IBlockReader) = EnergyCableTile()
}