package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;

public final class GrindingRecipe extends AbstractMachineRecipe {
	public GrindingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(F.Recipes.GRINDING, idIn, groupIn, builder);
	}

	public ItemStack getIcon() {
		return new ItemStack(F.Blocks.GRINDING_MILL);
	}

	public IRecipeSerializer<?> getSerializer() {
		return F.Recipes.GRINDING_SERIALIZER;
	}
}
