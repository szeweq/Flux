package szewek.flux.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import szewek.flux.F;

public final class AlloyCasterContainer extends AbstractMachineContainer {
	public AlloyCasterContainer(int id, PlayerInventory playerInventoryIn, PacketBuffer data) {
		super(F.Containers.ALLOY_CASTER, F.Recipes.ALLOYING, id, playerInventoryIn, 2, 1);
	}

	public AlloyCasterContainer(int id, PlayerInventory playerInventoryIn, IInventory machineInventoryIn, IIntArray dataIn) {
		super(F.Containers.ALLOY_CASTER, F.Recipes.ALLOYING, id, playerInventoryIn, 2, 1, machineInventoryIn, dataIn);
	}

	protected void initSlots(PlayerInventory playerInventory) {
		this.addSlot(new Slot(machineInventory, 0, 56, 26));
		this.addSlot(new Slot(machineInventory, 1, 56, 44));
		this.addSlot(new MachineResultSlot(playerInventory.player, machineInventory, 2, 116, 35));
		this.initPlayerSlotsAt(playerInventory, 8, 84);
	}

}
