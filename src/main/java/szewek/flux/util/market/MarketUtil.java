package szewek.flux.util.market;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;

import java.util.Set;
import java.util.function.BiPredicate;

public final class MarketUtil {
	private MarketUtil() {}

	public static boolean doTransaction(MerchantOffer offer, ItemStack stack1, ItemStack stack2) {
		boolean accept = canAccept(offer, stack1, stack2);
		if (accept) {
			stack1.shrink(offer.getCostA().getCount());
			if (!offer.getCostB().isEmpty()) {
				stack2.shrink(offer.getCostB().getCount());
			}
		}
		return accept;
	}

	public static boolean canAccept(MerchantOffer offer, ItemStack stack1, ItemStack stack2) {
		final ItemStack offerStack = offer.getBaseCostA();
		if (F.Tags.MARKET_ACCEPT.contains(offerStack.getItem())) {
			final ItemStack offerStack2 = offer.getCostB();
			return customEqualWithoutDamage(MarketUtil::matchingItemTag, stack1, offerStack)
					&& stack1.getCount() >= offerStack.getCount()
					&& customEqualWithoutDamage(ItemStack::sameItem, stack2, offerStack2)
					&& stack2.getCount() >= offerStack2.getCount();
		} else {
			return offer.satisfiedBy(stack1, stack2);
		}
	}

	private static boolean customEqualWithoutDamage(BiPredicate<ItemStack, ItemStack> func, ItemStack left, ItemStack right) {
		if (right.isEmpty() && left.isEmpty()) {
			return true;
		} else {
			ItemStack stack = left.copy();
			if (stack.getItem().canBeDepleted()) {
				stack.setDamageValue(stack.getDamageValue());
			}

			return func.test(stack, right) && (!right.hasTag() || stack.hasTag() && NBTUtil.compareNbt(right.getTag(), stack.getTag(), false));
		}
	}

	private static boolean matchingItemTag(ItemStack left, ItemStack right) {
		if (ItemStack.isSame(left, right)) {
			return true;
		}
		Set<ResourceLocation> checkTags = right.getItem().getTags();
		Set<ResourceLocation> tags = left.getItem().getTags();
		return checkTags.containsAll(tags);
	}

	public static int compareOffers(MerchantOffer o1, MerchantOffer o2) {
		ItemStack o1buy1 = o1.getBaseCostA();
		ItemStack o2buy1 = o2.getBaseCostA();
		if (o1buy1.getItem() != o2buy1.getItem()) {
			if (o1buy1.getItem() == Items.EMERALD) {
				return -1;
			}
			if (o2buy1.getItem() == Items.EMERALD) {
				return 1;
			}
		}
		if (areSameOffers(o1, o2)) {
			return 0;
		}
		int c = o1buy1.getCount() - o2buy1.getCount();
		if (c == 0) {
			c = -1;
		}
		return c;
	}

	public static boolean areSameOffers(MerchantOffer o1, MerchantOffer o2) {
		return o1.getBaseCostA().equals(o2.getBaseCostA(), false)
				&& o1.getCostB().equals(o2.getCostB(), false)
				&& o1.getResult().equals(o2.getResult(), false);
	}
}
