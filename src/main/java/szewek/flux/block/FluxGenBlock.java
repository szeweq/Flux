package szewek.flux.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.wrapper.InvWrapper;
import szewek.flux.FTiles;
import szewek.flux.tile.FluxGenTile;

import javax.annotation.Nullable;

public class FluxGenBlock extends ContainerBlock {
	public FluxGenBlock(Properties props) {
		super(props);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return FTiles.FLUXGEN.create();
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return FTiles.FLUXGEN.create();
	}

	@Override
	public ActionResultType func_225533_a_(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rt) {
		if (world.isRemote) return ActionResultType.SUCCESS;
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof FluxGenTile) {
			FluidActionResult far = FluidUtil.tryEmptyContainerAndStow(player.getHeldItem(hand), (FluxGenTile) tile, new InvWrapper(player.inventory), FluxGenTile.fluidCap, player, true);
			if (far.success) player.setHeldItem(hand, far.result);
			else player.openContainer((FluxGenTile) tile);
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public void onBlockPlacedBy(World w, BlockPos pos, BlockState state, @Nullable LivingEntity ent, ItemStack stack) {
		if (w != null && !w.isRemote) updateRedstoneState(w, pos);
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader w, BlockPos pos, BlockPos neighbor) {
		if (w != null && !w.isRemote() && w instanceof World) updateRedstoneState((World) w, pos);
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	private void updateRedstoneState(World w, BlockPos pos) {
		FluxGenTile tfg = (FluxGenTile) w.getTileEntity(pos);
		if (tfg != null) {
			boolean b = tfg.receivedRedstone;
			boolean nb = w.getRedstonePowerFromNeighbors(pos) > 0;
			if (b != nb) tfg.receivedRedstone = nb;
		}
	}
}
