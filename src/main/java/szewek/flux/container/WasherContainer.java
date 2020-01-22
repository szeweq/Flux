package szewek.flux.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import szewek.flux.F;

public class WasherContainer extends Machine2For1Container {
	public WasherContainer(int id, PlayerInventory playerInventoryIn, PacketBuffer data) {
		super(F.Containers.WASHER, F.Recipes.WASHING, id, playerInventoryIn, 2, 1);
	}

	public WasherContainer(int id, PlayerInventory playerInventoryIn, IInventory machineInventoryIn, IIntArray dataIn) {
		super(F.Containers.WASHER, F.Recipes.WASHING, id, playerInventoryIn, 2, 1, machineInventoryIn, dataIn);
	}
}