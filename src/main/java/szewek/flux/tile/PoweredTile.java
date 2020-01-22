package szewek.flux.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.flux.energy.IEnergyReceiver;

import javax.annotation.Nullable;

public abstract class PoweredTile extends TileEntity implements IEnergyReceiver, ITickableTileEntity {
	protected int energy;
	private final LazyOptional<IEnergyStorage> handler = LazyOptional.of(() -> this);

	public PoweredTile(TileEntityType tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public void read(CompoundNBT compound) {
		super.read(compound);
		energy = MathHelper.clamp(compound.getInt("E"), 0, 500000);
	}

	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("E", energy);
		return compound;
	}

	public int getMaxEnergyStored() {
		return 500000;
	}

	public int getEnergyStored() {
		return energy;
	}

	public int receiveEnergy(int maxReceive, boolean simulate) {
		int r = maxReceive;
		if (maxReceive > 500000 - energy) {
			r = 500000 - energy;
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
