package szewek.flux.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;

abstract class EntityInteractingTile extends PoweredTile {
	protected AxisAlignedBB aabb;
	protected int cooldown;

	public EntityInteractingTile(TileEntityType tileEntityTypeIn, ForgeConfigSpec.IntValue energyUse) {
		super(tileEntityTypeIn, energyUse);
	}

	private void updateAABB(BlockPos bp) {
		int x = bp.getX(), y = bp.getY(), z = bp.getZ();
		aabb = new AxisAlignedBB(x-4, y-4, z-4, x+4, y+4, z+4);
	}

	@Override
	public void load(BlockState blockState, CompoundNBT compound) {
		super.load(blockState, compound);
		updateAABB(worldPosition);
	}


	@Override
	public void setPosition(BlockPos posIn) {
		super.setPosition(posIn);
		updateAABB(posIn);
	}

	@Override
	public void setLevelAndPosition(World w, BlockPos posIn) {
		super.setLevelAndPosition(w, posIn);
		updateAABB(posIn);
	}

	@Override
	public void tick() {
		if (level == null || level.isClientSide) {
			return;
		}
		if (cooldown > 0) {
			--cooldown;
		} else if (aabb != null) {
			cooldown = 20;
			int usage = energyUse.get();
			if (energy.getEnergyStored() >= usage) {
				interact(usage);
			}
		}
	}

	protected abstract void interact(int usage);
}
