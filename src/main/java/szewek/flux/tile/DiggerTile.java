package szewek.flux.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.Tags;
import szewek.flux.F;
import szewek.flux.FluxConfig;
import szewek.flux.block.ActiveTileBlock;
import szewek.flux.util.ItemsUtil;
import szewek.flux.util.savedata.Data;
import szewek.flux.util.savedata.SaveDataManager;

import java.util.List;

public final class DiggerTile extends PoweredTile {
	@Data("OffX") private int offsetX;
	@Data("OffY") private int offsetY;
	@Data("OffZ") private int offsetZ;
	@Data("Finished") private boolean finished;
	private boolean lastFlag;

	public DiggerTile() {
		super(F.Tiles.DIGGER);
	}

	public void tick() {
		assert world != null;
		if (!world.isRemote) {
			boolean flag = !finished;
			final int usage = FluxConfig.COMMON.diggerEU.get();
			if (flag && energy >= usage) {
				if (offsetY == 0 || offsetX == 5 && offsetZ == 5) {
					offsetX = -5;
					offsetZ = -5;
					offsetY += -1;
				} else if (offsetX == 5) {
					offsetX = -5;
					offsetZ++;
				} else offsetX++;

				BlockPos bp = pos.add(offsetX, offsetY, offsetZ);
				if (bp.getY() < 0) {
					finished = true;
					world.setBlockState(pos, world.getBlockState(pos).with(ActiveTileBlock.LIT, false), 3);
					markDirty();
					return;
				}

				BlockState bs = world.getBlockState(bp);
				Block b = bs.getBlock();
				if (!Tags.Blocks.DIRT.contains(b) && !Tags.Blocks.STONE.contains(b) && !Tags.Blocks.COBBLESTONE.contains(b)) {
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
