package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import szewek.flux.F;
import szewek.flux.tile.OnlineMarketTile;

import javax.annotation.Nullable;
import java.util.OptionalInt;

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

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
		if (!world.isRemote()) {
			TileEntity te = world.getTileEntity(pos);
			if (te != null && F.T.ONLINE_MARKET == te.getType()) {
				OptionalInt oint = player.openContainer((INamedContainerProvider) te);
				if (oint.isPresent()) {
					OnlineMarketTile omt = (OnlineMarketTile) te;
					player.openMerchantContainer(oint.getAsInt(), omt.getOffers(), 1, omt.getXp(), omt.func_213705_dZ(), omt.func_223340_ej());
				}
			}
		}
		return ActionResultType.SUCCESS;
	}
}
