package szewek.flux.block

import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.ContainerBlock
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.items.wrapper.InvWrapper
import szewek.flux.FTiles
import szewek.flux.tile.FluxGenTile

class FluxGenBlock(props: Properties) : ContainerBlock(props) {
    override fun hasTileEntity(state: BlockState) = true

    override fun createNewTileEntity(worldIn: IBlockReader) = FTiles.FLUXGEN.create()

    override fun createTileEntity(state: BlockState, world: IBlockReader) = FTiles.FLUXGEN.create()

    override fun func_225533_a_(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, rt: BlockRayTraceResult): ActionResultType {
        if (world.isRemote) return ActionResultType.SUCCESS
        val tile = world.getTileEntity(pos)
        if (tile is FluxGenTile) {
            val far = FluidUtil.tryEmptyContainerAndStow(player.getHeldItem(hand), tile as FluxGenTile?, InvWrapper(player.inventory), FluxGenTile.fluidCap, player, true)
            if (far.success) player.setHeldItem(hand, far.result) else player.openContainer(tile as FluxGenTile?)
        }
        return ActionResultType.SUCCESS
    }

    override fun onBlockPlacedBy(w: World, pos: BlockPos, state: BlockState, ent: LivingEntity?, stack: ItemStack) {
        if (!w.isRemote) updateRedstoneState(w, pos)
    }

    override fun onNeighborChange(state: BlockState, w: IWorldReader, pos: BlockPos, neighbor: BlockPos) {
        if (!w.isRemote && w is World) updateRedstoneState(w, pos)
    }

    override fun getRenderType(p_149645_1_: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    private fun updateRedstoneState(w: World, pos: BlockPos) {
        val tfg = w.getTileEntity(pos) as FluxGenTile?
        if (tfg != null) {
            val b = tfg.receivedRedstone
            val nb = w.getRedstonePowerFromNeighbors(pos) > 0
            if (b != nb) tfg.receivedRedstone = nb
        }
    }
}