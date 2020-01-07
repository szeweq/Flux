package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import szewek.flux.MFBlocks;
import szewek.flux.MFRecipes;

public class WashingRecipe extends AbstractMachineRecipe {
	public WashingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(MFRecipes.WASHING, idIn, groupIn, builder);
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(MFBlocks.WASHER);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return MFRecipes.WASHING_SERIALIZER;
	}
}
