package szewek.flux.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import szewek.flux.tile.AbstractMachineTile;

import javax.annotation.Nullable;

public final class MachineBlock extends ContainerBlock {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

	public MachineBlock() {
		super(Properties.create(Material.IRON).hardnessAndResistance(1.0F).sound(SoundType.METAL).lightValue(13));
		this.setDefaultState(stateContainer.getBaseState().with(FACING, Direction.NORTH).with(LIT, false));
	}

	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		TileEntityType<?> tetype = ForgeRegistries.TILE_ENTITIES.getValue(getRegistryName());
		return tetype != null ? tetype.create() : null;
	}

	public int getLightValue(BlockState state) {
		return state.get(LIT) ? super.getLightValue(state) : 0;
	}

	public ActionResultType func_225533_a_(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
		if (world.isRemote()) {
			return ActionResultType.SUCCESS;
		} else {
			TileEntity te = world.getTileEntity(pos);
			if (te != null) {
				TileEntityType<?> type = ForgeRegistries.TILE_ENTITIES.getValue(this.getRegistryName());
				if (type == te.getType()) {
					player.openContainer((INamedContainerProvider)te);
				}
			}

			return ActionResultType.SUCCESS;
		}
	}

	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		if (stack.hasDisplayName()) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof AbstractMachineTile) {
				((AbstractMachineTile)tileentity).setCustomName(stack.getDisplayName());
			}
		}

	}

	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof AbstractMachineTile) {
				InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory)tileentity);
				worldIn.updateComparatorOutputLevel(pos, this);
			}

			super.onReplaced(state, worldIn, pos, newState, isMoving);
		}

	}

	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		return Container.calcRedstone(worldIn.getTileEntity(pos));
	}

	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	public BlockState rotate(BlockState state, Rotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}

	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}
}