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
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

public final class EnergyCableTile extends TileEntity implements ITickableTileEntity {
	private int energy, cooldown = 0;
	private boolean isDirty = false;
	private final EnergyCableTile.Sided[] sides = new EnergyCableTile.Sided[6];
	private final EnumSet<Direction> sideCache = EnumSet.noneOf(Direction.class);
	private final EnumMap<Direction, LazyOptional<IEnergyStorage>> lazyEnergyCache = new EnumMap<>(Direction.class);

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
				if (isDirty) {
					markDirty();
					isDirty = false;
				}
			} else {
				cooldown = 4;
				Set<Direction> availableSides = EnumSet.complementOf(sideCache);
				sideCache.clear();
				for (Direction dir : availableSides) {
					IEnergyStorage ie = getCachedEnergy(dir);
					if (ie != null && ie.canReceive()) {
						int r = 10000;
						if (r >= energy) r = energy;
						r = ie.receiveEnergy(r, true);
						if (r > 0) {
							energy = energy - r;
							isDirty = true;
							ie.receiveEnergy(r, false);
						}
					}
				}
			}
		}
	}

	@Nullable
	private IEnergyStorage getCachedEnergy(Direction dir) {
		LazyOptional<IEnergyStorage> lazy = lazyEnergyCache.get(dir);
		if (lazy == null) {
			assert world != null;
			TileEntity te = world.getTileEntity(pos.offset(dir));
			if (te == null) return null;
			lazy = te.getCapability(CapabilityEnergy.ENERGY, dir.getOpposite());
			if (lazy.isPresent()) {
				lazy.addListener(l -> lazyEnergyCache.remove(dir));
				lazyEnergyCache.put(dir, lazy);
			}
		}
		return lazy.orElse(null);
	}

	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!removed && cap == CapabilityEnergy.ENERGY && side != null) {
			return sides[side.getIndex()].lazy.cast();
		} else return super.getCapability(cap, side);
	}

	public final class Sided implements IEnergyStorage {
		private final Direction dir;
		private LazyOptional<IEnergyStorage> lazy = LazyOptional.of(() -> this);

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
	}
}
