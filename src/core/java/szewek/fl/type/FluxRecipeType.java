package szewek.fl.type;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;

public class FluxRecipeType<T extends IRecipe<?>> implements IRecipeType<T> {
	private final String key;
	public final IRecipeSerializer<T> serializer;

	public FluxRecipeType(String key, IRecipeSerializer<T> ser) {
		this.key = key;
		serializer = ser;
	}

	@Override
	public String toString() {
		return key;
	}
}
