package szewek.flux.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.flux.energy.EnergyCache;

import javax.annotation.Nullable;

public final class EnergyCableTile extends AbstractCableTile {
	private int energy, cooldown;
	private final Side[] sides = new Side[6];
	private byte sideFlag;
	private final EnergyCache energyCache = new EnergyCache(this);

	public EnergyCableTile(TileEntityType<EnergyCableTile> type) {
		super(type);
		for(int i = 0; i < 6; i++) {
			sides[i] = new Side(i);
		}
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		energy = MathHelper.clamp(compound.getInt("E"), 0, 50000);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("E", energy);
		return compound;
	}

	@Override
	protected void updateSide(Direction dir) {
		try {
			IEnergyStorage ie = energyCache.getCached(dir);
			if (ie != null) {
				int r;
				if (ie instanceof Side) {
					r = (energy - ie.getEnergyStored()) / 2;
					if (r != 0) {
						energy -= r;
						((Side) ie).syncEnergy(r);
					}
				} else if (ie.canReceive()) {
					r = 10000;
					if (r >= energy) {
						r = energy;
					}
					r = ie.receiveEnergy(r, true);
					if (r > 0) {
						energy = energy - r;
						ie.receiveEnergy(r, false);
					}
				}
			}
		} catch (Exception ignored) {
			// Keep garbage "integrations" away from my precious Energy Cable!
			energyCache.clear();
			// A good mod developer ALWAYS invalidates LazyOptional instances!
		}
	}

	public LazyOptional<IEnergyStorage> getSide(Direction dir) {
		return sides[dir.getIndex()].lazy;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!removed && cap == CapabilityEnergy.ENERGY && side != null) {
			return sides[side.getIndex()].lazy.cast();
		} else {
			return super.getCapability(cap, side);
		}
	}

	@Override
	public void remove() {
		super.remove();
		energyCache.clear();
		for (Side s : sides) {
			s.lazy.invalidate();
		}
	}

	public final class Side implements IEnergyStorage, NonNullSupplier<IEnergyStorage> {
		private final byte bit;
		private final LazyOptional<IEnergyStorage> lazy = LazyOptional.of(this);

		private Side(int i) {
			bit = (byte) (1 << i);
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			int r = maxReceive;
			if (r > 0) {
				if (r > 50000 - energy) {
					r = 50000 - energy;
				}
				if (!simulate) {
					energy += r;
					sideFlag |= bit;
				}
			}
			return r;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			int r = maxExtract;
			if (r > 0) {
				if (r > energy) {
					r = energy;
				}
				if (!simulate) {
					energy -= r;
					sideFlag |= bit;
				}
			}
			return r;
		}

		private void syncEnergy(int diff) {
			energy += diff;
			sideFlag |= bit;
		}

		@Override
		public int getEnergyStored() {
			return energy;
		}

		@Override
		public int getMaxEnergyStored() {
			return 50000;
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return true;
		}

		@Override
		public IEnergyStorage get() {
			return this;
		}
	}
}
