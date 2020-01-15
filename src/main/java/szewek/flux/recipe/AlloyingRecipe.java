package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;

public final class AlloyingRecipe extends AbstractMachineRecipe {
	public AlloyingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(F.Recipes.ALLOYING, idIn, groupIn, builder);
	}

	public ItemStack getIcon() {
		return new ItemStack(F.Blocks.ALLOY_CASTER);
	}

	public IRecipeSerializer<?> getSerializer() {
		return F.Recipes.ALLOYING_SERIALIZER;
	}
}
