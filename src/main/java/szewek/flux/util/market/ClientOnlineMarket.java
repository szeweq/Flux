package szewek.flux.util.market;

import net.minecraft.entity.NPCMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MerchantOffers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class ClientOnlineMarket extends NPCMerchant {

	public ClientOnlineMarket(PlayerEntity p_i50184_1_) {
		super(p_i50184_1_);
		super.overrideOffers(new OnlineMarketMerchantOffers());
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void overrideOffers(@Nullable MerchantOffers offers) {
		OnlineMarketMerchantOffers marketOffers = new OnlineMarketMerchantOffers();
		if (offers != null) {
			marketOffers.addAll(offers);
		}
		super.overrideOffers(marketOffers);
	}
}
