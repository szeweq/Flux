package szewek.flux.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import szewek.flux.FluxMod;
import szewek.flux.container.FluxGenContainer;

import java.util.Arrays;

public final class FluxGenScreen extends ContainerScreen<FluxGenContainer> {
	private static final ResourceLocation BG_TEX = FluxMod.location("textures/gui/fluxgen.png");

	public FluxGenScreen(FluxGenContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.renderBackground(0);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		assert minecraft != null;
		minecraft.getTextureManager().bindTexture(BG_TEX);
		this.blit(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		RenderSystem.pushMatrix();
		RenderSystem.translatef((float)this.guiLeft, (float)this.guiTop, 0.0F);
		this.drawGuiBar(86, 34, 90, 52, container.getWorkFill(), -5723992, -1052689, false);
		this.drawGuiBar(68, 63, 108, 71, container.getEnergyFill(), -5758687, -1097150, true);
		RenderSystem.popMatrix();
	}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = title.getFormattedText();
		this.font.drawString(s, (float)(xSize / 2 - font.getStringWidth(s) / 2), 5.0F, 4210752);
		font.drawString(playerInventory.getDisplayName().getFormattedText(), 8.0F, (float)(ySize - 96 + 2), 4210752);
		int mx = mouseX - guiLeft;
		int my = mouseY - guiTop;
		if (68 <= mx && 107 >= mx && 63 <= my && 70 >= my) {
			renderTooltip(Arrays.asList(container.energyText(), container.genText()), mx, my);
		}

		renderHoveredToolTip(mx, my);
	}

	private void drawGuiBar(int x, int y, int x2, int y2, float fill, int c1, int c2, boolean reverse) {
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(7425);
		this.drawRectBatchOnly(x, y, x2, y2, 0xff323232);
		x = x + 1;
		y = y + 1;
		x2 = x2 - 1;
		y2 = y2 - 1;
		if (fill > (float)0) {
			this.drawGradientRectBatchOnly(x, y, x2, y2, c1, c2);
			int f;
			if (x2 - x > y2 - y) {
				f = MathHelper.ceil((float)(x2 - x) * fill);
				if (reverse) {
					x += f;
				} else {
					x2 -= f;
				}
			} else {
				f = MathHelper.ceil((float)(y2 - y) * fill);
				if (reverse) {
					y += f;
				} else {
					y2 -= f;
				}
			}
		}

		this.drawRectBatchOnly(x, y, x2, y2, 0xff151515);
		RenderSystem.shadeModel(7424);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableTexture();
	}

	private void drawRectBatchOnly(double left, double top, double right, double bottom, int c) {
		double p;
		if (left < right) {
			p = left;
			left = right;
			right = p;
		}

		if (top < bottom) {
			p = top;
			top = bottom;
			bottom = p;
		}

		float ya = (float)(c >> 24 & 255) / 255.0F;
		float yr = (float)(c >> 16 & 255) / 255.0F;
		float yg = (float)(c >> 8 & 255) / 255.0F;
		float yb = (float)(c & 255) / 255.0F;
		double z = this.getBlitOffset();
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder vb = tes.getBuffer();
		vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
		vb.func_225582_a_(left, bottom, z).func_227885_a_(yr, yg, yb, ya).endVertex();
		vb.func_225582_a_(right, bottom, z).func_227885_a_(yr, yg, yb, ya).endVertex();
		vb.func_225582_a_(right, top, z).func_227885_a_(yr, yg, yb, ya).endVertex();
		vb.func_225582_a_(left, top, z).func_227885_a_(yr, yg, yb, ya).endVertex();
		tes.draw();
	}

	private void drawGradientRectBatchOnly(double left, double top, double right, double bottom, int color1, int color2) {
		float ya = (float)(color1 >> 24 & 255) / 255.0F;
		float yr = (float)(color1 >> 16 & 255) / 255.0F;
		float yg = (float)(color1 >> 8 & 255) / 255.0F;
		float yb = (float)(color1 & 255) / 255.0F;
		float za = (float)(color2 >> 24 & 255) / 255.0F;
		float zr = (float)(color2 >> 16 & 255) / 255.0F;
		float zg = (float)(color2 >> 8 & 255) / 255.0F;
		float zb = (float)(color2 & 255) / 255.0F;
		double z = this.getBlitOffset();
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder vb = tes.getBuffer();
		vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
		if (right - left > bottom - top) {
			vb.func_225582_a_(right, top, z).func_227885_a_(yr, yg, yb, ya).endVertex();
			vb.func_225582_a_(left, top, z).func_227885_a_(zr, zg, zb, za).endVertex();
			vb.func_225582_a_(left, bottom, z).func_227885_a_(zr, zg, zb, za).endVertex();
			vb.func_225582_a_(right, bottom, z).func_227885_a_(yr, yg, yb, ya).endVertex();
		} else {
			vb.func_225582_a_(right, top, z).func_227885_a_(yr, yg, yb, ya).endVertex();
			vb.func_225582_a_(left, top, z).func_227885_a_(yr, yg, yb, ya).endVertex();
			vb.func_225582_a_(left, bottom, z).func_227885_a_(zr, zg, zb, za).endVertex();
			vb.func_225582_a_(right, bottom, z).func_227885_a_(zr, zg, zb, za).endVertex();
		}
		tes.draw();
	}
}
