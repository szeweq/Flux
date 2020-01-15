package szewek.flux.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import szewek.flux.tile.AbstractMachineTile;

public class MachineResultSlot extends Slot {
	private final PlayerEntity player;
	private int removeCount;

	public MachineResultSlot(PlayerEntity player, IInventory inventoryIn, int slotIndex, int xPosition, int yPosition) {
		super(inventoryIn, slotIndex, xPosition, yPosition);
		this.player = player;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		if (getHasStack()) {
			removeCount += Math.min(amount, getStack().getCount());
		}
		return super.decrStackSize(amount);
	}

	@Override
	public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
		onCrafting(stack);
		return super.onTake(thePlayer, stack);
	}

	@Override
	protected void onCrafting(ItemStack stack, int amount) {
		removeCount += amount;
		onCrafting(stack);
	}

	@Override
	protected void onCrafting(ItemStack stack) {
		stack.onCrafting(player.world, player, removeCount);
		if (!player.world.isRemote && inventory instanceof AbstractMachineTile) {
			((AbstractMachineTile) inventory).updateRecipes(player);
		}
		removeCount = 0;
	}
}