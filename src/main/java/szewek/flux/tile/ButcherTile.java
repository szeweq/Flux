package szewek.flux.tile;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.DamageSource;
import szewek.flux.F;
import szewek.flux.FluxCfg;

import java.util.List;

public class ButcherTile extends EntityInteractingTile {

	public ButcherTile() {
		super(F.T.BUTCHER);
	}

	@Override
	protected void interact() {
		final int usage = FluxCfg.COMMON.butcherEU.get();
		if (aabb != null && energy >= usage) {
			assert world != null;
			List<AnimalEntity> animals = world.getEntitiesWithinAABB(AnimalEntity.class, aabb, e -> !e.isChild());
			for (AnimalEntity animal : animals) {
				animal.attackEntityFrom(DamageSource.GENERIC, 100);
				energy -= usage;
				if (energy < usage) {
					break;
				}
			}
		}
	}
}
