package szewek.flux.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import szewek.flux.F;
import szewek.flux.tile.InteractorRailTile;

import java.util.List;
import java.util.Random;

public class InteractorRailBlock extends AbstractRailBlock {
	public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

	public InteractorRailBlock() {
		super(true, Block.Properties.create(Material.MISCELLANEOUS).doesNotBlockMovement().hardnessAndResistance(0.7F).sound(SoundType.METAL));
		setDefaultState(stateContainer.getBaseState().with(SHAPE, RailShape.NORTH_SOUTH).with(POWERED, false).with(TRIGGERED, false));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return F.T.INTERACTOR_RAIL.create();
	}

	private AxisAlignedBB getDectectionBox(BlockPos pos) {
		final float f = 0.2F;
		return new AxisAlignedBB((float)pos.getX() + f, pos.getY(), (float)pos.getZ() + f, (float)(pos.getX() + 1) - f, (float)(pos.getY() + 1) - f, (float)(pos.getZ() + 1) - f);
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (!worldIn.isRemote) {
			updateTile(worldIn, pos, state);
		}
	}

	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		updateTile(worldIn, pos, state);
	}

	private void updateTile(World world, BlockPos pos, BlockState state) {
		boolean trigger = state.get(TRIGGERED);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof InteractorRailTile) {
			InteractorRailTile irTile = (InteractorRailTile) tile;
			List<AbstractMinecartEntity> minecarts = world.getEntitiesWithinAABB(AbstractMinecartEntity.class, getDectectionBox(pos), null);
			irTile.setMinecarts(minecarts);
			boolean newTrigger = !minecarts.isEmpty();

			if (trigger != newTrigger) {
				world.setBlockState(pos, state.with(TRIGGERED, newTrigger));
			}

			if (newTrigger) {
				world.getPendingBlockTicks().scheduleTick(pos, this, 10);
			}
		} else {
			world.setBlockState(pos, state.with(TRIGGERED, false));
		}
	}

	@Override
	protected void updateState(BlockState state, World worldIn, BlockPos pos, Block blockIn) {
		boolean flag = state.get(POWERED);
		boolean flag1 = worldIn.isBlockPowered(pos);// || findPoweredRailSignal(worldIn, pos, state, true, 0) || findPoweredRailSignal(worldIn, pos, state, false, 0);
		if (flag1 != flag) {
			BlockState newState = state.with(POWERED, flag1);
			worldIn.setBlockState(pos, newState, 3);
			RailState railstate = new RailState(worldIn, pos, state);
			for(BlockPos blockpos : railstate.getConnectedRails()) {
				BlockState blockstate = worldIn.getBlockState(blockpos);
				blockstate.neighborChanged(worldIn, blockpos, blockstate.getBlock(), pos, false);
			}
			worldIn.notifyNeighborsOfStateChange(pos, this);
			worldIn.notifyNeighborsOfStateChange(pos.down(), this);
			if (state.get(SHAPE).isAscending()) {
				worldIn.notifyNeighborsOfStateChange(pos.up(), this);
			}
			worldIn.markBlockRangeForRenderUpdate(pos, state, newState);
		}

	}

	@Override
	public Property<RailShape> getShapeProperty() {
		return SHAPE;
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return true;
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return 0;
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		if (blockAccess.getBlockState(pos.offset(side.getOpposite())).getBlock() == this) {
			return 0;
		}
		return blockState.get(POWERED) ? 15 : 0;
	}

	@Override
	public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rot) {
		return Blocks.DETECTOR_RAIL.rotate(state, rot);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return Blocks.DETECTOR_RAIL.mirror(state, mirrorIn);
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(SHAPE, POWERED, TRIGGERED);
	}
}
