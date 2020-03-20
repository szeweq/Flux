package szewek.flux.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
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

import static szewek.flux.FluxMod.MODID;

@OnlyIn(Dist.CLIENT)
public final class FluxGenScreen extends ContainerScreen<FluxGenContainer> implements HoverSet.HoverListener {
	private static final ResourceLocation BG_TEX = new ResourceLocation(MODID, "textures/gui/fluxgen.png");
	private static final GuiBar
			workFillBar = new GuiBar(86, 34, 90, 52, 0xffa8a8a8, 0xffefefef, false),
			energyFillBar = new GuiBar(68, 63, 108, 71, 0xffa82121, 0xffef4242, true);
	private static final GuiRect
			hoverHotFluid = new GuiRect(47, 15, 63, 71),
			hoverColdFluid = new GuiRect(113, 15, 129, 71);
	private final HoverSet tooltips = new HoverSet(this);

	public FluxGenScreen(FluxGenContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		tooltips.addAll(energyFillBar, hoverHotFluid, hoverColdFluid);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.renderBackground(0);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		assert minecraft != null;
		minecraft.getTextureManager().bindTexture(BG_TEX);
		blit(guiLeft, guiTop, 0, 0, xSize, ySize);
		RenderSystem.pushMatrix();
		RenderSystem.translatef(guiLeft, guiTop, 0.0F);
		workFillBar.draw(container.getWorkFill(), getBlitOffset());
		energyFillBar.draw(container.getEnergyFill(), getBlitOffset());
		RenderSystem.popMatrix();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = title.getFormattedText();
		this.font.drawString(s, (float)(xSize / 2 - font.getStringWidth(s) / 2), 5.0F, 0x404040);
		font.drawString(playerInventory.getDisplayName().getFormattedText(), 8.0F, ySize - 96 + 2, 0x404040);
		int mx = mouseX - guiLeft;
		int my = mouseY - guiTop;
		DrawUtils.drawFluidStack(47, 15, 16, 56, container.getHotFluid(), 4000, getBlitOffset());
		DrawUtils.drawFluidStack(113, 15, 16, 56, container.getColdFluid(), 4000, getBlitOffset());
		tooltips.checkCoords(mx, my);
		renderHoveredToolTip(mx, my);
	}

	@Override
	public void onHover(GuiRect rect, int mx, int my) {
		List<String> l;
		if (rect == energyFillBar) {
			l = Arrays.asList(container.energyText(), container.genText());
		} else if (rect == hoverHotFluid) {
			FluidStack hot = container.getHotFluid();
			l = Arrays.asList(I18n.format(hot.getTranslationKey()), hot.getAmount() + " mB");
		} else if (rect == hoverColdFluid) {
			FluidStack cold = container.getColdFluid();
			l = Arrays.asList(I18n.format(cold.getTranslationKey()), cold.getAmount() + " mB");
		} else {
			return;
		}
		renderTooltip(l, mx, my);
	}

}
