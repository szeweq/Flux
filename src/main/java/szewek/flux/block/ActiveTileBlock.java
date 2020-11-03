package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import szewek.fl.network.FluxAnalytics;

public class ActiveTileBlock extends Block {
	public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

	public ActiveTileBlock() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(1f).sound(SoundType.METAL));
		setDefaultState(getStateContainer().getBaseState().with(LIT, false));
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
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (worldIn.isRemote) {
			FluxAnalytics.putView("flux/place/" + getRegistryName());
		}
	}
}
