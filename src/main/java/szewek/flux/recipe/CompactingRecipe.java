package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import szewek.flux.MFBlocks;
import szewek.flux.MFRecipes;

public class CompactingRecipe extends AbstractMachineRecipe {
	public CompactingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(MFRecipes.COMPACTING, idIn, groupIn, builder);
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(MFBlocks.COMPACTOR);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return MFRecipes.COMPACTING_SERIALIZER;
	}
}
