package szewek.flux.util.metals;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import szewek.flux.item.MetalItem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Metals {
	static final Set<Metal> allMetals = new HashSet<>();
	public static final Metal
			IRON = Metal.vanilla(0xC0C0C0, "iron", 1),
			GOLD = Metal.vanilla(0xFFD84A, "gold", 2),
			NETHERITE = new Metal(0x6B686B, "netherite", 4, Metal.NO_ORE | Metal.VANILLA),
			COPPER = new Metal(0xE99868, "copper", 1),
			TIN = new Metal(0xCAD6DC, "tin", 1),
			BRONZE = Metal.alloy(0xCCA168, "bronze", 2),
			STEEL = Metal.alloy(0xA0A0A0, "steel", 3);

	private Metals() {}

	public static Iterable<Metal> all() {
		return Collections.unmodifiableSet(allMetals);
	}

	private static int getColor(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof MetalItem ? ((MetalItem)item).metal.color : 0;
	}

	public static int gritColors(ItemStack stack, int layer) {
		return layer == 0 ? 0xffffff : getColor(stack);
	}

	public static int ingotColors(ItemStack stack, int layer) {
		return layer == 0 ? getColor(stack) : 0xffffff;
	}

	public static int itemColors(ItemStack stack, int layer) {
		return getColor(stack);
	}
}
