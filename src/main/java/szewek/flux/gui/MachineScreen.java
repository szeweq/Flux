package szewek.flux.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.recipebook.AbstractRecipeBookGui;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import szewek.flux.container.AbstractMachineContainer;

@OnlyIn(Dist.CLIENT)
public class MachineScreen extends ContainerScreen<AbstractMachineContainer> implements IRecipeShownListener {
	private final AbstractRecipeBookGui recipeGui;
	private boolean recipeBookShown;
	private final ResourceLocation guiTexture;
	private static final ResourceLocation recipeTex = new ResourceLocation("textures/gui/recipe_button.png");
	private static final ITextComponent compatInfo = new TranslationTextComponent("flux.recipe_compat");

	public MachineScreen(AbstractMachineContainer screenContainer, String filterName, PlayerInventory inv, ITextComponent titleIn, ResourceLocation guiTexture) {
		super(screenContainer, inv, titleIn);
		this.guiTexture = guiTexture;
		recipeGui = new MachineRecipeGui(screenContainer.recipeType, filterName);
	}

	@Override
	public void init() {
		super.init();
		recipeBookShown = width < 379;
		recipeGui.init(width, height, minecraft, recipeBookShown, menu);
		leftPos = recipeGui.updateScreenPosition(recipeBookShown, width, imageWidth);
		addButton(new ImageButton(leftPos + 20, height / 2 - 49, 20, 18, 0, 0, 19, recipeTex, button -> {
			recipeGui.initVisuals(recipeBookShown);
			recipeGui.toggleVisibility();
			leftPos = recipeGui.updateScreenPosition(recipeBookShown, width, imageWidth);
			((ImageButton)button).setPosition(leftPos + 20, height / 2 - 49);
		}));
	}

	@Override
	public void tick() {
		super.tick();
		recipeGui.tick();
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack);
		if (recipeGui.isVisible() && recipeBookShown) {
			renderBg(matrixStack, partialTicks, mouseX, mouseY);
			recipeGui.render(matrixStack, mouseX, mouseY, partialTicks);
		} else {
			recipeGui.render(matrixStack, mouseX, mouseY, partialTicks);
			super.render(matrixStack, mouseX, mouseY, partialTicks);
			// RENDER GHOST RECIPE
			recipeGui.renderGhostRecipe(matrixStack, leftPos, topPos, true, partialTicks);
		}
		// RENDER TOOLTIPS
		renderTooltip(matrixStack, mouseX, mouseY);
		recipeGui.renderTooltip(matrixStack, leftPos, topPos, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
		String s = title.getString();
		font.draw(matrixStack, s, (float)(imageWidth / 2 - font.width(s) / 2), 6F, 0x404040);
		ITextComponent var8 = inventory.getDisplayName();
		font.draw(matrixStack, var8.getString(), 8F, (float)(imageHeight - 96 + 2), 0x404040);
		int mx = mouseX - leftPos;
		int my = mouseY - topPos;
		if (151 <= mx && 168 >= mx && 16 <= my && 69 >= my) {
			renderComponentTooltip(matrixStack, menu.energyText(), mx, my);
		}
		if (menu.isCompatRecipe()) {
			font.draw(matrixStack, "!", 82F, 24F, 0xFF0000);
			if (80 <= mx && 84 >= mx && 24 <= my && 32 >= my) {
				renderTooltip(matrixStack, compatInfo, mx, my);
			}
		}
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bind(guiTexture);
		int i = leftPos;
		int j = topPos;
		blit(matrixStack, i, j, 0, 0, imageWidth, imageHeight);

		int n = menu.energyScaled();
		if (n > 0) {
			blit(matrixStack, i + 152, j + 71 - n, 176, 71 - n, 16, n - 1);
		}

		n = menu.processScaled();
		if (n > 0) {
			blit(matrixStack, i + 79, j + 34, 176, 0, n + 1, 16);
		}

	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return recipeGui.mouseClicked(mouseX, mouseY, mouseButton) || recipeBookShown && recipeGui.isVisible() || super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.slotClicked(slotIn, slotId, mouseButton, type);
		recipeGui.slotClicked(slotIn);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return !recipeGui.keyPressed(keyCode, scanCode, modifiers) && super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
		boolean flag = mouseX < (double)guiLeftIn || mouseY < (double)guiTopIn || mouseX >= (double)(guiLeftIn + imageWidth) || mouseY >= (double)(guiTopIn + imageHeight);
		return recipeGui.hasClickedOutside(mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight, mouseButton) && flag;
	}

	@Override
	public boolean charTyped(char c, int modifiers) {
		return recipeGui.charTyped(c, modifiers) || super.charTyped(c, modifiers);
	}

	@Override
	public void recipesUpdated() {
		recipeGui.recipesUpdated();
	}

	@Override
	public RecipeBookGui getRecipeBookComponent() {
		return recipeGui;
	}

	@Override
	public void onClose() {
		recipeGui.removed();
		super.onClose();
	}

	public static ScreenManager.IScreenFactory<AbstractMachineContainer, MachineScreen> make(final String filterName, String guiName) {
		final ResourceLocation texGui = new ResourceLocation("flux", "textures/gui/" + guiName + ".png");
		return (container, inv, title) -> new MachineScreen(container, filterName, inv, title, texGui);
	}
}
