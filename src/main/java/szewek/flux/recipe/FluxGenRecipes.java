package szewek.flux.recipe;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

public final class FluxGenRecipes {
	private static final Map<ItemStack, Result> catalysts = new HashMap<>();
	private static final Map<FluidStack, Result> coldFluids = new HashMap<>();
	private static final Map<FluidStack, Result> hotFluids = new HashMap<>();

	static {
		catalysts.put(new ItemStack(Items.FLINT), new Result(2, 2));
		catalysts.put(new ItemStack(Items.REDSTONE), new Result(2, 1));
		catalysts.put(new ItemStack(Blocks.REDSTONE_BLOCK), new Result(10, 1));
		catalysts.put(new ItemStack(Items.BLAZE_POWDER), new Result(4, 1));
		catalysts.put(new ItemStack(Items.PRISMARINE_SHARD), new Result(3, 1));
		catalysts.put(new ItemStack(Items.CONDUIT), new Result(30, 1));
		catalysts.put(new ItemStack(Items.DRAGON_BREATH), new Result(60, 1));
		catalysts.put(new ItemStack(Items.NETHER_STAR), new Result(100, 1));
		catalysts.put(new ItemStack(Items.TOTEM_OF_UNDYING), new Result(200, 1));
		hotFluids.put(new FluidStack(Fluids.LAVA, 0), new Result(2, 200));
		coldFluids.put(new FluidStack(Fluids.WATER, 0), new Result(50, 200));
	}

	public static boolean isCatalyst(ItemStack stack) {
		if (!stack.isEmpty()) {
			if (catalysts.containsKey(stack))
				return true;
			for (ItemStack is : catalysts.keySet()) {
				if (is.getDamage() == 32767 ? stack.isItemEqualIgnoreDurability(is) : stack.isItemEqual(is))
					return true;
			}
		}
		return false;
	}

	public static Result getCatalyst(ItemStack stack) {
		if (!stack.isEmpty()) {
			if (catalysts.containsKey(stack))
				return catalysts.get(stack);
			for (ItemStack is : catalysts.keySet()) {
				if (is.getDamage() == 32767 ? stack.isItemEqualIgnoreDurability(is) : stack.isItemEqual(is))
					return catalysts.get(is);
			}
		}
		return Result.DEFAULT;
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
		if (stack == null) return false;
		if (m.containsKey(stack))
			return true;
		Fluid fl = stack.getFluid();
		for (FluidStack fs : m.keySet()) {
			if (fs.getRawFluid() == fl) return true;
		}
		return false;
	}

	private static Result getFluid(FluidStack stack, Map<FluidStack, Result> m) {
		if (stack == null) return Result.DEFAULT;
		if (m.containsKey(stack))
			return m.get(stack);
		Fluid fl = stack.getFluid();
		for (FluidStack fs : m.keySet()) {
			if (fs.getRawFluid() == fl) return m.get(fs);
		}
		return Result.DEFAULT;
	}

	public static final class Result {
		static final Result DEFAULT = new Result(1, 0);

		public final int factor, usage;

		Result(int f, int u) {
			factor = f;
			usage = u;
		}
	}
}
