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

	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	public ItemStack remove(int amount) {
		if (hasItem()) {
			removeCount += Math.min(amount, getItem().getCount());
		}

		return super.remove(amount);
	}

	protected void onQuickCraft(ItemStack stack, int amount) {
		removeCount += amount;
		checkTakeAchievements(stack);
	}

	protected void checkTakeAchievements(ItemStack stack) {
		stack.onCraftedBy(player.level, player, removeCount);
		removeCount = 0;
	}

	public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
		checkTakeAchievements(stack);
		MerchantOffer offer = merchantInventory.getActiveOffer();
		if (offer != null) {
			ItemStack stack1 = merchantInventory.getItem(0);
			ItemStack stack2 = merchantInventory.getItem(1);
			if (MarketUtil.doTransaction(offer, stack1, stack2) || MarketUtil.doTransaction(offer, stack2, stack1)) {
				merchant.notifyTrade(offer);
				thePlayer.awardStat(Stats.TRADED_WITH_VILLAGER);
				merchantInventory.setItem(0, stack1);
				merchantInventory.setItem(1, stack2);
			}

			merchant.overrideXp(merchant.getVillagerXp() + offer.getXp());
		}

		return stack;
	}
}
