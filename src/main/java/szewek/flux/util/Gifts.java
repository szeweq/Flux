package szewek.flux.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.HashSet;
import java.util.Set;

public final class Gifts {
	private static final ITextComponent RECEIVED_GIFT = new TranslationTextComponent("flux.gift.received");
	private static final Set<ResourceLocation> GIFT_LOOT_TABLES = new HashSet<>();

	public static int colorByGift(ItemStack stack, int pass) {
		CompoundNBT tag = stack.getTag();
		if (tag == null) {
			return 0x808080;
		}
		String name = pass == 0 ? "Box" : "Ribbon";
		return tag.contains(name) ? tag.getInt(name) : 0x404040;
	}

	public static void saveGiftLootTables(Set<ResourceLocation> locSet) {
		GIFT_LOOT_TABLES.clear();
		GIFT_LOOT_TABLES.addAll(locSet);
	}

	public static void makeGiftsForPlayer(ServerPlayerEntity player) {
		CompoundNBT data = player.getPersistentData();
		ListNBT received = data.getList("receivedGifts", 8);
		boolean change = false;
		LootContext lootCtx = new LootContext.Builder((ServerWorld) player.world)
				.withParameter(LootParameters.POSITION, player.getPosition())
				.withParameter(LootParameters.THIS_ENTITY, player)
				.build(LootParameterSets.GIFT);
		for (ResourceLocation loc : GIFT_LOOT_TABLES) {
			final StringNBT locNBT = StringNBT.valueOf(loc.toString());
			if (!received.contains(locNBT)) {
				produceGifts(player, lootCtx, loc);
				change = true;
				received.add(locNBT);
			}
		}
		if (change) {
			player.sendMessage(RECEIVED_GIFT);
			data.put("receivedGifts", received);
		}
	}

	public static void produceGifts(ServerPlayerEntity player, LootContext lootCtx, ResourceLocation loc) {
		LootTable lootTable = player.server.getLootTableManager().getLootTableFromLocation(loc);
		for (ItemStack stack : lootTable.generate(lootCtx)) {
			ItemHandlerHelper.giveItemToPlayer(player, stack);
		}
	}

	private Gifts() {}
}
