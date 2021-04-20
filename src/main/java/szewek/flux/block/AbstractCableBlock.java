package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.material.Material;
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
	protected AbstractCableBlock() {
		super(0.25F, Block.Properties.of(Material.METAL)
				.strength(0.3f));
		registerDefaultState(stateDefinition.any()
				.setValue(SixWayBlock.NORTH, false)
				.setValue(SixWayBlock.EAST, false)
				.setValue(SixWayBlock.SOUTH, false)
				.setValue(SixWayBlock.WEST, false)
				.setValue(SixWayBlock.UP, false)
				.setValue(SixWayBlock.DOWN, false)
		);
	}

	protected abstract boolean checkTile(TileEntity te, Direction dir);

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return makeConnections(context.getLevel(), context.getClickedPos());
	}

	private BlockState makeConnections(IBlockReader w, BlockPos pos) {
		BlockState bs = defaultBlockState();
		boolean x;
		for(Map.Entry<Direction, BooleanProperty> e : PROPERTY_BY_DIRECTION.entrySet()) {
			Direction dir = e.getKey();
			x = true;
			BlockPos bp = pos.relative(dir);
			Block b = w.getBlockState(pos).getBlock();
			if (b != this) {
				TileEntity te = w.getBlockEntity(bp);
				if (te == null || !checkTile(te, dir.getOpposite())) {
					x = false;
				}
			}
			bs = bs.setValue(e.getValue(), x);
		}
		return bs;
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		Block b = facingState.getBlock();
		boolean x = true;
		if (b != this) {
			TileEntity te = worldIn.getBlockEntity(facingPos);
			if (te == null || !checkTile(te, facing.getOpposite())) {
				x = false;
			}
		}
		return stateIn.setValue(PROPERTY_BY_DIRECTION.get(facing), x);
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
}
