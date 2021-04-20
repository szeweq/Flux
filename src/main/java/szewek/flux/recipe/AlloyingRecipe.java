package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;

public final class AlloyingRecipe extends AbstractMachineRecipe {
	public AlloyingRecipe(ResourceLocation idIn, MachineRecipeSerializer.Builder builder) {
		super(F.R.ALLOYING, idIn, builder);
	}

	@Deprecated
	public AlloyingRecipe(ResourceLocation idIn, String group, MachineRecipeSerializer.Builder builder) {
		this(idIn, builder.withGroup(group));
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(F.B.ALLOY_CASTER);
	}
}
