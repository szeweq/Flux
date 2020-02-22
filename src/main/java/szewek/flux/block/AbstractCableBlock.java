package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SixWayBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import java.util.Map;

public abstract class AbstractCableBlock extends SixWayBlock {
	protected AbstractCableBlock(Properties properties) {
		super(0.25F, properties);
		this.setDefaultState(stateContainer.getBaseState()
				.with(SixWayBlock.NORTH, false)
				.with(SixWayBlock.EAST, false)
				.with(SixWayBlock.SOUTH, false)
				.with(SixWayBlock.WEST, false)
				.with(SixWayBlock.UP, false)
				.with(SixWayBlock.DOWN, false)
		);
	}

	protected abstract boolean checkTile(TileEntity te, Direction dir);

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return makeConnections(context.getWorld(), context.getPos());
	}

	private BlockState makeConnections(IBlockReader w, BlockPos pos) {
		BlockState bs = this.getDefaultState();
		boolean x;
		for(Map.Entry<Direction, BooleanProperty> e : FACING_TO_PROPERTY_MAP.entrySet()) {
			Direction dir = e.getKey();
			x = true;
			BlockPos bp = pos.offset(dir);
			Block b = w.getBlockState(pos).getBlock();
			if (b != this) {
				TileEntity te = w.getTileEntity(bp);
				if (te == null || !checkTile(te, dir.getOpposite()))
					x = false;
			}
			bs = bs.with(e.getValue(), x);
		}
		return bs;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		Block b = facingState.getBlock();
		boolean x = true;
		if (b != this) {
			TileEntity te = worldIn.getTileEntity(facingPos);
			if (te == null || !checkTile(te, facing.getOpposite())) {
				x = false;
			}
		}
		return stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), x);
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
	}

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
}
