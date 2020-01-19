package szewek.flux.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.flux.energy.IEnergyReceiver;
import szewek.flux.util.savedata.Data;
import szewek.flux.util.savedata.SaveDataManager;

import javax.annotation.Nullable;

public abstract class PoweredTile extends TileEntity implements IEnergyReceiver, ITickableTileEntity {
	protected final int maxEnergy = 500000;
	@Data("E") protected int energy;
	private final LazyOptional<IEnergyStorage> handler = LazyOptional.of(() -> this);

	public PoweredTile(TileEntityType tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public void read(CompoundNBT compound) {
		super.read(compound);
		SaveDataManager.read(this, compound);
	}

	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		SaveDataManager.write(this, compound);
		if (energy < 0) energy = 0;
		if (energy > maxEnergy) energy = maxEnergy;
		return compound;
	}

	public int getMaxEnergyStored() {
		return maxEnergy;
	}

	public int getEnergyStored() {
		return energy;
	}

	public int receiveEnergy(int maxReceive, boolean simulate) {
		int r = maxReceive;
		if (maxReceive > maxEnergy - energy) {
			r = maxEnergy - energy;
		}
		if (!simulate) {
			energy += r;
		}
		return r;
	}

	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!removed && CapabilityEnergy.ENERGY == cap) {
			return handler.cast();
		} else return super.getCapability(cap, side);
	}


}
