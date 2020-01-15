package szewek.flux.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import szewek.flux.util.gift.GiftData;
import szewek.flux.util.gift.Gifts;

import java.util.List;

public final class GiftItem extends Item {
	public GiftItem(Properties properties) {
		super(properties);
	}

	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		playerIn.setActiveHand(handIn);
		return new ActionResult<>(ActionResultType.CONSUME, playerIn.getHeldItem(handIn));
	}

	public int getUseDuration(ItemStack stack) {
		return 30;
	}

	public UseAction getUseAction(ItemStack stack) {
		return UseAction.EAT;
	}

	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
		if (!worldIn.isRemote() && entityLiving instanceof ServerPlayerEntity && stack.getItem() == (GiftItem)this) {
			CompoundNBT tag = stack.getTag();
			if (tag == null || tag.isEmpty()) {
				entityLiving.sendMessage(new TranslationTextComponent("flux.gift.invalid"));
				return ItemStack.EMPTY;
			}

			int xday = tag.getInt("xDay");
			GiftData gd = Gifts.get(xday);
			if (gd == null) {
				entityLiving.sendMessage(new TranslationTextComponent("flux.gift.invalid"));
				return ItemStack.EMPTY;
			}

			List<ItemStack> stacks = gd.getStacks();
			PlayerEntity player = (PlayerEntity)entityLiving;
			for (ItemStack giftStack : stacks) {
				ItemHandlerHelper.giveItemToPlayer(player, giftStack, -1);
			}

			player.sendMessage(gd.getText());
			stack.grow(-1);
		}

		return stack;
	}


}
