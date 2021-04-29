package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.wrapper.InvWrapper;
import szewek.fl.network.FluxAnalytics;
import szewek.fl.network.NetCommon;
import szewek.flux.F;
import szewek.flux.tile.FluxGenTile;

import java.util.Objects;

public final class FluxGenBlock extends Block {
	public FluxGenBlock() {
		super(Block.Properties.of(Material.METAL).strength(1f).sound(SoundType.METAL));
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
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rt) {
		if (!world.isClientSide) {
			TileEntity tile = world.getBlockEntity(pos);
			if (tile instanceof FluxGenTile) {
				FluidActionResult far = FluidUtil.tryEmptyContainerAndStow(player.getItemInHand(hand), ((FluxGenTile) tile).getTank(), new InvWrapper(player.inventory), 4000, player, true);
				if (far.success) {
					player.setItemInHand(hand, far.getResult());
				} else {
					player.openMenu((FluxGenTile) tile);
				}
			}
		}
		NetCommon.putAction(player, "open", Objects.toString(getRegistryName(), "unknown"));
		return ActionResultType.SUCCESS;
	}

	@Override
	public void setPlacedBy(World w, BlockPos pos, BlockState state, LivingEntity ent, ItemStack stack) {
		if (!w.isClientSide) {
			updateRedstoneState(w, pos);
		}
		if (ent instanceof PlayerEntity) {
			NetCommon.putAction((PlayerEntity) ent, "place", Objects.toString(getRegistryName(), "unknown"));
		}

	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader w, BlockPos pos, BlockPos neighbor) {
		if (!w.isClientSide() && w instanceof World) {
			updateRedstoneState((World) w, pos);
		}
	}

	private void updateRedstoneState(World w, BlockPos pos) {
		FluxGenTile tfg = (FluxGenTile)w.getBlockEntity(pos);
		if (tfg != null) {
			tfg.setRedstoneState(w.getBestNeighborSignal(pos) > 0);
		}

	}

}
