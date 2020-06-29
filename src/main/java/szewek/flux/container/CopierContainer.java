package szewek.flux.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import szewek.flux.F;

public final class CopierContainer extends Machine2For1Container {

	public CopierContainer(int id, PlayerInventory playerInventoryIn, PacketBuffer data) {
		super(F.C.COPIER, F.R.COPYING, id, playerInventoryIn);
	}

	public CopierContainer(int id, PlayerInventory playerInventoryIn, IInventory machineInventoryIn, IIntArray dataIn) {
		super(F.C.COPIER, F.R.COPYING, id, playerInventoryIn, machineInventoryIn, dataIn);
	}
}
