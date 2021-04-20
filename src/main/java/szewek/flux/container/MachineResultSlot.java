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
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack remove(int amount) {
		if (hasItem()) {
			removeCount += Math.min(amount, getItem().getCount());
		}
		return super.remove(amount);
	}

	@Override
	public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
		checkTakeAchievements(stack);
		return super.onTake(thePlayer, stack);
	}

	@Override
	protected void onQuickCraft(ItemStack stack, int amount) {
		removeCount += amount;
		checkTakeAchievements(stack);
	}

	@Override
	protected void checkTakeAchievements(ItemStack stack) {
		stack.onCraftedBy(player.level, player, removeCount);
		if (!player.level.isClientSide && container instanceof AbstractMachineTile) {
			((AbstractMachineTile) container).updateRecipes(player);
		}
		removeCount = 0;
	}
}