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
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;
import java.util.Map;

public class AbstractCableBlock extends SixWayBlock {

	protected AbstractCableBlock(Properties properties) {
		super(0.25F, properties);
		setDefaultState(stateContainer.getBaseState()
				.with(NORTH, false).with(EAST, false).with(SOUTH, false)
				.with(WEST, false).with(UP, false).with(DOWN, false)
		);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return makeConnections(context.getWorld(), context.getPos());
	}

	public BlockState makeConnections(IBlockReader w, BlockPos pos) {
		BlockState bs = getDefaultState();
		for (Map.Entry<Direction, BooleanProperty> entry : FACING_TO_PROPERTY_MAP.entrySet()) {
			Direction dir = entry.getKey();
			boolean x = true;
			BlockPos bp = pos.offset(dir);
			Block b = w.getBlockState(bp).getBlock();
			if (b != this) {
				TileEntity te = w.getTileEntity(bp);
				if (te == null || !te.getCapability(CapabilityEnergy.ENERGY, dir.getOpposite()).isPresent()) {
					x = false;
				}
			}
			bs = bs.with(entry.getValue(), x);
		}
		return bs;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		Block b = facingState.getBlock();
		boolean x = true;
		if (b != this) {
			TileEntity te = worldIn.getTileEntity(facingPos);
			if (te == null || !te.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).isPresent()) {
				x = false;
			}
		}
		return stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), x);
	}

	// func_225534_a_

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
	}

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
}
