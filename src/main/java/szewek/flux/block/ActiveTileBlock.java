package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import szewek.fl.network.NetCommon;

import java.util.Objects;

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

	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (placer instanceof PlayerEntity) {
			NetCommon.putAction((PlayerEntity) placer, "place", Objects.toString(getRegistryName(), "unknown"));
		}
	}
}
