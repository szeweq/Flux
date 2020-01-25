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
import szewek.flux.F;
import szewek.flux.tile.FluxGenTile;

public final class FluxGenBlock extends ContainerBlock {
	public FluxGenBlock(Properties props) {
		super(props);
	}

	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return F.T.FLUXGEN.create();
	}

	public TileEntity createTileEntity(BlockState var1, IBlockReader var2) {
		return F.T.FLUXGEN.create();
	}

	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rt) {
		if (!world.isRemote()) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof FluxGenTile) {
				FluidActionResult far = FluidUtil.tryEmptyContainerAndStow(player.getHeldItem(hand), (FluxGenTile) tile, new InvWrapper(player.inventory), 4000, player, true);
				if (far.success) player.setHeldItem(hand, far.getResult());
				else player.openContainer((FluxGenTile) tile);
			}
		}
		return ActionResultType.SUCCESS;
	}

	public void onBlockPlacedBy(World w, BlockPos pos, BlockState state, LivingEntity ent, ItemStack stack) {
		if (!w.isRemote()) this.updateRedstoneState(w, pos);
	}

	public void onNeighborChange(BlockState state, IWorldReader w, BlockPos pos, BlockPos neighbor) {
		if (!w.isRemote() && w instanceof World) this.updateRedstoneState((World) w, pos);
	}

	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	private void updateRedstoneState(World w, BlockPos pos) {
		FluxGenTile tfg = (FluxGenTile)w.getTileEntity(pos);
		if (tfg != null) {
			boolean nb = w.getRedstonePowerFromNeighbors(pos) > 0;
			if (tfg.receivedRedstone != nb) {
				tfg.receivedRedstone = nb;
			}
		}

	}

}
