package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import szewek.flux.MFBlocks;
import szewek.flux.MFRecipes;

public class AlloyingRecipe extends AbstractMachineRecipe {
	public AlloyingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(MFRecipes.ALLOYING, idIn, groupIn, builder);
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(MFBlocks.ALLOY_CASTER);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return MFRecipes.ALLOYING_SERIALIZER;
	}
}
