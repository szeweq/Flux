package szewek.flux.block;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;
import szewek.fl.signal.SignalCapability;
import szewek.flux.F;

public final class SignalCableBlock extends AbstractCableBlock {
	@Override
	protected boolean checkTile(TileEntity te, Direction dir) {
		return te.getCapability(SignalCapability.SIGNAL_CAP, dir).isPresent();
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return F.T.SIGNAL_CABLE.create();
	}
}
