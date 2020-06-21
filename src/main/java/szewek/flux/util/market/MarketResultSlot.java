package szewek.flux.util.market;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.MerchantInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.stats.Stats;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MarketResultSlot extends Slot {
	private final MerchantInventory merchantInventory;
	private final PlayerEntity player;
	private int removeCount;
	private final IMerchant merchant;

	public MarketResultSlot(PlayerEntity player, IMerchant merchant, MerchantInventory merchantInventory, int slotIndex, int xPosition, int yPosition) {
		super(merchantInventory, slotIndex, xPosition, yPosition);
		this.player = player;
		this.merchant = merchant;
		this.merchantInventory = merchantInventory;
	}

	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	public ItemStack decrStackSize(int amount) {
		if (getHasStack()) {
			removeCount += Math.min(amount, getStack().getCount());
		}

		return super.decrStackSize(amount);
	}

	protected void onCrafting(ItemStack stack, int amount) {
		removeCount += amount;
		onCrafting(stack);
	}

	protected void onCrafting(ItemStack stack) {
		stack.onCrafting(player.world, player, removeCount);
		removeCount = 0;
	}

	public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
		onCrafting(stack);
		MerchantOffer offer = merchantInventory.func_214025_g();
		if (offer != null) {
			ItemStack stack1 = merchantInventory.getStackInSlot(0);
			ItemStack stack2 = merchantInventory.getStackInSlot(1);
			if (MarketUtil.doTransaction(offer, stack1, stack2) || MarketUtil.doTransaction(offer, stack2, stack1)) {
				merchant.onTrade(offer);
				thePlayer.addStat(Stats.TRADED_WITH_VILLAGER);
				merchantInventory.setInventorySlotContents(0, stack1);
				merchantInventory.setInventorySlotContents(1, stack2);
			}

			merchant.setXP(merchant.getXp() + offer.getGivenExp());
		}

		return stack;
	}
}
