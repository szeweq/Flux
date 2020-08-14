package szewek.flux.tile;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.DamageSource;
import szewek.flux.F;
import szewek.flux.FluxCfg;

import java.util.List;

public class MobPounderTile extends EntityInteractingTile {

	public MobPounderTile() {
		super(F.T.MOB_POUNDER, FluxCfg.COMMON.mobPounderEU);
	}

	@Override
	protected void interact(int usage) {
		assert world != null;
		List<LivingEntity> mobs = world.getEntitiesWithinAABB(LivingEntity.class, aabb, m -> m instanceof IMob);
		for (LivingEntity mob : mobs) {
			mob.attackEntityFrom(DamageSource.GENERIC, 100);
			energy -= usage;
			if (energy < usage) {
				break;
			}
		}
	}
}
