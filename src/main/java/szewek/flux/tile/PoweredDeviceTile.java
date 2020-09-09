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
import szewek.flux.tile.part.MachineEnergy;

import javax.annotation.Nullable;

public abstract class PoweredDeviceTile extends LockableTileEntity implements ITickableTileEntity {
	protected MachineEnergy energy = new MachineEnergy(1_000_000);
	protected int energyUse;
	protected boolean isDirty;

	protected PoweredDeviceTile(TileEntityType<?> typeIn) {
		super(typeIn);
	}

	protected abstract void serverTick(World w);

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

	@Nullable
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!removed && cap == CapabilityEnergy.ENERGY) {
			return energy.lazyCast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return player.world.getTileEntity(pos) == this && pos.distanceSq(player.getPositionVec(), true) <= 64.0;
	}

	@Override
	public void remove() {
		energy.invalidate();
		super.remove();
	}
}
