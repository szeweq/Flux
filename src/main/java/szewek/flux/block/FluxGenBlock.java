package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
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
import szewek.fl.network.FluxAnalytics;
import szewek.flux.F;
import szewek.flux.tile.FluxGenTile;

public final class FluxGenBlock extends Block {
	public FluxGenBlock() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(1f).sound(SoundType.METAL));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState var1, IBlockReader var2) {
		return F.T.FLUXGEN.create();
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rt) {
		if (!world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof FluxGenTile) {
				FluidActionResult far = FluidUtil.tryEmptyContainerAndStow(player.getHeldItem(hand), ((FluxGenTile) tile).getTank(), new InvWrapper(player.inventory), 4000, player, true);
				if (far.success) {
					player.setHeldItem(hand, far.getResult());
				} else {
					player.openContainer((FluxGenTile) tile);
				}
			}
		} else {
			FluxAnalytics.putView("flux/open/" + getRegistryName());
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public void onBlockPlacedBy(World w, BlockPos pos, BlockState state, LivingEntity ent, ItemStack stack) {
		if (!w.isRemote) {
			updateRedstoneState(w, pos);
		} else {
			FluxAnalytics.putView("flux/place/" + getRegistryName());
		}
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader w, BlockPos pos, BlockPos neighbor) {
		if (!w.isRemote() && w instanceof World) {
			updateRedstoneState((World) w, pos);
		}
	}

	private void updateRedstoneState(World w, BlockPos pos) {
		FluxGenTile tfg = (FluxGenTile)w.getTileEntity(pos);
		if (tfg != null) {
			tfg.setRedstoneState(w.getRedstonePowerFromNeighbors(pos) > 0);
		}

	}

}
