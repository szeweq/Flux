package szewek.flux.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import szewek.flux.item.MetalItem;

public enum Metal {
	IRON(0xE2C0BA, "iron", 1), GOLD(0xFFD700, "gold", 2), COPPER(0xE99868, "copper", 1), TIN(0xD0D3D6, "tin", 2);

	public final int color, harvestLevel;
	public final String name;

	Metal(int color, String name, int harvest) {
		this.color = color;
		this.name = name;
		harvestLevel = harvest;
	}

	public static int getColor(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof MetalItem) {
			return ((MetalItem) item).getMetal().color;
		}
		return 0;
	}

	public static int gritColors(ItemStack stack, int layer) {
		if (layer == 0) return 0xFFFFFF;
		return getColor(stack);
	}

	public static int ingotColors(ItemStack stack, int layer) {
		if (layer != 0) return 0xFFFFFF;
		return getColor(stack);
	}

	public static int itemColors(ItemStack stack, int layer) {
		return getColor(stack);
	}

	public static boolean all(Metal x) {
		return true;
	}

	public static boolean nonVanilla(Metal x) {
		return x != IRON && x != GOLD;
	}
}
