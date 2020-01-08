package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import szewek.flux.FBlocks;
import szewek.flux.FRecipes;

public class GrindingRecipe extends AbstractMachineRecipe {
	public GrindingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(FRecipes.GRINDING, idIn, groupIn, builder);
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(FBlocks.GRINDING_MILL);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return FRecipes.GRINDING_SERIALIZER;
	}
}
