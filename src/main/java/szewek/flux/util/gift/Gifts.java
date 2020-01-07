package szewek.flux.util.gift;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class Gifts {
	private static final Int2ObjectMap<GiftData> GIFT_MAP = new Int2ObjectOpenHashMap<>();

	private static void add(int day, int month, String name, int boxColor, int ribbonColor, Supplier<List<ItemStack>> makeItems) {
		GIFT_MAP.put(month * 32 + day, new GiftData(name, day, month, boxColor, ribbonColor, makeItems));
	}

	public static GiftData get(int xday) {
		return GIFT_MAP.get(xday);
	}

	public static int colorByGift(ItemStack stack, int pass) {
		CompoundNBT tag = stack.getTag();
		if (tag == null) return 0x808080;
		int xday = tag.getInt("xDay");
		GiftData gd = GIFT_MAP.get(xday);
		if (gd == null) return 0x404040;
		return pass == 0 ? gd.boxColor : gd.ribbonColor;
	}

	static {
		add(1, 1, "newyear", 0x2020F0, 0xF0F020, () -> Arrays.asList(
				new ItemStack(Items.NETHER_STAR),
				new ItemStack(Items.FIREWORK_ROCKET, 16)
		));
	}
}
