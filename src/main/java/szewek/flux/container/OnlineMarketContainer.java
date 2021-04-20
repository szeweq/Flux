package szewek.flux.container;

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import szewek.flux.F;
import szewek.flux.util.market.ClientOnlineMarket;
import szewek.flux.util.market.MarketResultSlot;

public class OnlineMarketContainer extends MerchantContainer {
	public OnlineMarketContainer(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
		this(id, playerInventory, new ClientOnlineMarket(playerInventory.player));
	}

	public OnlineMarketContainer(int id, PlayerInventory playerInventory, IMerchant merchant) {
		super(id, playerInventory, merchant);
		Slot outputSlot = new MarketResultSlot(playerInventory.player, merchant, tradeContainer, 2, 220, 37);
		outputSlot.index = 2;
		slots.set(2, outputSlot);
	}

	@Override
	public ContainerType<?> getType() {
		return F.C.ONLINE_MARKET;
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index == 2) {
				if (!moveItemStackTo(itemstack1, 3, 39, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (index != 0 && index != 1) {
				if (index < 30) {
					if (!moveItemStackTo(itemstack1, 30, 39, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index < 39 && !moveItemStackTo(itemstack1, 3, 30, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!moveItemStackTo(itemstack1, 3, 39, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemstack1);
		}

		return itemstack;
	}
}
