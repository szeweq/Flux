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
		if (level != null && !level.isClientSide) {
			serverTick(level);
			if (isDirty) {
				setChanged();
				isDirty = false;
			}
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!remove && cap == CapabilityEnergy.ENERGY) {
			return energy.lazyCast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public boolean canOpen(PlayerEntity player) {
		return player.level.getBlockEntity(worldPosition) == this && worldPosition.distSqr(player.position(), true) <= 64.0;
	}

	@Override
	public void setRemoved() {
		energy.invalidate();
		super.setRemoved();
	}
}
