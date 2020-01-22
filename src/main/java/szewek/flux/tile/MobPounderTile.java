package szewek.flux.tile;

import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.util.DamageSource;
import szewek.flux.F;
import szewek.flux.FluxConfig;

import java.util.List;

public class MobPounderTile extends EntityInteractingTile {

	public MobPounderTile() {
		super(F.Tiles.MOB_POUNDER);
	}

	@Override
	protected void interact() {
		final int usage = FluxConfig.COMMON.mobPounderEU.get();
		if (aabb != null && energy >= usage) {
			assert world != null;
			List<MonsterEntity> monsters = world.getEntitiesWithinAABB(MonsterEntity.class, aabb);
			for (MonsterEntity monster : monsters) {
				monster.attackEntityFrom(DamageSource.GENERIC, 100);
				energy -= usage;
				if (energy < usage) break;
			}
		}
	}
}
