package szewek.flux.util.market;

import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import szewek.fl.network.FluxAnalytics;
import szewek.flux.F;

import java.util.Set;
import java.util.function.BiPredicate;

public final class MarketUtil {
	private MarketUtil() {}

	public static boolean doTransaction(MerchantOffer offer, ItemStack stack1, ItemStack stack2) {
		boolean accept = canAccept(offer, stack1, stack2);
		if (accept) {
			stack1.shrink(offer.func_222205_b().getCount());
			if (!offer.getBuyingStackSecond().isEmpty()) {
				stack2.shrink(offer.getBuyingStackSecond().getCount());
			}
			FluxAnalytics.putView("flux/online_market/transaction");
		}
		return accept;
	}

	public static boolean canAccept(MerchantOffer offer, ItemStack stack1, ItemStack stack2) {
		final ItemStack offerStack = offer.getBuyingStackFirst();
		if (F.Tags.MARKET_ACCEPT.contains(offerStack.getItem())) {
			final ItemStack offerStack2 = offer.getBuyingStackSecond();
			return customEqualWithoutDamage(MarketUtil::matchingItemTag, stack1, offerStack)
					&& stack1.getCount() >= offerStack.getCount()
					&& customEqualWithoutDamage(ItemStack::isItemEqual, stack2, offerStack2)
					&& stack2.getCount() >= offerStack2.getCount();
		} else {
			return offer.matches(stack1, stack2);
		}
	}

	private static boolean customEqualWithoutDamage(BiPredicate<ItemStack, ItemStack> func, ItemStack left, ItemStack right) {
		if (right.isEmpty() && left.isEmpty()) {
			return true;
		} else {
			ItemStack stack = left.copy();
			if (stack.getItem().isDamageable()) {
				stack.setDamage(stack.getDamage());
			}

			return func.test(stack, right) && (!right.hasTag() || stack.hasTag() && NBTUtil.areNBTEquals(right.getTag(), stack.getTag(), false));
		}
	}

	private static boolean matchingItemTag(ItemStack left, ItemStack right) {
		if (ItemStack.areItemsEqual(left, right)) {
			return true;
		}
		Set<ResourceLocation> checkTags = right.getItem().getTags();
		Set<ResourceLocation> tags = left.getItem().getTags();
		return checkTags.containsAll(tags);
	}
}
