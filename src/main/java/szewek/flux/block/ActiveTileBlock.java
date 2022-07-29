package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ForgeRegistries;

public class ActiveTileBlock extends Block {
	public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

	public ActiveTileBlock() {
		super(Block.Properties.of(Material.METAL).strength(1f).sound(SoundType.METAL));
		registerDefaultState(stateDefinition.any().setValue(LIT, false));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		TileEntityType<?> tetype = ForgeRegistries.TILE_ENTITIES.getValue(getRegistryName());
		return tetype == null ? null : tetype.create();
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}
}
