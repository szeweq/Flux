package szewek.flux.tile;

import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import szewek.flux.F;

import java.util.List;

public class MobPounderTile extends PoweredTile {
	private AxisAlignedBB aabb;
	private int cooldown = 0;

	public MobPounderTile() {
		super(F.Tiles.MOB_POUNDER);
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
				if (aabb != null && energy >= 1000) {
					List<MonsterEntity> monsters = world.getEntitiesWithinAABB(MonsterEntity.class, aabb);
					for (MonsterEntity monster : monsters) {
						monster.attackEntityFrom(DamageSource.GENERIC, 100);
						energy -= 1000;
						if (energy < 1000) break;
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
