package szewek.flux.util.gift;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public interface IGift {
	List<ItemStack> getStacks();
	ITextComponent getText();
}
