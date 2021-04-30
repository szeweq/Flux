package szewek.flux.tile;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.DamageSource;
import szewek.flux.F;
import szewek.flux.FluxCfg;

import java.util.List;

public class ButcherTile extends EntityInteractingTile {

	public ButcherTile() {
		super(F.T.BUTCHER, FluxCfg.ENERGY.butcher);
	}

	@Override
	protected void interact(int usage) {
		List<AnimalEntity> animals = level.getEntitiesOfClass(AnimalEntity.class, aabb, e -> !e.isBaby());
		for (AnimalEntity animal : animals) {
			if (!energy.use(usage)) break;
			animal.hurt(DamageSource.GENERIC, 100);
		}
	}
}
