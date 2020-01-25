package szewek.flux.tile;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import szewek.flux.F;
import szewek.flux.FluxCfg;
import szewek.flux.util.ItemsUtil;

import java.util.ArrayList;
import java.util.List;

public class ItemAbsorberTile extends EntityInteractingTile {

	public ItemAbsorberTile() {
		super(F.T.ITEM_ABSORBER);
	}

	@Override
	protected void interact() {
		final int usage = FluxCfg.COMMON.itemAbsorberEU.get();
		if (aabb != null && energy >= usage) {
			assert world != null;
			List<ItemEntity> itemDrops = world.getEntitiesWithinAABB(ItemEntity.class, aabb);
			List<ItemStack> list = new ArrayList<>();
			for (ItemEntity itemDrop : itemDrops) {
				ItemStack item = itemDrop.getItem();
				list.add(item);
				itemDrop.remove();
				energy -= usage;
				if (energy < usage) break;
			}
			ItemsUtil.trySendingItems(list, world, pos);
		}
	}
}
