package szewek.flux.gui;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import szewek.flux.container.AbstractMachineContainer;

import java.util.Collections;

@OnlyIn(Dist.CLIENT)
public class MachineScreen<T extends AbstractMachineContainer> extends ContainerScreen<T> implements IRecipeShownListener {
	private static final ResourceLocation recipeTex = new ResourceLocation("textures/gui/recipe_button.png");
	public final AbstractRecipeBookGui recipeGui;
	private final ResourceLocation guiTexture;
	private boolean recipeBookShown;

	public static <M extends AbstractMachineContainer> ScreenManager.IScreenFactory<M, MachineScreen<M>> make(final String filterName, final String guiName) {
		final ResourceLocation texGui = new ResourceLocation("flux", "textures/gui/" + guiName + ".png");
		return (container, inv, title) -> new MachineScreen<M>(container, filterName, inv, title, texGui);
	}

	public MachineScreen(T screenContainer, String filterName, PlayerInventory inv, ITextComponent titleIn, ResourceLocation guiTextureIn) {
		super(screenContainer, inv, titleIn);
		recipeGui = new MachineRecipeGui(screenContainer.recipeType, filterName);
		guiTexture = guiTextureIn;
	}

	@Override
	public void init() {
		super.init();
		recipeBookShown = width < 379;
		recipeGui.init(width, height, minecraft, recipeBookShown, container);
		guiLeft = recipeGui.updateScreenPosition(recipeBookShown, width, xSize);
		addButton((new ImageButton(guiLeft + 20, height / 2 - 49, 20, 18, 0, 0, 19, recipeTex, (button) -> {
			recipeGui.initSearchBar(recipeBookShown);
			recipeGui.toggleVisibility();
			guiLeft = recipeGui.updateScreenPosition(recipeBookShown, width, xSize);
			((ImageButton)button).setPosition(guiLeft + 20, height / 2 - 49);
		})));
	}

	@Override
	public void tick() {
		super.tick();
		this.recipeGui.tick();
	}

	@Override
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

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = title.getFormattedText();
		font.drawString(s, (float)(xSize / 2 - this.font.getStringWidth(s) / 2), 6.0F, 0x404040);
		font.drawString(playerInventory.getDisplayName().getFormattedText(), 8.0F, (float)(ySize - 96 + 2), 0x404040);
		int mx = mouseX - guiLeft;
		int my = mouseY - guiTop;
		if (mx >= 151 && mx < 169 && my >= 16 && my < 70)
			renderTooltip(Collections.singletonList(container.energyText()), mx, my);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(this.guiTexture);
		int i = this.guiLeft;
		int j = this.guiTop;
		this.blit(i, j, 0, 0, xSize, ySize);
		int n = container.energyScaled();
		if (n > 0) {
			this.blit(i + 152, j + 71 - n, 176, 71 - n, 16, n - 1);
		}
		n = container.processScaled();
		if (n > 0) {
			this.blit(i + 79, j + 34, 176, 0, n + 1, 16);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (this.recipeGui.mouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
		} else return this.recipeBookShown && this.recipeGui.isVisible() || super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		recipeGui.slotClicked(slotIn);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return !recipeGui.keyPressed(keyCode, scanCode, modifiers) && super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
		boolean flag = mouseX < (double)guiLeftIn || mouseY < (double)guiTopIn || mouseX >= (double)(guiLeftIn + xSize) || mouseY >= (double)(guiTopIn + ySize);
		return recipeGui.func_195604_a(mouseX, mouseY, guiLeft, guiTop, xSize, ySize, mouseButton) && flag;
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
	public RecipeBookGui getRecipeGui() {
		return recipeGui;
	}

	@Override
	public void removed() {
		recipeGui.removed();
		super.removed();
	}
}
