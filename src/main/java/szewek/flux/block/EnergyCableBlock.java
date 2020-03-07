package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.energy.CapabilityEnergy;
import szewek.flux.F;

public final class EnergyCableBlock extends AbstractCableBlock {

	public EnergyCableBlock() {
		super(Block.Properties.create(Material.IRON)
				.hardnessAndResistance(0.3f));
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
