package szewek.flux.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.RedstoneTorchBlock
import net.minecraft.state.BooleanProperty
import net.minecraft.state.StateContainer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.IBlockReader
import szewek.flux.tile.DiggerTile

class DiggerBlock(properties: Properties) : Block(properties) {

    init {
        defaultState = stateContainer.baseState.with(LIT, false)
    }

    override fun hasTileEntity(state: BlockState?): Boolean = true

    override fun createTileEntity(state: BlockState?, world: IBlockReader?): TileEntity = DiggerTile()

    override fun fillStateContainer(builder: StateContainer.Builder<Block, BlockState>) {
        builder.add(LIT)
    }

    companion object {
        val LIT: BooleanProperty = RedstoneTorchBlock.LIT
    }
}