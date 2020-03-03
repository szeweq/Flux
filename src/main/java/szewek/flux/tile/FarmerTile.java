package szewek.flux.tile;

import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import szewek.fl.util.ItemsUtil;
import szewek.fl.util.SpatialWalker;
import szewek.fl.util.SpatialWalker.Action;
import szewek.flux.F;
import szewek.flux.FluxCfg;

import javax.annotation.Nullable;
import java.util.Collections;
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
			if (b != F.B.FARMER) {
				if (b instanceof CropsBlock) {
					CropsBlock crop = (CropsBlock) b;
					if (crop.isMaxAge(bs)) {
						tryHarvest(bs, bp, crop.withAge(0));
					}
				} else if (b instanceof StemGrownBlock) {
					tryHarvest(bs, bp, null);
				} else if (b == Blocks.SUGAR_CANE || b == Blocks.CACTUS) {
					harvestPillar(b, bp, 3);
				} else if (b == Blocks.BAMBOO) {
					harvestPillar(b, bp, 16);
				} else if (b == Blocks.SEA_PICKLE && bs.get(SeaPickleBlock.PICKLES) > 1) {
					tryHarvest(bs, bp, bs.with(SeaPickleBlock.PICKLES, 1));
				} else if (b == Blocks.SWEET_BERRY_BUSH) {
					int n = bs.get(SweetBerryBushBlock.AGE);
					if (n > 1) {
						world.setBlockState(bp, bs.with(SweetBerryBushBlock.AGE, 1));
						ItemsUtil.trySendingItems(Collections.singleton(new ItemStack(Items.SWEET_BERRIES, n)), world, pos);
					}
				}
				energy -= usage;
			}
		}
	}

	private void harvestPillar(Block b, BlockPos bp, int max) {
		int i;
		//noinspection StatementWithEmptyBody
		for (i = 0; i < max && world.getBlockState(bp.up(i + 1)).getBlock() == b; ++i);
		for (; i > 0; i--) {
			BlockPos pbp = bp.up(i);
			tryHarvest(world.getBlockState(pbp), pbp, null);
		}
	}

	private void tryHarvest(BlockState bs, BlockPos bp, @Nullable BlockState nbs) {
		List<ItemStack> drops = bs.getDrops(new LootContext.Builder((ServerWorld) world).withParameter(LootParameters.POSITION, bp).withParameter(LootParameters.TOOL, ItemStack.EMPTY));
		if (!drops.isEmpty()) {
			if (nbs == null) {
				world.removeBlock(bp, false);
			} else {
				world.setBlockState(bp, nbs);
			}
			ItemsUtil.trySendingItems(drops, world, pos);
		}
	}
}
