package szewek.flux.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import szewek.flux.util.Gifts;

public final class GiftItem extends Item {
	private static final ITextComponent GIFT_INVALID = new TranslationTextComponent("flux.gift.invalid");

	public GiftItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		playerIn.setActiveHand(handIn);
		return new ActionResult<>(ActionResultType.CONSUME, playerIn.getHeldItem(handIn));
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 30;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.EAT;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
		if (!worldIn.isRemote && entityLiving instanceof ServerPlayerEntity && stack.getItem() == this) {
			CompoundNBT tag = stack.getTag();
			if (tag == null || tag.isEmpty() || !tag.contains("LootTable")) {
				entityLiving.sendMessage(GIFT_INVALID, Util.DUMMY_UUID);
				return ItemStack.EMPTY;
			}

			String lt = tag.getString("LootTable");
			ResourceLocation loc = ResourceLocation.tryCreate(lt);
			if (loc == null) {
				entityLiving.sendMessage(GIFT_INVALID, Util.DUMMY_UUID);
				return ItemStack.EMPTY;
			}
			LootContext lootCtx = new LootContext.Builder((ServerWorld) worldIn)
					.withParameter(LootParameters.POSITION, entityLiving.getPosition())
					.withParameter(LootParameters.THIS_ENTITY, entityLiving)
					.build(LootParameterSets.GIFT);
			Gifts.produceGifts((ServerPlayerEntity) entityLiving, lootCtx, loc);

			stack.grow(-1);
		}

		return stack;
	}


}
