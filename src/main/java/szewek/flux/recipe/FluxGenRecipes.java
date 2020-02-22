package szewek.flux.recipe;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import szewek.fl.util.IntPair;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FluxGenRecipes {
	/* IntPair values: l = factor; r = usage */
	public static final IntPair DEFAULT = IntPair.of(1, 0);
	private static final Map<Item, IntPair> catalysts = new ConcurrentHashMap<>();
	private static final Map<Fluid, IntPair> coldFluids = new ConcurrentHashMap<>();
	private static final Map<Fluid, IntPair> hotFluids = new ConcurrentHashMap<>();

	public static boolean isCatalyst(Item item) {
		return catalysts.containsKey(item);
	}

	public static IntPair getCatalyst(Item item) {
		return catalysts.getOrDefault(item, DEFAULT);
	}

	public static boolean isHotFluid(Fluid fluid) {
		return isFluid(fluid, hotFluids);
	}

	public static IntPair getHotFluid(Fluid fluid) {
		return getFluid(fluid, hotFluids);
	}

	public static boolean isColdFluid(Fluid fluid) {
		return isFluid(fluid, coldFluids);
	}

	public static IntPair getColdFluid(Fluid fluid) {
		return getFluid(fluid, coldFluids);
	}

	private static boolean isFluid(@Nullable Fluid fluid, Map<Fluid, IntPair> m) {
		return m.containsKey(fluid);
	}

	private static IntPair getFluid(@Nullable Fluid fluid, Map<Fluid, IntPair> m) {
		return m.getOrDefault(fluid, DEFAULT);
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
		hotFluids.put(Fluids.LAVA, IntPair.of(2, 200));
		coldFluids.put(Fluids.WATER, IntPair.of(50, 200));
	}
}
