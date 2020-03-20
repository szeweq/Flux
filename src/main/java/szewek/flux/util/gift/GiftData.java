package szewek.flux.util.gift;

import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public final class GiftData {
	public final int boxColor, ribbonColor;
	private final List<ItemStack> stacks;

	public GiftData(int boxColor, int ribbonColor, List<ItemStack> stacks) {
		this.boxColor = boxColor;
		this.ribbonColor = ribbonColor;
		this.stacks = stacks;
	}

	public List<ItemStack> getStacks() {
		return stacks.stream().map(ItemStack::copy).collect(Collectors.toList());
	}

}
