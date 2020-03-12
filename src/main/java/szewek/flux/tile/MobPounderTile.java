package szewek.flux.tile;

import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.util.DamageSource;
import szewek.flux.F;
import szewek.flux.FluxCfg;

import java.util.List;

public class MobPounderTile extends EntityInteractingTile {

	public MobPounderTile() {
		super(F.T.MOB_POUNDER);
	}

	@Override
	protected void interact() {
		final int usage = FluxCfg.COMMON.mobPounderEU.get();
		if (aabb != null && energy >= usage) {
			assert world != null;
			List<MonsterEntity> monsters = world.getEntitiesWithinAABB(MonsterEntity.class, aabb);
			for (MonsterEntity monster : monsters) {
				monster.attackEntityFrom(DamageSource.GENERIC, 100);
				energy -= usage;
				if (energy < usage) {
					break;
				}
			}
		}
	}
}
