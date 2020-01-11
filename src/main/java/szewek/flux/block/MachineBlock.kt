package szewek.flux.block

import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.InventoryHelper
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.state.BooleanProperty
import net.minecraft.state.DirectionProperty
import net.minecraft.state.StateContainer
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraftforge.registries.ForgeRegistries
import szewek.flux.tile.AbstractMachineTile

class MachineBlock : ContainerBlock(Properties.create(Material.IRON).hardnessAndResistance(1f).sound(SoundType.METAL).lightValue(13)) {
    override fun createNewTileEntity(worldIn: IBlockReader): TileEntity? {
        return ForgeRegistries.TILE_ENTITIES.getValue(registryName)?.create()
    }

    @Suppress("DEPRECATION")
    override fun getLightValue(state: BlockState): Int {
        return if (state[LIT]) super.getLightValue(state) else 0
    }

    override fun func_225533_a_(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, rayTrace: BlockRayTraceResult): ActionResultType {
        if (world.isRemote) return ActionResultType.SUCCESS
        val te = world.getTileEntity(pos)
        if (te != null) {
            val type: TileEntityType<*>? = ForgeRegistries.TILE_ENTITIES.getValue(registryName)
            if (type === te.type) {
                player.openContainer(te as INamedContainerProvider?)
            }
        }
        return ActionResultType.SUCCESS
    }

    override fun getStateForPlacement(context: BlockItemUseContext): BlockState? {
        return defaultState.with(FACING, context.placementHorizontalFacing.opposite)
    }

    override fun onBlockPlacedBy(worldIn: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        if (stack.hasDisplayName()) {
            val tileentity = worldIn.getTileEntity(pos)
            if (tileentity is AbstractMachineTile) {
                tileentity.customName = stack.displayName
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onReplaced(state: BlockState, worldIn: World, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (state.block !== newState.block) {
            val tileentity = worldIn.getTileEntity(pos)
            if (tileentity is AbstractMachineTile) {
                InventoryHelper.dropInventoryItems(worldIn, pos, tileentity as IInventory)
                worldIn.updateComparatorOutputLevel(pos, this)
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving)
        }
    }

    override fun hasComparatorInputOverride(state: BlockState) = true

    override fun getComparatorInputOverride(blockState: BlockState, worldIn: World, pos: BlockPos): Int {
        return Container.calcRedstone(worldIn.getTileEntity(pos))
    }

    override fun getRenderType(state: BlockState) = BlockRenderType.MODEL

    override fun rotate(state: BlockState, rot: Rotation): BlockState {
        return state.with(FACING, rot.rotate(state[FACING]))
    }

    override fun mirror(state: BlockState, mirrorIn: Mirror): BlockState {
        return state.rotate(mirrorIn.toRotation(state[FACING]))
    }

    override fun fillStateContainer(builder: StateContainer.Builder<Block, BlockState>) {
        builder.add(FACING, LIT)
    }

    companion object {
        val FACING: DirectionProperty = HorizontalBlock.HORIZONTAL_FACING
        val LIT: BooleanProperty = RedstoneTorchBlock.LIT
    }

    init {
        defaultState = stateContainer.baseState.with(FACING, Direction.NORTH).with(LIT, false)
    }
}