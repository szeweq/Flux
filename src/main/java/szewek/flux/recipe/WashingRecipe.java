package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;

public final class WashingRecipe extends AbstractMachineRecipe {
	public WashingRecipe(ResourceLocation idIn, MachineRecipeSerializer.Builder builder) {
		super(F.R.WASHING, idIn, builder);
	}

	@Deprecated
	public WashingRecipe(ResourceLocation idIn, String group, MachineRecipeSerializer.Builder builder) {
		this(idIn, builder.withGroup(group));
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(F.B.WASHER);
	}


}
