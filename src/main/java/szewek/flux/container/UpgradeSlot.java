package szewek.flux.container;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import szewek.flux.item.ChipItem;
import szewek.flux.tile.AbstractMachineTile;

public class UpgradeSlot extends Slot {
	public UpgradeSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack.getItem() instanceof ChipItem;
	}

	@Override
	public void onSlotChanged() {
		if (inventory instanceof AbstractMachineTile) {
			((AbstractMachineTile) inventory).updateValues();
		}
		super.onSlotChanged();
	}
}
