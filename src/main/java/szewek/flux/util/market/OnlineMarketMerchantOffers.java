package szewek.flux.util.market;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static szewek.flux.util.market.MarketUtil.canAccept;

@ParametersAreNonnullByDefault
public class OnlineMarketMerchantOffers extends MerchantOffers {
	@Nullable
	@Override
	public MerchantOffer func_222197_a(ItemStack stack1, ItemStack stack2, int index) {
		if (index > 0 && index < size()) {
			MerchantOffer offer1 = get(index);
			return canAccept(offer1, stack1, stack2) ? offer1 : null;
		} else {
			for (MerchantOffer offer : this) {
				if (canAccept(offer, stack1, stack2)) {
					return offer;
				}
			}
			return null;
		}
	}

	public static int compareOffers(MerchantOffer o1, MerchantOffer o2) {
		ItemStack o1buy1 = o1.getBuyingStackFirst();
		ItemStack o2buy1 = o2.getBuyingStackFirst();
		if (o1buy1.getItem() == Items.EMERALD) {
			if (o2buy1.getItem() == Items.EMERALD) {
				return o1buy1.getCount() - o2buy1.getCount();
			}
			return -1;
		}
		return o1buy1.getCount() - o2buy1.getCount();
	}
}
