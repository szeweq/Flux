package szewek.flux.compat.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;
import szewek.flux.recipe.CopyingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CopierCategory implements IRecipeCategory<CopyingRecipe> {
	private static final ResourceLocation TEX = F.loc("textures/gui/copier.png");
	private final IDrawable
			background,
			icon;
	private final String localizedName;
	protected final IDrawableAnimated arrow;

	public CopierCategory(IGuiHelper guiHelper, Block icon, IDrawableAnimated arrow) {
		background = guiHelper.createDrawable(TEX, 55, 25, 82, 36);
		this.icon = guiHelper.createDrawableIngredient(new ItemStack(icon));
		localizedName = I18n.get("gui.flux.jei.category.copying");
		this.arrow = arrow;
	}

	@Override
	public ResourceLocation getUid() {
		return JEIFluxPlugin.COPYING;
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
	public void draw(CopyingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		arrow.draw(matrixStack, 24, 9);
	}

	@Override
	public void setIngredients(CopyingRecipe copyingRecipe, IIngredients ingredients) {
		ingredients.setInputIngredients(copyingRecipe.getIngredients());
		ingredients.setOutputLists(VanillaTypes.ITEM,
				Collections.singletonList(Arrays.asList(copyingRecipe.getSource().getItems())));
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CopyingRecipe copyingRecipe, IIngredients ingredients) {
		JEIFluxPlugin.setMachineRecipeLayout(recipeLayout.getItemStacks(), ingredients);
	}
}
