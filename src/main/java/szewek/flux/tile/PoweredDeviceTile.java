package szewek.flux.tile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import szewek.flux.tile.part.MachineEnergy;

public abstract class PoweredDeviceTile extends LockableTileEntity implements ITickableTileEntity {
	protected MachineEnergy energy = new MachineEnergy(1_000_000);
	protected int energyUse;
	protected boolean isDirty;

	protected PoweredDeviceTile(TileEntityType<?> typeIn) {
		super(typeIn);
	}

	protected abstract void serverTick(World w);

	public static void tick(World w, BlockPos bp, BlockState state, PoweredDeviceTile it) {
		if (!w.isClientSide) {
			it.serverTick(w);
			if (it.isDirty) {
				it.setChanged();
				it.isDirty = false;
			}
		}
	}

	// Provide compatibility with MC 1.17
	@Override
	public void tick() {
		tick(level, worldPosition, getBlockState(), this);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
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
