package szewek.flux.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;
import szewek.flux.F.B;
import szewek.flux.F.R;
import szewek.flux.container.AbstractMachineContainer;
import szewek.flux.container.CopierContainer;
import szewek.flux.container.Machine2For1Container;
import szewek.flux.gui.MachineScreen;
import szewek.flux.recipe.AlloyingRecipe;
import szewek.flux.recipe.CompactingRecipe;
import szewek.flux.recipe.GrindingRecipe;
import szewek.flux.recipe.WashingRecipe;
import szewek.flux.util.Toolset;

import java.util.*;
import java.util.stream.Collectors;

import static szewek.flux.Flux.MODID;

@JeiPlugin
public class JEIFluxPlugin implements IModPlugin {
	static final ResourceLocation
			FLUXGEN = F.loc("fluxgen"),
			GRINDING = F.loc("grinding"),
			ALLOYING = F.loc("alloying"),
			WASHING = F.loc("washing"),
			COMPACTING = F.loc("compacting"),
			COPYING = F.loc("copying"),
			GUI_VANILLA = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");

	@Override
	public ResourceLocation getPluginUid() {
		return F.loc("jei");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration reg) {
		IJeiHelpers jeiHelpers = reg.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		MachineCategory.Builder mcb = new MachineCategory.Builder(guiHelper);

		MachineCategory<GrindingRecipe> grinding = mcb.build("grinding", "grinding_mill", GrindingRecipe.class, B.GRINDING_MILL);
		MachineCategory<AlloyingRecipe> alloying = mcb.build("alloying", "alloy_caster", AlloyingRecipe.class, B.ALLOY_CASTER);
		MachineCategory<WashingRecipe> washing = mcb.build("washing", "washer", WashingRecipe.class, B.WASHER);
		MachineCategory<CompactingRecipe> compacting = mcb.build("compacting", "compactor", CompactingRecipe.class, B.COMPACTOR);
		CopierCategory copying = new CopierCategory(guiHelper, B.COPIER, mcb.arrow);
		reg.addRecipeCategories(grinding, alloying, washing, compacting, copying);

		reg.addRecipeCategories(new FluxGenCategory(guiHelper));
	}

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		reg.addRecipes(getRecipes(R.GRINDING), GRINDING);
		reg.addRecipes(getRecipes(R.ALLOYING), ALLOYING);
		reg.addRecipes(getRecipes(R.WASHING), WASHING);
		reg.addRecipes(getRecipes(R.COMPACTING), COMPACTING);
		reg.addRecipes(getRecipes(R.COPYING), COPYING);

		IVanillaRecipeFactory vanillaRecipeFactory = reg.getVanillaRecipeFactory();
		reg.addRecipes(getFluxRepairRecipes(vanillaRecipeFactory), VanillaRecipeCategoryUid.ANVIL);
		reg.addRecipes(FluxGenCategory.Product.getAll(), FLUXGEN);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration reg) {
		reg.addGuiContainerHandler(MachineScreen.class, new MachineScreenHandler());
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(Machine2For1Container.class, GRINDING, 0, 2, 4, 36);
		registration.addRecipeTransferHandler(Machine2For1Container.class, ALLOYING, 0, 2, 4, 36);
		registration.addRecipeTransferHandler(Machine2For1Container.class, WASHING, 0, 2, 4, 36);
		registration.addRecipeTransferHandler(Machine2For1Container.class, COMPACTING, 0, 2, 4, 36);
		registration.addRecipeTransferHandler(CopierContainer.class, COPYING, 0, 2, 4, 36);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
		reg.addRecipeCatalyst(new ItemStack(B.GRINDING_MILL), GRINDING);
		reg.addRecipeCatalyst(new ItemStack(B.ALLOY_CASTER), ALLOYING);
		reg.addRecipeCatalyst(new ItemStack(B.WASHER), WASHING);
		reg.addRecipeCatalyst(new ItemStack(B.COMPACTOR), COMPACTING);
		reg.addRecipeCatalyst(new ItemStack(B.COPIER), COPYING);
		reg.addRecipeCatalyst(new ItemStack(B.FLUXGEN), FLUXGEN);
	}

	@SuppressWarnings({"ConstantConditions", "unchecked"})
	private static <C extends IInventory, T extends IRecipe<C>> Collection<T> getRecipes(IRecipeType<T> rtype) {
		ClientWorld world = Minecraft.getInstance().world;
		RecipeManager rm = world.getRecipeManager();
		Map<ResourceLocation, IRecipe<C>> rmap = rm.getRecipes(rtype);
		return (Collection<T>) rmap.values();
	}

	private static List<Object> getFluxRepairRecipes(IVanillaRecipeFactory vanillaRecipeFactory) {
		final Toolset[] toolsets = {F.I.BRONZE_TOOLS, F.I.STEEL_TOOLS};
		final List<Object> recipes = new ArrayList<>();
		for (Toolset tools : toolsets) {
			List<ItemStack> repairItems = tools.tier.repairMaterialTag.getAllElements().stream().map(ItemStack::new).collect(Collectors.toList());
			if (repairItems.isEmpty()) {
				repairItems = Collections.singletonList(new ItemStack(tools.tier.material));
			}
			Iterator<ItemStack> toolItems = Arrays.stream(tools.allTools()).map(ItemStack::new).iterator();
			while (toolItems.hasNext()) {
				ItemStack stack = toolItems.next();
				ItemStack damaged1 = stack.copy();
				damaged1.setDamage(damaged1.getMaxDamage());
				ItemStack damaged2 = stack.copy();
				damaged2.setDamage(damaged2.getMaxDamage() * 3 / 4);
				ItemStack damaged3 = stack.copy();
				damaged3.setDamage(damaged3.getMaxDamage() * 2 / 4);
				Object repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(damaged1, repairItems, Collections.singletonList(damaged2));
				Object repairWithSame = vanillaRecipeFactory.createAnvilRecipe(damaged2, Collections.singletonList(damaged2), Collections.singletonList(damaged3));
				recipes.add(repairWithMaterial);
				recipes.add(repairWithSame);
			}
		}
		return recipes;
	}

	static void setMachineRecipeLayout(IGuiItemStackGroup guiItemStacks, IIngredients ingredients) {
		guiItemStacks.init(0, true, 0, 0);
		guiItemStacks.init(1, true, 0, 18);
		guiItemStacks.init(2, false, 60, 9);
		guiItemStacks.set(ingredients);
	}

	private static class MachineScreenHandler implements IGuiContainerHandler<MachineScreen> {
		@Override
		public Collection<IGuiClickableArea> getGuiClickableAreas(MachineScreen containerScreen, double mouseX, double mouseY) {
			IGuiClickableArea area = IGuiClickableArea.createBasic(78, 32, 28, 23, F.loc(containerScreen.getContainer().recipeType.toString()));
			return Collections.singleton(area);
		}
	}
}
