package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import szewek.flux.F;

import javax.annotation.Nullable;

public class OnlineMarketBlock extends Block {
	public OnlineMarketBlock() {
		super(Properties.create(Material.IRON).hardnessAndResistance(2.0F).sound(SoundType.METAL).lightValue(13));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return F.T.ONLINE_MARKET.create();
	}
}
