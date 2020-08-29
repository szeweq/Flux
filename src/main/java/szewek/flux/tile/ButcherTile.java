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
