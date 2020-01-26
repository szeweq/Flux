package szewek.flux.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F.R;
import szewek.flux.F.B;
import szewek.flux.FluxMod;
import szewek.flux.container.Machine2For1Container;
import szewek.flux.gui.MachineScreen;
import szewek.flux.recipe.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@JeiPlugin
public class JEIFluxPlugin implements IModPlugin {
	private static final ResourceLocation
			GRINDING = FluxMod.location("grinding"),
			ALLOYING = FluxMod.location("alloying"),
			WASHING = FluxMod.location("washing"),
			COMPACTING = FluxMod.location("compacting");

	private MachineCategory<GrindingRecipe> grindingCategory;
	private MachineCategory<AlloyingRecipe> alloyingCategory;
	private MachineCategory<WashingRecipe> washingCategory;
	private MachineCategory<CompactingRecipe> compactingCategory;

	@Override
	public ResourceLocation getPluginUid() {
		return FluxMod.location("jei");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration reg) {
		IJeiHelpers jeiHelpers = reg.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		grindingCategory = new MachineCategory<>(guiHelper, "grinding", "grinding_mill", GrindingRecipe.class, B.GRINDING_MILL, 200);
		alloyingCategory = new MachineCategory<>(guiHelper, "alloying", "alloy_caster", AlloyingRecipe.class, B.ALLOY_CASTER, 200);
		washingCategory = new MachineCategory<>(guiHelper, "washing", "washer", WashingRecipe.class, B.WASHER, 200);
		compactingCategory = new MachineCategory<>(guiHelper, "compacting", "compactor", CompactingRecipe.class, B.COMPACTOR, 200);
		reg.addRecipeCategories(
				grindingCategory, alloyingCategory, washingCategory, compactingCategory
		);
	}

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		reg.addRecipes(getRecipes(R.GRINDING), GRINDING);
		reg.addRecipes(getRecipes(R.ALLOYING), ALLOYING);
		reg.addRecipes(getRecipes(R.WASHING), WASHING);
		reg.addRecipes(getRecipes(R.COMPACTING), COMPACTING);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration reg) {
		reg.addGuiContainerHandler(MachineScreen.class, new IGuiContainerHandler<MachineScreen>() {
			@Override
			public Collection<IGuiClickableArea> getGuiClickableAreas(MachineScreen containerScreen) {
				IGuiClickableArea area = IGuiClickableArea.createBasic(78, 32, 28, 23, FluxMod.location(((MachineScreen<?>) containerScreen).getContainer().recipeType.toString()));
				return Collections.singleton(area);
			}
		});
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(Machine2For1Container.class, GRINDING, 0, 2, 3, 36);
		registration.addRecipeTransferHandler(Machine2For1Container.class, ALLOYING, 0, 2, 3, 36);
		registration.addRecipeTransferHandler(Machine2For1Container.class, WASHING, 0, 2, 3, 36);
		registration.addRecipeTransferHandler(Machine2For1Container.class, COMPACTING, 0, 2, 3, 36);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
		reg.addRecipeCatalyst(new ItemStack(B.GRINDING_MILL), GRINDING);
		reg.addRecipeCatalyst(new ItemStack(B.ALLOY_CASTER), ALLOYING);
		reg.addRecipeCatalyst(new ItemStack(B.WASHER), WASHING);
		reg.addRecipeCatalyst(new ItemStack(B.COMPACTOR), COMPACTING);
	}

	@SuppressWarnings({"ConstantConditions", "unchecked"})
	private static <C extends IInventory, T extends IRecipe<C>> Collection<T> getRecipes(IRecipeType<T> rtype) {
		ClientWorld world = Minecraft.getInstance().world;
		RecipeManager rm = world.getRecipeManager();
		Map<ResourceLocation, IRecipe<C>> rmap = rm.getRecipes(rtype);
		return (Collection<T>) rmap.values();
	}
}
