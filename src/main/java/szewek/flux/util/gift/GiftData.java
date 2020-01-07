package szewek.flux.util.gift;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.function.Supplier;

public class GiftData implements IGift {
	public final String name;
	public final int day, month;
	public final int boxColor, ribbonColor;
	private final Supplier<List<ItemStack>> makeItems;

	public GiftData(String name, int day, int month, int boxColor, int ribbonColor, Supplier<List<ItemStack>> makeItems) {
		this.name = name;
		this.day = day;
		this.month = month;
		this.boxColor = boxColor;
		this.ribbonColor = ribbonColor;
		this.makeItems = makeItems;
	}

	public List<ItemStack> getStacks() {
		return makeItems.get();
	}

	@Override
	public ITextComponent getText() {
		return new TranslationTextComponent("flux.gift.text." + name);
	}
}
