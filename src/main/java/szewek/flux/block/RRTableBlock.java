package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import szewek.flux.F;

import javax.annotation.Nullable;

public class RRTableBlock extends Block {
	public RRTableBlock() {
		super(Properties.create(Material.IRON).hardnessAndResistance(1.5F).sound(SoundType.METAL));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return F.T.RR_TABLE.create();
	}
}
