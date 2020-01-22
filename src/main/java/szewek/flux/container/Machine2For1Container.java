package szewek.flux.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.IIntArray;
import szewek.flux.recipe.AbstractMachineRecipe;

abstract class Machine2For1Container extends AbstractMachineContainer {
	protected Machine2For1Container(ContainerType containerTypeIn, IRecipeType<? extends AbstractMachineRecipe> recipeType, int id, PlayerInventory playerInventoryIn, int inputSize, int outputSize) {
		super(containerTypeIn, recipeType, id, playerInventoryIn, inputSize, outputSize);
	}

	protected Machine2For1Container(ContainerType containerTypeIn, IRecipeType<? extends AbstractMachineRecipe> recipeType, int id, PlayerInventory playerInventoryIn, int inputSize, int outputSize, IInventory machineInventoryIn, IIntArray dataIn) {
		super(containerTypeIn, recipeType, id, playerInventoryIn, inputSize, outputSize, machineInventoryIn, dataIn);
	}

	protected void initSlots(PlayerInventory playerInventory) {
		addSlot(new Slot(machineInventory, 0, 56, 26));
		addSlot(new Slot(machineInventory, 1, 56, 44));
		addSlot(new MachineResultSlot(playerInventory.player, machineInventory, 2, 116, 35));
		initPlayerSlotsAt(playerInventory, 8, 84);
	}
}
