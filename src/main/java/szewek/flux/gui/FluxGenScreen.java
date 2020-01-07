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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import szewek.flux.FluxMod;
import szewek.flux.container.FluxGenContainer;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public final class FluxGenScreen extends ContainerScreen<FluxGenContainer> {
	private static final ResourceLocation BG_TEX = FluxMod.location("textures/gui/fluxgen.png");

	public FluxGenScreen(FluxGenContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		renderBackground(0);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.textureManager.bindTexture(BG_TEX);
		blit(guiLeft, guiTop, 0, 0, xSize, ySize);
		RenderSystem.pushMatrix();
		RenderSystem.translatef(guiLeft, guiTop, 0);
		drawGuiBar(86, 34, 90, 52, container.getWorkFill(), 0xFFA8A8A8, 0xFFEFEFEF, false);
		drawGuiBar(68, 63, 108, 71, container.getEnergyFill(), 0xFFA82121, 0xFFEF4242, true);
		RenderSystem.popMatrix();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = title.getFormattedText();
		font.drawString(s, (float)(xSize / 2 - font.getStringWidth(s) / 2), 5.0F, 0x404040);
		font.drawString(playerInventory.getDisplayName().getFormattedText(), 8.0F, (float)(ySize - 96 + 2), 0x404040);
		int mx = mouseX - guiLeft;
		int my = mouseY - guiTop;
		if (mx >= 68 && mx < 108 && my >= 63 && my < 71)
			renderTooltip(Arrays.asList(container.energyText(), container.genText()), mx, my);
		renderHoveredToolTip(mx, my);
	}

	private void drawGuiBar(int x, int y, int x2, int y2, float fill, int c1, int c2, boolean reverse) {
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(7425);
		//
		drawRectBatchOnly(x, y, x2, y2, 0xFF323232);
		x++;
		y++;
		x2--;
		y2--;
		int f;
		if (fill > 0) {
			drawGradientRectBatchOnly(x, y, x2, y2, c1, c2);
			if (x2 - x > y2 - y) {
				f = MathHelper.ceil((x2 - x) * fill);
				if (reverse) x += f; else x2 -= f;
			} else {
				f = MathHelper.ceil((y2 - y) * fill);
				if (reverse) y += f; else y2 -= f;
			}
		}
		drawRectBatchOnly(x, y, x2, y2, 0xFF151515);
		//
		RenderSystem.shadeModel(7424);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableTexture();
	}

	private void drawRectBatchOnly(double left, double top, double right, double bottom, int c) {
		double p = 0;
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
		float ya = (float) (c >> 24 & 255) / 255f;
		float yr = (float) (c >> 16 & 255) / 255f;
		float yg = (float) (c >> 8 & 255) / 255f;
		float yb = (float) (c & 255) / 255f;
		double z = getBlitOffset();
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder vb = tes.getBuffer();
		vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
		// func_225582_a_ == pos
		vb.func_225582_a_(left, bottom, z).func_227885_a_(yr, yg, yb, ya).endVertex();
		vb.func_225582_a_(right, bottom, z).func_227885_a_(yr, yg, yb, ya).endVertex();
		vb.func_225582_a_(right, top, z).func_227885_a_(yr, yg, yb, ya).endVertex();
		vb.func_225582_a_(left, top, z).func_227885_a_(yr, yg, yb, ya).endVertex();
		tes.draw();
	}

	private void drawGradientRectBatchOnly(double left, double top, double right, double bottom, int color1, int color2) {
		float ya = (float) (color1 >> 24 & 255) / 255f;
		float yr = (float) (color1 >> 16 & 255) / 255f;
		float yg = (float) (color1 >> 8 & 255) / 255f;
		float yb = (float) (color1 & 255) / 255f;
		float za = (float) (color2 >> 24 & 255) / 255f;
		float zr = (float) (color2 >> 16 & 255) / 255f;
		float zg = (float) (color2 >> 8 & 255) / 255f;
		float zb = (float) (color2 & 255) / 255f;
		double z = getBlitOffset();
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder vb = tes.getBuffer();
		vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
		// func_227885_a_ == color
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
