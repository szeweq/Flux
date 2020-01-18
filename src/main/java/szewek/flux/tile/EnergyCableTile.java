package szewek.flux.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.flux.energy.EnergyCache;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

public final class EnergyCableTile extends TileEntity implements ITickableTileEntity {
	private int energy, cooldown = 0;
	private final EnergyCableTile.Sided[] sides = new EnergyCableTile.Sided[6];
	private final EnumSet<Direction> sideCache = EnumSet.noneOf(Direction.class);
	private final EnergyCache energyCache = new EnergyCache();

	public EnergyCableTile(TileEntityType<EnergyCableTile> type) {
		super(type);
		Direction[] dirs = Direction.values();
		for(int i = 0; i < 6; i++) {
			sides[i] = new EnergyCableTile.Sided(dirs[i]);
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
			if (cooldown > 0) {
				--cooldown;
			} else {
				cooldown = 4;
				Set<Direction> availableSides = EnumSet.complementOf(sideCache);
				sideCache.clear();
				for (Direction dir : availableSides) {
					IEnergyStorage ie = energyCache.getCached(dir, world, pos);
					if (ie != null) {
						int r;
						if (ie instanceof Sided) {
							r = ie.getEnergyStored();
							if (r < energy) {
								r = (r - energy) / 2;
								if (r > 0) {
									energy -= r;
									ie.receiveEnergy(r, false);
								}
							}
						} else if (ie.canReceive()) {
							r = 10000;
							if (r >= energy) r = energy;
							r = ie.receiveEnergy(r, true);
							if (r > 0) {
								energy = energy - r;
								ie.receiveEnergy(r, false);
							}
						}
					}
				}
			}
		}
	}

	public LazyOptional<IEnergyStorage> getLazySide(Direction dir) {
		return sides[dir.getIndex()].lazy;
	}

	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!removed && cap == CapabilityEnergy.ENERGY && side != null) {
			return sides[side.getIndex()].lazy.cast();
		} else return super.getCapability(cap, side);
	}

	@Override
	public void remove() {
		super.remove();
		for (Sided s : sides) {
			s.lazy.invalidate();
		}
	}

	public final class Sided implements IEnergyStorage, NonNullSupplier<IEnergyStorage> {
		private final Direction dir;
		private LazyOptional<IEnergyStorage> lazy = LazyOptional.of(this);

		private Sided(Direction d) {
			dir = d;
		}

		public int receiveEnergy(int maxReceive, boolean simulate) {
			int r = maxReceive;
			if (maxReceive > 50000 - energy) {
				r = 50000 - energy;
			}
			if (!simulate) {
				energy += r;
				sideCache.add(dir);
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
				sideCache.add(dir);
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

		@Override
		public IEnergyStorage get() {
			return this;
		}
	}
}
