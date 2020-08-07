package szewek.flux.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

abstract class EntityInteractingTile extends PoweredTile {
	protected AxisAlignedBB aabb;
	protected int cooldown;

	public EntityInteractingTile(TileEntityType tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void read(BlockState blockState, CompoundNBT compound) {
		super.read(blockState, compound);
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
	public void setWorldAndPos(World w, BlockPos posIn) {
		super.setWorldAndPos(w, posIn);
		int x = posIn.getX(), y = posIn.getY(), z = posIn.getZ();
		aabb = new AxisAlignedBB(x-4, y-4, z-4, x+4, y+4, z+4);
	}

	@Override
	public void tick() {
		if (!world.isRemote) {
			if (cooldown > 0) {
				--cooldown;
			} else {
				cooldown = 20;
				interact();
			}
		}
	}

	protected abstract void interact();
}
