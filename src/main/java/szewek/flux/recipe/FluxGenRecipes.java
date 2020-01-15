package szewek.flux.recipe;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

public final class FluxGenRecipes {
	public static final Result DEFAULT = new Result(1, 0);
	private static final Map<Item, Result> catalysts = new HashMap<>();
	private static final Map<FluidStack, Result> coldFluids = new HashMap<>();
	private static final Map<FluidStack, Result> hotFluids = new HashMap<>();

	public static boolean isCatalyst(Item item) {
		return catalysts.containsKey(item);
	}

	public static Result getCatalyst(Item item) {
		return catalysts.getOrDefault(item, DEFAULT);
	}

	public static boolean isHotFluid(FluidStack stack) {
		return isFluid(stack, hotFluids);
	}

	public static Result getHotFluid(FluidStack stack) {
		return getFluid(stack, hotFluids);
	}

	public static boolean isColdFluid(FluidStack stack) {
		return isFluid(stack, coldFluids);
	}

	public static Result getColdFluid(FluidStack stack) {
		return getFluid(stack, coldFluids);
	}

	private static boolean isFluid(FluidStack stack, Map<FluidStack, Result> m) {
		if (stack == null) {
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

	private static Result getFluid(FluidStack stack, Map<FluidStack, Result> m) {
		if (stack == null) {
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
		catalysts.put(item, new Result(factor, usage));
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
		hotFluids.put(new FluidStack(Fluids.LAVA, 0), new FluxGenRecipes.Result(2, 200));
		coldFluids.put(new FluidStack(Fluids.WATER, 0), new FluxGenRecipes.Result(50, 200));
	}

	public static final class Result {
		public final int factor, usage;

		public Result(int factor, int usage) {
			this.factor = factor;
			this.usage = usage;
		}
	}
}
