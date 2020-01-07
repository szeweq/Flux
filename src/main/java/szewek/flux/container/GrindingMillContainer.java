package szewek.flux.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import szewek.flux.MFContainers;
import szewek.flux.MFRecipes;

public class GrindingMillContainer extends AbstractMachineContainer {
	public GrindingMillContainer(int id, PlayerInventory playerInventoryIn, PacketBuffer data) {
		super(MFContainers.GRINDING_MILL, MFRecipes.GRINDING, id, playerInventoryIn, 2, 1);
	}

	public GrindingMillContainer(int id, PlayerInventory playerInventoryIn, IInventory machineInventoryIn, IIntArray dataIn) {
		super(MFContainers.GRINDING_MILL, MFRecipes.GRINDING, id, playerInventoryIn, 2, 1, machineInventoryIn, dataIn);
	}

	@Override
	protected void initSlots(PlayerInventory playerInventory) {
		addSlot(new Slot(machineInventory, 0, 56, 26));
		addSlot(new Slot(machineInventory, 1, 56, 44));
		addSlot(new MachineResultSlot(playerInventory.player, machineInventory, 2, 116, 35));

		initPlayerSlotsAt(playerInventory, 8, 84);
	}
}
