package szewek.flux.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.flux.energy.EnergyCache;

import java.util.concurrent.atomic.AtomicInteger;

import static szewek.flux.tile.EnergyCableTile.Side;

public final class EnergyCableTile extends AbstractCableTile<IEnergyStorage, Side> {
	private final AtomicInteger energy = new AtomicInteger();
	private final EnergyCache energyCache = new EnergyCache(this);

	public EnergyCableTile(TileEntityType<EnergyCableTile> type) {
		super(type, CapabilityEnergy.ENERGY);
		for(int i = 0; i < 6; i++) {
			sides[i] = new Side(i, sideFlag, energy);
		}
	}

	@Override
	public void read(BlockState blockState, CompoundNBT compound) {
		super.read(blockState, compound);
		energy.set(MathHelper.clamp(compound.getInt("E"), 0, 50000));
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("E", energy.get());
		return compound;
	}

	@Override
	protected void updateSide(Direction dir) {
		try {
			IEnergyStorage ie = energyCache.getCached(dir);
			if (ie != null) {
				int r;
				int n = energy.get();
				if (ie instanceof Side) {
					r = (n - ie.getEnergyStored()) / 2;
					if (r != 0) {
						energy.addAndGet(-r);
						((Side) ie).syncEnergy(r);
					}
				} else if (ie.canReceive()) {
					r = 10000;
					if (r >= n) {
						r = n;
					}
					r = ie.receiveEnergy(r, true);
					if (r > 0) {
						energy.addAndGet(-r);
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

	@Override
	public void remove() {
		super.remove();
		energyCache.clear();
	}

	public static final class Side extends AbstractCableTile.AbstractSide<IEnergyStorage> implements IEnergyStorage {
		private final AtomicInteger energy;

		private Side(int i, AtomicInteger sf, AtomicInteger energy) {
			super(i, sf);
			this.energy = energy;
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			int r = maxReceive;
			if (r > 0) {
				int n = 50000 - energy.get();
				if (r > n) {
					r = n;
				}
				if (!simulate) {
					energy.addAndGet(r);
					update();
				}
			}
			return r;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			int r = maxExtract;
			if (r > 0) {
				int n = energy.get();
				if (r > n) {
					r = n;
				}
				if (!simulate) {
					energy.addAndGet(-r);
					update();
				}
			}
			return r;
		}

		private void syncEnergy(int diff) {
			energy.addAndGet(diff);
			update();
		}

		@Override
		public int getEnergyStored() {
			return energy.get();
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
