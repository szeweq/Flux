package szewek.flux.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import szewek.flux.block.ActiveTileBlock;

import java.util.List;

public final class DiggerTile extends BlockInteractingTile {
	private boolean lastFlag;

	public DiggerTile() {
		super(F.T.DIGGER, new SpatialWalker(-5, -256, -5, 5, -1, 5) {
			@Override
			public boolean canWalk() {
				return true;
			}
		});
		walker.startFrom(true, false, true);
		walker.putActions(Action.X_POS, Action.Z_POS, Action.Y_NEG);
	}

	@Override
	public void tick() {
		assert world != null;
		if (!world.isRemote) {
			boolean flag = !disabled;
			final int usage = FluxCfg.COMMON.diggerEU.get();
			if (flag && energy >= usage) {
				walker.walk();
				BlockPos bp = walker.getPosOffset(pos);

				if (bp.getY() < 0) {
					disabled = true;
					world.setBlockState(pos, world.getBlockState(pos).with(ActiveTileBlock.LIT, false), 3);
					markDirty();
					return;
				}

				BlockState bs = world.getBlockState(bp);
				Block b = bs.getBlock();
				if (!F.Tags.DIGGER_SKIP.contains(b) && !b.hasTileEntity(bs)) {
					List<ItemStack> drops = bs.getDrops(new LootContext.Builder((ServerWorld)world).withParameter(LootParameters.POSITION, pos).withParameter(LootParameters.TOOL, ItemStack.EMPTY));
					if (!drops.isEmpty()) {
						world.removeBlock(bp, false);
						ItemsUtil.trySendingItems(drops, world, pos);
					}
				}
				energy -= usage;
			}

			if (flag != lastFlag) {
				world.setBlockState(pos, world.getBlockState(pos).with(ActiveTileBlock.LIT, flag), 3);
				lastFlag = flag;
			}
		}

	}


}
