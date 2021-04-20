package szewek.flux.tile;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import szewek.fl.util.ItemsUtil;
import szewek.flux.F;
import szewek.flux.FluxCfg;

import java.util.ArrayList;
import java.util.List;

public class ItemAbsorberTile extends EntityInteractingTile {

	public ItemAbsorberTile() {
		super(F.T.ITEM_ABSORBER, FluxCfg.ENERGY.itemAbsorber);
	}

	@Override
	protected void interact(int usage) {
		assert level != null;
		List<ItemEntity> itemDrops = level.getEntitiesOfClass(ItemEntity.class, aabb);
		List<ItemStack> list = new ArrayList<>();
		for (ItemEntity itemDrop : itemDrops) {
			if (!energy.use(usage)) break;
			ItemStack item = itemDrop.getItem();
			list.add(item);
			itemDrop.remove();
		}
		ItemsUtil.trySendingItems(list, level, worldPosition);
	}
}
