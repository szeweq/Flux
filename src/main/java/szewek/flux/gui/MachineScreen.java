package szewek.flux.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.recipebook.AbstractRecipeBookGui;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import szewek.flux.container.AbstractMachineContainer;

import java.util.Arrays;
import java.util.Collections;

public final class MachineScreen<T extends AbstractMachineContainer> extends ContainerScreen<T> implements IRecipeShownListener {
	private final AbstractRecipeBookGui recipeGui;
	private boolean recipeBookShown;
	private final ResourceLocation guiTexture;
	private static final ResourceLocation recipeTex = new ResourceLocation("textures/gui/recipe_button.png");

	public MachineScreen(T screenContainer, String filterName, PlayerInventory inv, ITextComponent titleIn, ResourceLocation guiTexture) {
		super(screenContainer, inv, titleIn);
		this.guiTexture = guiTexture;
		this.recipeGui = new MachineRecipeGui(screenContainer.recipeType, filterName);
	}

	public void init() {
		super.init();
		recipeBookShown = this.width < 379;
		recipeGui.init(width, height, minecraft, recipeBookShown, container);
		guiLeft = recipeGui.updateScreenPosition(recipeBookShown, width, xSize);
		addButton((Widget)(new ImageButton(guiLeft + 20, height / 2 - 49, 20, 18, 0, 0, 19, recipeTex, button -> {
			recipeGui.initSearchBar(recipeBookShown);
			recipeGui.toggleVisibility();
			guiLeft = recipeGui.updateScreenPosition(recipeBookShown, width, xSize);
			((ImageButton)button).setPosition(guiLeft + 20, height / 2 - 49);
		})));
	}

	public void tick() {
		super.tick();
		recipeGui.tick();
	}

	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();
		if (recipeGui.isVisible() && recipeBookShown) {
			drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			recipeGui.render(mouseX, mouseY, partialTicks);
		} else {
			recipeGui.render(mouseX, mouseY, partialTicks);
			super.render(mouseX, mouseY, partialTicks);
			recipeGui.renderGhostRecipe(guiLeft, guiTop, true, partialTicks);
		}

		renderHoveredToolTip(mouseX, mouseY);
		recipeGui.renderTooltip(guiLeft, guiTop, mouseX, mouseY);
	}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = title.getFormattedText();
		font.drawString(s, (float)(xSize / 2 - font.getStringWidth(s) / 2), 6.0F, 0x404040);
		ITextComponent var8 = playerInventory.getDisplayName();
		font.drawString(var8.getFormattedText(), 8.0F, (float)(ySize - 96 + 2), 0x404040);
		int mx = mouseX - guiLeft;
		int my = mouseY - guiTop;
		if (151 <= mx && 168 >= mx && 16 <= my && 69 >= my) {
			renderTooltip(Collections.singletonList(container.energyText()), mx, my);
		}

	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(guiTexture);
		int i = guiLeft;
		int j = guiTop;
		blit(i, j, 0, 0, xSize, ySize);

		int n = container.energyScaled();
		if (n > 0) {
			blit(i + 152, j + 71 - n, 176, 71 - n, 16, n - 1);
		}

		n = container.processScaled();
		if (n > 0) {
			blit(i + 79, j + 34, 176, 0, n + 1, 16);
		}

	}

	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return recipeGui.mouseClicked(mouseX, mouseY, mouseButton) || (recipeBookShown && recipeGui.isVisible() || super.mouseClicked(mouseX, mouseY, mouseButton));
	}

	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		recipeGui.slotClicked(slotIn);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return !recipeGui.keyPressed(keyCode, scanCode, modifiers) && super.keyPressed(keyCode, scanCode, modifiers);
	}

	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
		boolean flag = mouseX < (double)guiLeftIn || mouseY < (double)guiTopIn || mouseX >= (double)(guiLeftIn + xSize) || mouseY >= (double)(guiTopIn + ySize);
		return recipeGui.func_195604_a(mouseX, mouseY, guiLeft, guiTop, xSize, ySize, mouseButton) && flag;
	}

	public boolean charTyped(char c, int modifiers) {
		return recipeGui.charTyped(c, modifiers) || super.charTyped(c, modifiers);
	}

	public void recipesUpdated() {
		recipeGui.recipesUpdated();
	}

	public RecipeBookGui getRecipeGui() {
		return recipeGui;
	}

	public void removed() {
		recipeGui.removed();
		super.removed();
	}

	public static <T extends AbstractMachineContainer> ScreenManager.IScreenFactory<T, MachineScreen<T>> make(final String filterName, String guiName) {
		final ResourceLocation texGui = new ResourceLocation("flux", "textures/gui/" + guiName + ".png");
		return (container, inv, title) -> new MachineScreen<>(container, filterName, inv, title, texGui);
	}
}
