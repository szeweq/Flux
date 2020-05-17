package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;

public final class AlloyingRecipe extends AbstractMachineRecipe {
	public AlloyingRecipe(ResourceLocation idIn, MachineRecipeSerializer.Builder builder) {
		super(F.R.ALLOYING, idIn, builder);
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(F.B.ALLOY_CASTER);
	}
}
