package szewek.flux.block;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import szewek.flux.tile.EnergyCableTile;

import javax.annotation.Nullable;

public class EnergyCableBlock extends AbstractCableBlock {
	public EnergyCableBlock(Properties properties) {
		super(properties);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new EnergyCableTile();
	}
}
