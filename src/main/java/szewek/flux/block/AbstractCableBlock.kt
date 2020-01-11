package szewek.flux.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.SixWayBlock
import net.minecraft.item.BlockItemUseContext
import net.minecraft.pathfinding.PathType
import net.minecraft.state.StateContainer
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorld
import net.minecraftforge.energy.CapabilityEnergy

open class AbstractCableBlock protected constructor(properties: Properties) : SixWayBlock(0.25f, properties) {
    override fun hasTileEntity(state: BlockState): Boolean = true

    override fun getStateForPlacement(context: BlockItemUseContext): BlockState? {
        return makeConnections(context.world, context.pos)
    }

    private fun makeConnections(w: IBlockReader, pos: BlockPos): BlockState {
        var bs = defaultState
        for ((dir, value) in FACING_TO_PROPERTY_MAP) {
            var x = true
            val bp = pos.offset(dir)
            val b = w.getBlockState(bp).block
            if (b !== this) {
                val te = w.getTileEntity(bp)
                if (te == null || !te.getCapability(CapabilityEnergy.ENERGY, dir.opposite).isPresent) {
                    x = false
                }
            }
            bs = bs.with(value, x)
        }
        return bs
    }

    override fun updatePostPlacement(stateIn: BlockState, facing: Direction, facingState: BlockState, worldIn: IWorld, currentPos: BlockPos, facingPos: BlockPos): BlockState {
        val b = facingState.block
        var x = true
        if (b !== this) {
            val te = worldIn.getTileEntity(facingPos)
            if (te == null || !te.getCapability(CapabilityEnergy.ENERGY, facing.opposite).isPresent) {
                x = false
            }
        }
        return stateIn.with(FACING_TO_PROPERTY_MAP[facing]!!, x)
    }

    override fun fillStateContainer(builder: StateContainer.Builder<Block, BlockState>) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN)
    }

    override fun allowsMovement(state: BlockState, worldIn: IBlockReader, pos: BlockPos, type: PathType): Boolean {
        return false
    }

    init {
        defaultState = stateContainer.baseState
                .with(NORTH, false).with(EAST, false).with(SOUTH, false)
                .with(WEST, false).with(UP, false).with(DOWN, false)
    }
}