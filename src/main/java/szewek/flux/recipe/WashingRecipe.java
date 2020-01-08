package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import szewek.flux.FBlocks;
import szewek.flux.FRecipes;

public class WashingRecipe extends AbstractMachineRecipe {
	public WashingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(FRecipes.WASHING, idIn, groupIn, builder);
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(FBlocks.WASHER);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return FRecipes.WASHING_SERIALIZER;
	}
}
