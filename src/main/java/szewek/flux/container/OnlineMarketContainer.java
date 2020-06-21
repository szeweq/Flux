package szewek.flux.container;

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.Slot;
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
		Slot outputSlot = new MarketResultSlot(playerInventory.player, merchant, merchantInventory, 2, 220, 37);
		outputSlot.slotNumber = 2;
		inventorySlots.set(2, outputSlot);
	}

	@Override
	public ContainerType<?> getType() {
		return F.C.ONLINE_MARKET;
	}
}
