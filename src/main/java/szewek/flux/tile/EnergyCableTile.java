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

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

public final class EnergyCableTile extends TileEntity implements ITickableTileEntity {
	private int energy;
	private boolean isDirty;
	private final EnergyCableTile.Sided[] sides = new EnergyCableTile.Sided[6];
	private static final int maxEnergy = 50000;

	public EnergyCableTile(TileEntityType<EnergyCableTile> type) {
		super(type);
		int i = 0;

		for(byte var2 = 5; i <= var2; ++i) {
			sides[i] = new EnergyCableTile.Sided();
		}

	}

	public void read(CompoundNBT compound) {
		super.read(compound);
		energy = compound.getInt("E");
		if (energy >= 50000) {
			energy = 50000;
		}

	}

	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("E", energy);
		return compound;
	}

	public void tick() {
		assert world != null;
		if (!world.isRemote()) {
			Set<Direction> availableSides = EnumSet.allOf(Direction.class);
			for (Direction dir : Direction.values()) {
				Sided side = sides[dir.getIndex()];
				if (side.extracted || side.received) {
					availableSides.remove(dir);
					side.received = side.extracted = false;
				}
			}

			for (Direction side : availableSides) {
				TileEntity te = world.getTileEntity(pos.offset(side));
				if (te != null) {
					te.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).ifPresent(ie -> {
						if (ie.canReceive()) {
							int r = 5000;
							if (r >= energy) {
								r = energy;
							}

							r = ie.receiveEnergy(r, true);
							if (r > 0) {
								energy = energy - r;
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

	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!removed && cap == CapabilityEnergy.ENERGY && side != null) {
			return sides[side.getIndex()].lazy.cast();
		} else return super.getCapability(cap, side);
	}



	public final class Sided implements IEnergyStorage {
		private boolean extracted;
		private boolean received;
		private LazyOptional<IEnergyStorage> lazy = LazyOptional.of(() -> this);

		public int receiveEnergy(int maxReceive, boolean simulate) {
			int r = maxReceive;
			if (maxReceive > maxEnergy - energy) {
				r = maxEnergy - energy;
			}
			if (!simulate) {
				energy += r;
				received = true;
			}
			return r;
		}

		public int extractEnergy(int maxExtract, boolean simulate) {
			int r = maxExtract;
			if (maxExtract > energy) {
				r = energy;
			}
			if (!simulate) {
				energy -= r;
				extracted = true;
			}

			return r;
		}

		public int getEnergyStored() {
			return energy;
		}

		public int getMaxEnergyStored() {
			return 50000;
		}

		public boolean canExtract() {
			return true;
		}

		public boolean canReceive() {
			return true;
		}
	}
}
