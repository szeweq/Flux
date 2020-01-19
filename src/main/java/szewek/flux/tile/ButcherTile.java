package szewek.flux.tile;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import szewek.flux.F;
import szewek.flux.FluxConfig;

import java.util.List;

public class ButcherTile extends PoweredTile {
	private AxisAlignedBB aabb = null;
	private int cooldown = 0;

	public ButcherTile() {
		super(F.Tiles.BUTCHER);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		aabb = new AxisAlignedBB(x-4, y-4, z-4, x+4, y+4, z+4);
	}

	@Override
	public void tick() {
		assert world != null;
		if (!world.isRemote) {
			if (cooldown > 0) --cooldown;
			else {
				cooldown = 20;
				final int usage = FluxConfig.COMMON.butcherEU.get();
				if (aabb != null && energy >= usage) {
					List<AnimalEntity> animals = world.getEntitiesWithinAABB(AnimalEntity.class, aabb, e -> !e.isChild());
					for (AnimalEntity animal : animals) {
						animal.attackEntityFrom(DamageSource.GENERIC, 100);
						energy -= usage;
						if (energy < usage) break;
					}
				}
			}
		}
	}

	@Override
	public void setPos(BlockPos posIn) {
		super.setPos(posIn);
		int x = posIn.getX(), y = posIn.getY(), z = posIn.getZ();
		aabb = new AxisAlignedBB(x-4, y-4, z-4, x+4, y+4, z+4);
	}
}
