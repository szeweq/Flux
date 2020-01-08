package szewek.flux.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.flux.FTiles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

public class EnergyCableTile extends TileEntity implements ITickableTileEntity {
	private static final int maxEnergy = 50000, sendAmount = 5000;
	private int energy = 0;
	private boolean isDirty = false;
	private Sided[] sides = new Sided[6];

	public EnergyCableTile() {
		super(FTiles.ENERGY_CABLE);
		for (int i = 0; i < 6; i++) {
			sides[i] = new Sided();
		}
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		energy = compound.getInt("E");
		if (energy >= maxEnergy) energy = maxEnergy;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("E", energy);
		return compound;
	}

	@Override
	public void tick() {
		if (!world.isRemote) {
			Set<Direction> availableSides = EnumSet.allOf(Direction.class);
			for (Direction dir : Direction.values()) {
				Sided side = sides[dir.getIndex()];
				if (side.extracted || side.received) {
					availableSides.remove(dir);
					side.extracted = side.received = false;
				}
			}
			for (Direction dir : availableSides) {
				TileEntity te = world.getTileEntity(pos.offset(dir));
				if (te != null) {
					te.getCapability(CapabilityEnergy.ENERGY, dir.getOpposite()).ifPresent(ie -> {
						if (ie.canReceive()) {
							int r = sendAmount;
							if (r >= energy) r = energy;
							r = ie.receiveEnergy(r, true);
							if (r > 0) {
								energy -= r;
								isDirty = true;
								ie.receiveEnergy(r, false);
							}
						}
					});
				}
			}
		}
		if (isDirty) {
			markDirty();
			isDirty = false;
		}
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if (!removed) {
			if (cap == CapabilityEnergy.ENERGY && side != null) {
				return sides[side.getIndex()].lazy.cast();
			}
		}
		return super.getCapability(cap, side);
	}

	public final class Sided implements IEnergyStorage {
		private boolean extracted = false, received = false;

		LazyOptional<IEnergyStorage> lazy = LazyOptional.of(() -> this);

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			int r = maxReceive;
			if (r > maxEnergy - energy) r = maxEnergy - energy;
			if (!simulate) {
				energy += r;
				received = true;
			}
			return r;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			int r = maxExtract;
			if (r > energy) r = energy;
			if (!simulate) {
				energy -= r;
				extracted = true;
			}
			return r;
		}

		@Override
		public int getEnergyStored() {
			return energy;
		}

		@Override
		public int getMaxEnergyStored() {
			return maxEnergy;
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
	}
}
