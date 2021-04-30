package szewek.flux.tile;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.DamageSource;
import szewek.flux.F;
import szewek.flux.FluxCfg;

import java.util.List;

public class MobPounderTile extends EntityInteractingTile {

	public MobPounderTile() {
		super(F.T.MOB_POUNDER, FluxCfg.ENERGY.mobPounder);
	}

	@Override
	protected void interact(int usage) {
		List<LivingEntity> mobs = level.getEntitiesOfClass(LivingEntity.class, aabb, m -> m instanceof IMob);
		for (LivingEntity mob : mobs) {
			if (!energy.use(usage)) break;
			mob.hurt(DamageSource.GENERIC, 100);
		}
	}
}
