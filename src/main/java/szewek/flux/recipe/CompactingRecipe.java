package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;

public final class CompactingRecipe extends AbstractMachineRecipe {
	public CompactingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(F.R.COMPACTING, idIn, groupIn, builder);
	}

	public ItemStack getIcon() {
		return new ItemStack(F.B.COMPACTOR);
	}
}
