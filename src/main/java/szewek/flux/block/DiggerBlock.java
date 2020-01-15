package szewek.flux.block;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import szewek.flux.tile.DiggerTile;

public final class DiggerBlock extends AbstractActiveTileBlock {
	public DiggerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new DiggerTile();
	}
}
