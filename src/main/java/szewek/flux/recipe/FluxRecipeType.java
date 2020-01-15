package szewek.flux.recipe;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;

public class FluxRecipeType<T extends IRecipe<?>> implements IRecipeType<T> {
	private final String key;

	public FluxRecipeType(String key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return key;
	}
}
