package szewek.flux.recipe;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.fluids.FluidStack;
import szewek.fl.util.IntPair;

import java.util.HashMap;
import java.util.Map;

public final class FluxGenRecipes {
	/* IntPair values: l = factor; r = usage */
	public static final IntPair DEFAULT = IntPair.of(1, 0);
	private static final Map<Item, IntPair> catalysts = new HashMap<>();
	private static final Map<FluidStack, IntPair> coldFluids = new HashMap<>();
	private static final Map<FluidStack, IntPair> hotFluids = new HashMap<>();

	public static boolean isCatalyst(Item item) {
		return catalysts.containsKey(item);
	}

	public static IntPair getCatalyst(Item item) {
		return catalysts.getOrDefault(item, DEFAULT);
	}

	public static boolean isHotFluid(FluidStack stack) {
		return isFluid(stack, hotFluids);
	}

	public static IntPair getHotFluid(FluidStack stack) {
		return getFluid(stack, hotFluids);
	}

	public static boolean isColdFluid(FluidStack stack) {
		return isFluid(stack, coldFluids);
	}

	public static IntPair getColdFluid(FluidStack stack) {
		return getFluid(stack, coldFluids);
	}

	private static boolean isFluid(FluidStack stack, Map<FluidStack, IntPair> m) {
		if (stack == null || stack.isEmpty()) {
			return false;
		} else if (m.containsKey(stack)) {
			return true;
		} else {
			Fluid fl = stack.getFluid();
			for (FluidStack fs : m.keySet()) {
				if (fs.getRawFluid() == fl) return true;
			}
			return false;
		}
	}

	private static IntPair getFluid(FluidStack stack, Map<FluidStack, IntPair> m) {
		if (stack == null || stack.isEmpty()) {
			return DEFAULT;
		} else {
			if (m.containsKey(stack)) {
				return m.getOrDefault(stack, DEFAULT);
			} else {
				Fluid fl = stack.getFluid();
				for (FluidStack fs : m.keySet()) {
					if (fs.getRawFluid() == fl) {
						return m.getOrDefault(fs, DEFAULT);
					}
				}
				return DEFAULT;
			}
		}
	}

	private static void add(Item item, int factor, int usage) {
		catalysts.put(item, IntPair.of(factor, usage));
	}

	private FluxGenRecipes() {
	}

	static {
		add(Items.FLINT, 2, 2);
		add(Items.REDSTONE, 2, 1);
		add(Items.REDSTONE_BLOCK, 10, 1);
		add(Items.BLAZE_POWDER, 4, 1);
		add(Items.PRISMARINE_SHARD, 3, 1);
		add(Items.CONDUIT, 30, 1);
		add(Items.DRAGON_BREATH, 60, 1);
		add(Items.NETHER_STAR, 100, 1);
		add(Items.TOTEM_OF_UNDYING, 200, 1);
		hotFluids.put(new FluidStack(Fluids.LAVA, 0), IntPair.of(2, 200));
		coldFluids.put(new FluidStack(Fluids.WATER, 0), IntPair.of(50, 200));
	}
}
