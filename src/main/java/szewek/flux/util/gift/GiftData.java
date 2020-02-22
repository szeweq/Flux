package szewek.flux.util.gift;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.stream.Collectors;

public final class GiftData implements IGift {
	public final String name;
	public final int boxColor, ribbonColor;
	private final List<ItemStack> stacks;

	public GiftData(String name, int boxColor, int ribbonColor, List<ItemStack> stacks) {
		this.name = name;
		this.boxColor = boxColor;
		this.ribbonColor = ribbonColor;
		this.stacks = stacks;
	}

	@Override
	public List<ItemStack> getStacks() {
		return this.stacks.stream().map(ItemStack::copy).collect(Collectors.toList());
	}

	@Override
	public ITextComponent getText() {
		return new TranslationTextComponent("flux.gift.text." + this.name);
	}


}
