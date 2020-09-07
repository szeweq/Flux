package szewek.flux.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import szewek.fl.gui.DrawUtils;
import szewek.fl.gui.GuiBar;
import szewek.fl.gui.GuiRect;
import szewek.fl.gui.HoverSet;
import szewek.flux.container.FluxGenContainer;

import java.util.Arrays;
import java.util.List;

import static szewek.flux.Flux.MODID;

@OnlyIn(Dist.CLIENT)
public final class FluxGenScreen extends ContainerScreen<FluxGenContainer> implements HoverSet.HoverListener {
	private static final ResourceLocation BG_TEX = new ResourceLocation(MODID, "textures/gui/fluxgen.png");
	private static final GuiBar
			workFillBar = new GuiBar(new GuiRect(86, 34, 90, 52), 0xffa8a8a8, 0xffefefef, false),
			energyFillBar = new GuiBar(new GuiRect(68, 63, 108, 71), 0xffa82121, 0xffef4242, true);
	private static final GuiRect
			hoverHotFluid = new GuiRect(47, 15, 63, 71),
			hoverColdFluid = new GuiRect(113, 15, 129, 71);
	private final HoverSet tooltips = new HoverSet(this);

	public FluxGenScreen(FluxGenContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		tooltips.addAll(energyFillBar.rect, hoverHotFluid, hoverColdFluid);
	}

	@Override
	protected void drawBackground(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		renderBackground(matrixStack, 0);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		assert client != null;
		client.getTextureManager().bindTexture(BG_TEX);
		drawTexture(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
		matrixStack.push();
		matrixStack.translate(guiLeft, guiTop, 0.0F);
		workFillBar.draw(matrixStack, container.getWorkFill(), getZOffset());
		energyFillBar.draw(matrixStack, container.getEnergyFill(), getZOffset());
		matrixStack.pop();
	}

	@Override
	protected void drawForeground(MatrixStack matrixStack, int mouseX, int mouseY) {
		final String s = title.getString();
		textRenderer.draw(matrixStack, s, (float)((xSize - textRenderer.getStringWidth(s)) / 2), 5.0F, 0x404040);
		textRenderer.draw(matrixStack, playerInventory.getDisplayName().getString(), 8.0F, ySize - 96 + 2, 0x404040);
		int mx = mouseX - guiLeft;
		int my = mouseY - guiTop;
		final Matrix4f matrix = matrixStack.peek().getModel();//.getLast().getMatrix();
		DrawUtils.drawFluidStack(matrix, hoverHotFluid, container.getHotFluid(), 4000, getZOffset());
		DrawUtils.drawFluidStack(matrix, hoverColdFluid, container.getColdFluid(), 4000, getZOffset());
		tooltips.checkCoords(matrixStack, mx, my);
		drawMouseoverTooltip(matrixStack, mx, my);
	}

	@Override
	public void onHover(GuiRect rect, MatrixStack matrixStack, int mx, int my) {
		List<ITextComponent> l;
		if (rect == energyFillBar.rect) {
			l = Arrays.asList(container.energyText(), container.genText());
		} else {
			FluidStack fluid;
			if (rect == hoverHotFluid) {
				fluid = container.getHotFluid();
			} else if (rect == hoverColdFluid) {
				fluid = container.getColdFluid();
			} else {
				return;
			}
			l = Arrays.asList(
					fluid.getDisplayName(),
					new StringTextComponent(fluid.getAmount() + " mB")
			);
		}
		renderTooltip(matrixStack, l, mx, my);
	}

}
