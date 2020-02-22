package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;

public final class WashingRecipe extends AbstractMachineRecipe {
	public WashingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(F.R.WASHING, idIn, groupIn, builder);
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(F.B.WASHER);
	}


}
