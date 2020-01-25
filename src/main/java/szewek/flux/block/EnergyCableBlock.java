package szewek.flux.block;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.energy.CapabilityEnergy;
import szewek.flux.F;

public final class EnergyCableBlock extends AbstractCableBlock {

	public EnergyCableBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean checkTile(TileEntity te, Direction dir) {
		return te.getCapability(CapabilityEnergy.ENERGY, dir).isPresent();
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return F.T.ENERGY_CABLE.create();
	}
}
