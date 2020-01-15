package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;

public class AbstractActiveTileBlock extends Block {
	public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

	public AbstractActiveTileBlock(Properties properties) {
		super(properties);
		setDefaultState(getStateContainer().getBaseState().with(LIT, false));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}
}
