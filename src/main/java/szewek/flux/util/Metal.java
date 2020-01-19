package szewek.flux.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import szewek.flux.item.MetalItem;

public enum Metal {
	IRON(0xE2C0BA, "iron", 1),
	GOLD(0xFFD700, "gold", 2),
	COPPER(0xE99868, "copper", 1),
	TIN(0xD0D3D6, "tin", 1);
	//BRONZE(0x000, "bronze", 2);

	public final int color;
	public final String metalName;
	public final int harvestLevel;

	Metal(int color, String metalName, int harvestLevel) {
		this.color = color;
		this.metalName = metalName;
		this.harvestLevel = harvestLevel;
	}

	public final boolean nonVanilla() {
		return this != IRON && this != GOLD;
	}

	public final boolean nonAlloy() {
		return true;//return this != BRONZE;
	}

	public final boolean notVanillaOrAlloy() {
		return nonVanilla() && nonAlloy();
	}

	public final boolean all() {
		return true;
	}

	private static int getColor(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof MetalItem ? ((MetalItem)item).metal.color : 0;
	}

	public static int gritColors(ItemStack stack, int layer) {
		return layer == 0 ? 0xffffff : getColor(stack);
	}

	public static int ingotColors(ItemStack stack, int layer) {
		return layer != 0 ? 0xffffff : getColor(stack);
	}

	public static int itemColors(ItemStack stack, int layer) {
		return getColor(stack);
	}
}
