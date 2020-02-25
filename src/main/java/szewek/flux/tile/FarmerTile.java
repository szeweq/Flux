package szewek.flux.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import szewek.fl.util.ItemsUtil;
import szewek.fl.util.SpatialWalker;
import szewek.fl.util.SpatialWalker.Action;
import szewek.flux.F;
import szewek.flux.FluxCfg;

import java.util.List;

public class FarmerTile extends BlockInteractingTile {

	public FarmerTile() {
		super(F.T.FARMER, new SpatialWalker(-5, 0, -5, 5, 0, 5) {
			@Override
			public boolean canWalk() {
				return true;
			}
		});
		walker.startFrom(true, true, true);
		walker.putActions(Action.X_POS, Action.Z_POS, Action.LOOP);
	}

	@Override
	public void tick() {
		assert world != null;
		final int usage = FluxCfg.COMMON.farmerEU.get();
		if (!world.isRemote() && energy >= usage) {
			walker.walk();
			BlockPos bp = walker.getPosOffset(pos);

			BlockState bs = world.getBlockState(bp);
			Block b = bs.getBlock();
			if (b != F.B.FARMER && b instanceof CropsBlock) {
				CropsBlock crop = (CropsBlock) b;
				if (crop.isMaxAge(bs)) {
					List<ItemStack> drops = bs.getDrops(new LootContext.Builder((ServerWorld)world).withParameter(LootParameters.POSITION, pos).withParameter(LootParameters.TOOL, ItemStack.EMPTY));
					if (!drops.isEmpty()) {
						world.setBlockState(bp, crop.withAge(0));
						ItemsUtil.trySendingItems(drops, world, pos);
					}
				}
			}
			energy -= usage;
		}
	}
}
