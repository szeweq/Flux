package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import szewek.flux.FBlocks;
import szewek.flux.FRecipes;

public class CompactingRecipe extends AbstractMachineRecipe {
	public CompactingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(FRecipes.COMPACTING, idIn, groupIn, builder);
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(FBlocks.COMPACTOR);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return FRecipes.COMPACTING_SERIALIZER;
	}
}
