package szewek.flux.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface IInventoryIO extends IInventory {
	List<ItemStack> getInputs();
	List<ItemStack> getOutputs();
}
