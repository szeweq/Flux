package szewek.flux.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import szewek.flux.F;
import szewek.flux.util.ItemsUtil;
import szewek.flux.util.savedata.Data;
import szewek.flux.util.savedata.SaveDataManager;

import java.util.List;

public class FarmerTile extends PoweredTile {
	@Data("OffX") private int offsetX;
	@Data("OffZ") private int offsetZ;

	public FarmerTile() {
		super(F.Tiles.FARMER);
		offsetX = offsetZ = -5;
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		SaveDataManager.read(this, compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		SaveDataManager.write(this, compound);
		return compound;
	}

	@Override
	public void tick() {
		assert world != null;
		if (!world.isRemote() && energy >= 100) {
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
			if (b != F.Blocks.FARMER) {
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
			energy -= 100;
		}
	}
}