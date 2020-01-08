package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import szewek.flux.FBlocks;
import szewek.flux.FRecipes;

public class AlloyingRecipe extends AbstractMachineRecipe {
	public AlloyingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(FRecipes.ALLOYING, idIn, groupIn, builder);
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(FBlocks.ALLOY_CASTER);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return FRecipes.ALLOYING_SERIALIZER;
	}
}
