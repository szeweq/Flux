package szewek.flux.compat.jei;

import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import szewek.flux.recipe.CopyingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;

import static szewek.flux.FluxMod.MODID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CopierCategory implements IRecipeCategory<CopyingRecipe> {
	private final IDrawable
			background,
			icon;
	private final String localizedName;
	protected final IDrawableAnimated arrow;
	private final ResourceLocation uid;

	public CopierCategory(IGuiHelper guiHelper, Block icon, int processTime) {
		uid = new ResourceLocation(MODID, "copying");
		background = guiHelper.createDrawable(new ResourceLocation(MODID, "textures/gui/copier.png"), 55, 25, 82, 36);
		this.icon = guiHelper.createDrawableIngredient(new ItemStack(icon));
		localizedName = I18n.format("gui.flux.jei.category.copying");
		arrow = guiHelper.drawableBuilder(new ResourceLocation("jei", "textures/gui/gui_vanilla.png"), 82, 128, 24, 17).buildAnimated(processTime, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public ResourceLocation getUid() {
		return uid;
	}

	@Override
	public Class<? extends CopyingRecipe> getRecipeClass() {
		return CopyingRecipe.class;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public void draw(CopyingRecipe recipe, double mouseX, double mouseY) {
		arrow.draw(24, 9);
	}

	@Override
	public void setIngredients(CopyingRecipe copyingRecipe, IIngredients ingredients) {
		ingredients.setInputIngredients(copyingRecipe.getIngredients());
		ingredients.setOutputLists(VanillaTypes.ITEM,
				Collections.singletonList(Arrays.asList(copyingRecipe.getSource().getMatchingStacks())));
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CopyingRecipe copyingRecipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 0);
		guiItemStacks.init(1, true, 0, 18);
		guiItemStacks.init(2, false, 60, 9);
		guiItemStacks.set(ingredients);
	}
}
