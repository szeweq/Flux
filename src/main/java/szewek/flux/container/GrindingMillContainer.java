package szewek.flux.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import szewek.flux.F;

public final class GrindingMillContainer extends Machine2For1Container {
	public GrindingMillContainer(int id, PlayerInventory playerInventoryIn, PacketBuffer data) {
		super(F.Containers.GRINDING_MILL, F.Recipes.GRINDING, id, playerInventoryIn, 2, 1);
	}

	public GrindingMillContainer(int id, PlayerInventory playerInventoryIn, IInventory machineInventoryIn, IIntArray dataIn) {
		super(F.Containers.GRINDING_MILL, F.Recipes.GRINDING, id, playerInventoryIn, 2, 1, machineInventoryIn, dataIn);
	}
}
