package szewek.flux.compat.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;
import szewek.flux.recipe.AbstractMachineRecipe;

import javax.annotation.ParametersAreNonnullByDefault;

import static szewek.flux.Flux.MODID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MachineCategory<T extends AbstractMachineRecipe> implements IRecipeCategory<T> {
	private final IDrawable
			background,
			icon;
	private final String localizedName;
	protected final IDrawableAnimated arrow;
	private final ResourceLocation uid;
	private final Class<T> cl;

	public MachineCategory(String resUid, Class<T> tClass, IDrawable bg, IDrawable icon, IDrawableAnimated arrow) {
		uid = F.loc(resUid);
		localizedName = I18n.get("gui.flux.jei.category." + resUid);
		cl = tClass;
		background = bg;
		this.icon = icon;
		this.arrow = arrow;
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
	public void setIngredients(T recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, T recipe, IIngredients ingredients) {
		JEIFluxPlugin.setMachineRecipeLayout(recipeLayout.getItemStacks(), ingredients);
	}

	@Override
	public void draw(T recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		arrow.draw(matrixStack, 24, 9);
		float experience = recipe.experience;
		if (experience > 0.0F) {
			String experienceString = I18n.get("gui.jei.category.smelting.experience", experience);
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(experienceString);
			fontRenderer.draw(matrixStack, experienceString, (float)(background.getWidth() - stringWidth), 0.0F, 0xff808080);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return uid;
	}

	@Override
	public Class<? extends T> getRecipeClass() {
		return cl;
	}

	static final class Builder {
		private final IGuiHelper guiHelper;
		final IDrawableAnimated arrow;

		Builder(IGuiHelper guiHelper) {
			this.guiHelper = guiHelper;
			arrow = guiHelper.drawableBuilder(JEIFluxPlugin.GUI_VANILLA, 82, 128, 24, 17).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
		}

		final <T extends AbstractMachineRecipe> MachineCategory<T> build(String resUid, String guiId, Class<T> tClass, Block block) {
			IDrawable bg = guiHelper.createDrawable(new ResourceLocation(MODID, "textures/gui/" + guiId + ".png"), 55, 25, 82, 36);
			IDrawable icon = guiHelper.createDrawableIngredient(new ItemStack(block));
			return new MachineCategory<>(resUid, tClass, bg, icon, arrow);
		}
	}
}
