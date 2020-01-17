package szewek.flux.recipe;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface ICompatRecipe {
	ItemStack getOutput();
	float getExperience();
	void consume(List<ItemStack> tt);
}
