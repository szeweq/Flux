package szewek.flux.tile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.fl.energy.IEnergyReceiver;

import javax.annotation.Nullable;

public abstract class PoweredDeviceTile extends LockableTileEntity implements IEnergyReceiver, ITickableTileEntity {
	protected int energy, energyUse;
	protected boolean isDirty;

	private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> this);

	protected PoweredDeviceTile(TileEntityType<?> typeIn) {
		super(typeIn);
	}

	protected abstract void serverTick(World w);

	protected boolean useEnergy() {
		boolean b = energy >= energyUse;
		if (b) {
			energy -= energyUse;
		}
		return b;
	}

	@Override
	public void tick() {
		if (world != null && !world.isRemote) {
			serverTick(world);
			if (isDirty) {
				markDirty();
				isDirty = false;
			}
		}
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if (maxReceive <= 0) return 0;
		int r = Math.min(maxReceive, 1_000_000 - energy);
		if (!simulate) {
			energy += r;
			isDirty = true;
		}
		return r;
	}

	@Override
	public int getEnergyStored() {
		return energy;
	}

	@Override
	public int getMaxEnergyStored() {
		return 1_000_000;
	}

	@Nullable
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!removed && cap == CapabilityEnergy.ENERGY) {
			return energyHandler.cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return player.world.getTileEntity(pos) == this && pos.distanceSq(player.getPositionVec(), true) <= 64.0;
	}

	@Override
	public void remove() {
		energyHandler.invalidate();
		super.remove();
	}
}
