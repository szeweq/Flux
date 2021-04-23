package szewek.flux.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import szewek.fl.util.ItemsUtil;
import szewek.fl.util.spatial.NonStopWalker;
import szewek.fl.util.spatial.WalkAction;
import szewek.flux.F;
import szewek.flux.FluxCfg;
import szewek.flux.block.ActiveTileBlock;

import java.util.List;

public final class DiggerTile extends BlockInteractingTile {
	private boolean lastFlag;

	public DiggerTile() {
		super(F.T.DIGGER, new NonStopWalker(-5, -256, -5, 5, -1, 5), FluxCfg.ENERGY.digger);
		walker.startFrom(true, false, true);
		walker.putActions(WalkAction.X_POS, WalkAction.Z_POS, WalkAction.Y_NEG);
	}

	@Override
	public void tick() {
		assert level != null;
		if (!level.isClientSide) {
			boolean flag = !disabled;
			final int usage = energyUse.get();
			if (flag && energy.getEnergyStored() >= usage) {
				walker.walk();
				BlockPos bp = walker.getPosOffset(worldPosition);

				if (bp.getY() < 0) {
					disabled = true;
					level.setBlock(worldPosition, level.getBlockState(worldPosition).setValue(ActiveTileBlock.LIT, false), 3);
					setChanged();
					return;
				}

				dig(bp);
				energy.use(usage);
			}

			if (flag != lastFlag) {
				level.setBlock(worldPosition, level.getBlockState(worldPosition).setValue(ActiveTileBlock.LIT, flag), 3);
				lastFlag = flag;
			}
		}

	}

	private void dig(BlockPos bp) {
		BlockState bs = level.getBlockState(bp);
		Block b = bs.getBlock();
		if (F.Tags.DIGGER_SKIP.contains(b) || b.hasTileEntity(bs)) {
			return;
		}
		List<ItemStack> drops = bs.getDrops(new LootContext.Builder((ServerWorld)level).withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(worldPosition)).withParameter(LootParameters.TOOL, ItemStack.EMPTY));
		if (!drops.isEmpty()) {
			level.removeBlock(bp, false);
			ItemsUtil.trySendingItems(drops, level, worldPosition);
		}
	}
}
