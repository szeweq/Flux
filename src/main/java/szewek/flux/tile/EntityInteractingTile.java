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

	@Override
	public void fromTag(BlockState blockState, CompoundNBT compound) {
		super.fromTag(blockState, compound);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		aabb = new AxisAlignedBB(x-4, y-4, z-4, x+4, y+4, z+4);
	}


	@Override
	public void setPos(BlockPos posIn) {
		super.setPos(posIn);
		int x = posIn.getX(), y = posIn.getY(), z = posIn.getZ();
		aabb = new AxisAlignedBB(x-4, y-4, z-4, x+4, y+4, z+4);
	}



	@Override
	public void setLocation(World w, BlockPos posIn) {
		super.setLocation(w, posIn);
		int x = posIn.getX(), y = posIn.getY(), z = posIn.getZ();
		aabb = new AxisAlignedBB(x-4, y-4, z-4, x+4, y+4, z+4);
	}

	@Override
	public void tick() {
		if (world == null || world.isRemote) {
			return;
		}
		if (cooldown > 0) {
			--cooldown;
		} else if (aabb != null) {
			cooldown = 20;
			int usage = energyUse.get();
			if (energy >= usage) {
				interact(usage);
			}
		}
	}

	protected abstract void interact(int usage);
}
