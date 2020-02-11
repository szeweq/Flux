package szewek.flux.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import szewek.flux.F;
import szewek.flux.FluxCfg;
import szewek.fl.util.ItemsUtil;

import java.util.List;

public class FarmerTile extends BlockInteractingTile {

	public FarmerTile() {
		super(F.T.FARMER);
		offsetX = offsetZ = -5;
	}

	@Override
	public void tick() {
		assert world != null;
		final int usage = FluxCfg.COMMON.farmerEU.get();
		if (!world.isRemote() && energy >= usage) {
			if (offsetX == 5 && offsetZ == 5) {
				offsetX = -5;
				offsetZ = -5;
			} else if (offsetX == 5) {
				offsetX = -5;
				offsetZ++;
			} else offsetX++;

			BlockPos bp = pos.add(offsetX, 0, offsetZ);
			BlockState bs = world.getBlockState(bp);
			Block b = bs.getBlock();
			if (b != F.B.FARMER) {
				if (b instanceof CropsBlock) {
					CropsBlock crop = (CropsBlock) b;
					if (crop.isMaxAge(bs)) {
						List<ItemStack> drops = bs.getDrops(new LootContext.Builder((ServerWorld)world).withParameter(LootParameters.POSITION, pos).withParameter(LootParameters.TOOL, ItemStack.EMPTY));
						if (!drops.isEmpty()) {
							world.setBlockState(bp, crop.withAge(0));
							ItemsUtil.trySendingItems(drops, world, pos);
						}
					}
				}
			}
			energy -= usage;
		}
	}
}
