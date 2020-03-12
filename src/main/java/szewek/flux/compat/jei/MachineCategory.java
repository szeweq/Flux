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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import szewek.flux.FluxMod;
import szewek.flux.recipe.AbstractMachineRecipe;

import javax.annotation.ParametersAreNonnullByDefault;

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

	public MachineCategory(IGuiHelper guiHelper, String resUid, String guiId, Class<T> tClass, Block icon, int regularProcessTime) {
		uid = FluxMod.location(resUid);
		cl = tClass;
		background = guiHelper.createDrawable(FluxMod.location("textures/gui/" + guiId + ".png"), 55, 25, 82, 36);
		this.icon = guiHelper.createDrawableIngredient(new ItemStack(icon));
		localizedName = I18n.format("gui.flux.jei.category." + resUid);
		arrow = guiHelper.drawableBuilder(new ResourceLocation("jei", "textures/gui/gui_vanilla.png"), 82, 128, 24, 17).buildAnimated(regularProcessTime, IDrawableAnimated.StartDirection.LEFT, false);
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
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, T recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 0);
		guiItemStacks.init(1, true, 0, 18);
		guiItemStacks.init(2, false, 60, 9);
		guiItemStacks.set(ingredients);
	}

	@Override
	public void draw(T recipe, double mouseX, double mouseY) {
		this.arrow.draw(24, 9);
		float experience = recipe.experience;
		if (experience > 0.0F) {
			String experienceString = I18n.format("gui.jei.category.smelting.experience", experience);
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer fontRenderer = minecraft.fontRenderer;
			int stringWidth = fontRenderer.getStringWidth(experienceString);
			fontRenderer.drawString(experienceString, (float)(background.getWidth() - stringWidth), 0.0F, 0xff808080);
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
}
