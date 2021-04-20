package szewek.flux.tile;

import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraftforge.common.Tags;
import szewek.fl.util.ItemsUtil;
import szewek.fl.util.spatial.NonStopWalker;
import szewek.fl.util.spatial.WalkAction;
import szewek.flux.F;
import szewek.flux.FluxCfg;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class FarmerTile extends BlockInteractingTile {

	public FarmerTile() {
		super(F.T.FARMER, new NonStopWalker(5, 0, 5), FluxCfg.ENERGY.farmer);

		walker.startFrom(true, true, true);
		walker.putActions(WalkAction.X_POS, WalkAction.Z_POS, WalkAction.LOOP);
	}

	@Override
	public void tick() {
		assert level != null;
		final int usage = energyUse.get();
		if (!level.isClientSide && energy.getEnergyStored() >= usage) {
			walker.walk();
			BlockPos bp = walker.getPosOffset(worldPosition);
			BlockState bs = level.getBlockState(bp);
			Block b = bs.getBlock();
			if (b != F.B.FARMER) {
				checkBlockForHarvest(b, bs, bp);
				energy.use(usage);
			}
		}
	}

	private void checkBlockForHarvest(Block b, BlockState bs, BlockPos bp) {
		if (b instanceof CropsBlock) {
			CropsBlock crop = (CropsBlock) b;
			if (crop.isMaxAge(bs)) {
				tryHarvest(bs, bp, crop.getStateForAge(0));
			}
		} else if (b instanceof StemGrownBlock) {
			tryHarvest(bs, bp, null);
		} else if (b == Blocks.SUGAR_CANE || b == Blocks.CACTUS) {
			harvestPillar(b, bp, 3);
		} else if (b == Blocks.BAMBOO) {
			harvestPillar(b, bp, 16);
		} else if (b == Blocks.SEA_PICKLE && bs.getValue(SeaPickleBlock.PICKLES) > 1) {
			tryHarvest(bs, bp, bs.setValue(SeaPickleBlock.PICKLES, 1));
		} else if (b == Blocks.NETHER_WART) {
			tryHarvest(bs, bp, bs.setValue(BlockStateProperties.AGE_3, 1));
		} else if (b == Blocks.SWEET_BERRY_BUSH) {
			int n = bs.getValue(BlockStateProperties.AGE_3);
			if (n > 1) {
				level.setBlockAndUpdate(bp, bs.setValue(BlockStateProperties.AGE_3, 1));
				ItemsUtil.trySendingItems(Collections.singleton(new ItemStack(Items.SWEET_BERRIES, n)), level, worldPosition);
			}
		}
	}

	private void harvestPillar(Block b, BlockPos bp, int max) {
		int i;
		//noinspection StatementWithEmptyBody
		for (i = 0; i < max && level.getBlockState(bp.above(i + 1)).getBlock() == b; ++i);
		for (; i > 0; i--) {
			BlockPos pbp = bp.above(i);
			tryHarvest(level.getBlockState(pbp), pbp, null);
		}
	}

	private void tryHarvest(BlockState bs, BlockPos bp, @Nullable BlockState nbs) {
		List<ItemStack> drops = bs.getDrops(new LootContext.Builder((ServerWorld) level)
				.withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(bp))
				.withParameter(LootParameters.TOOL, ItemStack.EMPTY)
		);
		if (!drops.isEmpty()) {
			if (nbs == null) {
				level.removeBlock(bp, false);
			} else {
				level.setBlockAndUpdate(bp, nbs);
			}
			for (int i = 0; i < drops.size(); i++) {
				ItemStack stack = drops.get(i);
				if (Tags.Items.SEEDS.contains(stack.getItem())) {
					stack.grow(-1);
					if (stack.isEmpty()) {
						drops.remove(i);
					}
					break;
				}
			}
			ItemsUtil.trySendingItems(drops, level, worldPosition);
		}
	}
}
