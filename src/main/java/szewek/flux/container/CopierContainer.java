package szewek.flux.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import szewek.flux.F;

public final class CopierContainer extends AbstractMachineContainer {
	public CopierContainer(int id, PlayerInventory playerInventoryIn, PacketBuffer data) {
		super(F.C.COPIER, F.R.COPYING, id, playerInventoryIn, 2, 1);
	}

	public CopierContainer(int id, PlayerInventory playerInventoryIn, IInventory machineInventoryIn, IIntArray dataIn) {
		super(F.C.COPIER, F.R.COPYING, id, playerInventoryIn, 2, 1, machineInventoryIn, dataIn);
	}

	@Override
	protected void initSlots(PlayerInventory pinv) {
		addSlot(new Slot(machineInventory, 0, 56, 26));
		addSlot(new Slot(machineInventory, 1, 56, 44));
		addSlot(new MachineResultSlot(pinv.player, machineInventory, 2, 116, 35));
		addSlot(new UpgradeSlot(machineInventory, 3, 22, 53));
		initPlayerSlotsAt(pinv, 8, 84);
	}
}
