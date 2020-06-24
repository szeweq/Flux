package szewek.flux.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.IIntArray;
import szewek.fl.type.FluxContainerType;
import szewek.fl.util.IntPair;
import szewek.flux.recipe.AbstractMachineRecipe;

public class Machine2For1Container extends AbstractMachineContainer {
	private static final IntPair IO_SIZE = IntPair.of(2, 1);

	public Machine2For1Container(ContainerType containerType, IRecipeType<?> recipeType, int id, PlayerInventory playerInventory) {
		super(containerType, recipeType, id, playerInventory, IO_SIZE);
	}

	public Machine2For1Container(ContainerType containerType, IRecipeType<?> recipeType, int id, PlayerInventory playerInventory, IInventory machineInventory, IIntArray data) {
		super(containerType, recipeType, id, playerInventory, IO_SIZE, machineInventory, data);
	}

	@Override
	protected void initSlots(PlayerInventory playerInventory) {
		addSlot(new Slot(machineInventory, 0, 56, 26));
		addSlot(new Slot(machineInventory, 1, 56, 44));
		addSlot(new MachineResultSlot(playerInventory.player, machineInventory, 2, 116, 35));
		addSlot(new UpgradeSlot(machineInventory, 3, 22, 53));
		initPlayerSlotsAt(playerInventory, 8, 84);
	}

	public static FluxContainerType.IContainerBuilder<Machine2For1Container> make(final IRecipeType<? extends AbstractMachineRecipe> rtype) {
		return (type, id, inv, data) -> new Machine2For1Container(type, rtype, id, inv);
	}
}
