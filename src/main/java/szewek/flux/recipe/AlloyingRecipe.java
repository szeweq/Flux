package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;

public final class AlloyingRecipe extends AbstractMachineRecipe {
	public AlloyingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(F.R.ALLOYING, idIn, groupIn, builder);
	}

	public ItemStack getIcon() {
		return new ItemStack(F.B.ALLOY_CASTER);
	}

	public IRecipeSerializer<?> getSerializer() {
		return F.R.ALLOYING_SERIALIZER;
	}
}
