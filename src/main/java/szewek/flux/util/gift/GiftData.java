package szewek.flux.util.gift;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.stream.Collectors;

public final class GiftData implements IGift {
	public final String name;
	public final int day, month, boxColor, ribbonColor;
	private final List<ItemStack> stacks;

	public GiftData(String name, int day, int month, int boxColor, int ribbonColor, List<ItemStack> stacks) {
		this.name = name;
		this.day = day;
		this.month = month;
		this.boxColor = boxColor;
		this.ribbonColor = ribbonColor;
		this.stacks = stacks;
	}

	public List<ItemStack> getStacks() {
		return this.stacks.stream().map(ItemStack::copy).collect(Collectors.toList());
	}

	public ITextComponent getText() {
		return new TranslationTextComponent("flux.gift.text." + this.name);
	}


}
