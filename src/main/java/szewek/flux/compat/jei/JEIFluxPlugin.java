package szewek.flux.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F.R;
import szewek.flux.F.B;
import szewek.flux.FluxMod;
import szewek.flux.recipe.*;

import java.util.Collection;
import java.util.Map;

@JeiPlugin
public class JEIFluxPlugin implements IModPlugin {
	private MachineCategory<GrindingRecipe> grindingCategory;
	private MachineCategory<AlloyingRecipe> alloyingCategory;
	private MachineCategory<WashingRecipe> washingCategory;
	private MachineCategory<CompactingRecipe> compactingCategory;

	@Override
	public ResourceLocation getPluginUid() {
		return FluxMod.location("jei");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		grindingCategory = new MachineCategory<>(guiHelper, "grinding", "grinding_mill", GrindingRecipe.class, B.GRINDING_MILL, 200);
		alloyingCategory = new MachineCategory<>(guiHelper, "alloying", "alloy_caster", AlloyingRecipe.class, B.ALLOY_CASTER, 200);
		washingCategory = new MachineCategory<>(guiHelper, "washing", "washer", WashingRecipe.class, B.WASHER, 200);
		compactingCategory = new MachineCategory<>(guiHelper, "compacting", "compactor", CompactingRecipe.class, B.COMPACTOR, 200);
		registration.addRecipeCategories(
				grindingCategory, alloyingCategory, washingCategory, compactingCategory
		);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(getRecipes(R.GRINDING), FluxMod.location("grinding"));
		registration.addRecipes(getRecipes(R.ALLOYING), FluxMod.location("alloying"));
		registration.addRecipes(getRecipes(R.WASHING), FluxMod.location("washing"));
		registration.addRecipes(getRecipes(R.COMPACTING), FluxMod.location("compacting"));
	}

	@SuppressWarnings({"ConstantConditions", "unchecked"})
	private static <C extends IInventory, T extends IRecipe<C>> Collection<T> getRecipes(IRecipeType<T> rtype) {
		ClientWorld world = Minecraft.getInstance().world;
		RecipeManager rm = world.getRecipeManager();
		Map<ResourceLocation, IRecipe<C>> rmap = rm.getRecipes(rtype);
		return (Collection<T>) rmap.values();
	}
}
