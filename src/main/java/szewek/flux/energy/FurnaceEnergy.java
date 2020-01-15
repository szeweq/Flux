package szewek.flux.energy;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FurnaceEnergy extends EnergyCapable {
	private static final int CAP = 25000, USE = 20;
	private final AbstractFurnaceTileEntity furnace;

	public FurnaceEnergy(AbstractFurnaceTileEntity furnace) {
		this.furnace = furnace;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if (!canReceive() || maxReceive <= 0) return 0;
		World w = furnace.getWorld();
		if (w == null) return 0;
		IIntArray data = furnace.furnaceData;
		int burnTime = data.get(0) * USE;
		if (burnTime >= CAP) return 0;
		int r = CAP - burnTime;
		if (r > maxReceive) r = maxReceive - maxReceive % USE;
		if (!simulate && r / USE > 0) {
			int maxBurnTime = data.get(1) * USE;
			if (maxBurnTime < CAP) data.set(1, CAP / USE);
			data.set(0, (burnTime + r) / USE);
			if (burnTime == 0) {
				BlockPos pos = furnace.getPos();
				w.setBlockState(pos, w.getBlockState(pos).with(AbstractFurnaceBlock.LIT, true), 3);
			}
			furnace.markDirty();
		}
		return r;
	}

	@Override
	public int getEnergyStored() {
		return Math.min(furnace.furnaceData.get(0) * USE, CAP);
	}

	@Override
	public int getMaxEnergyStored() {
		return CAP;
	}

	@Override
	public boolean canReceive() {
		if (furnace.isRemoved()) return false;
		World w = furnace.getWorld();
		return w != null ?
			w.getRecipeManager()
					.getRecipe(furnace.recipeType, furnace, w)
					.map(recipe -> furnace.canSmelt(recipe))
					.orElse(false)
		: false;
	}
}
