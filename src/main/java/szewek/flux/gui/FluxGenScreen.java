package szewek.flux.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.gui.GuiUtils;
import szewek.flux.FluxMod;
import szewek.flux.container.FluxGenContainer;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
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
		blit(guiLeft, guiTop, 0, 0, xSize, ySize);
		RenderSystem.pushMatrix();
		RenderSystem.translatef((float) guiLeft, (float) guiTop, 0.0F);
		drawGuiBar(86, 34, 90, 52, container.getWorkFill(), -5723992, -1052689, false);
		drawGuiBar(68, 63, 108, 71, container.getEnergyFill(), -5758687, -1097150, true);
		RenderSystem.popMatrix();
	}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = title.getFormattedText();
		this.font.drawString(s, (float)(xSize / 2 - font.getStringWidth(s) / 2), 5.0F, 0x404040);
		font.drawString(playerInventory.getDisplayName().getFormattedText(), 8.0F, (float)(ySize - 96 + 2), 0x404040);
		int mx = mouseX - guiLeft;
		int my = mouseY - guiTop;
		FluidStack hot = container.getHotFluid();
		FluidStack cold = container.getColdFluid();
		drawFluidStack(47, 15, 16, 56, hot, 4000);
		drawFluidStack(113, 15, 16, 56, cold, 4000);
		if (68 <= mx && 107 >= mx && 63 <= my && 70 >= my) {
			renderTooltip(Arrays.asList(container.energyText(), container.genText()), mx, my);
		}
		if (47 <= mx && 62 >= mx && 15 <= my && 70 >= my) {
			renderTooltip(Arrays.asList(I18n.format(hot.getTranslationKey()), hot.getAmount() + " mB"), mx, my);
		}
		if (113 <= mx && 128 >= mx && 15 <= my && 70 >= my) {
			renderTooltip(Arrays.asList(I18n.format(cold.getTranslationKey()), cold.getAmount() + " mB"), mx, my);
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

		drawRectBatchOnly(x, y, x2, y2, 0xff151515);
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
		vb.pos(left, bottom, z).color(yr, yg, yb, ya).endVertex();
		vb.pos(right, bottom, z).color(yr, yg, yb, ya).endVertex();
		vb.pos(right, top, z).color(yr, yg, yb, ya).endVertex();
		vb.pos(left, top, z).color(yr, yg, yb, ya).endVertex();
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
			vb.pos(right, top, z).color(yr, yg, yb, ya).endVertex();
			vb.pos(left, top, z).color(zr, zg, zb, za).endVertex();
			vb.pos(left, bottom, z).color(zr, zg, zb, za).endVertex();
			vb.pos(right, bottom, z).color(yr, yg, yb, ya).endVertex();
		} else {
			vb.pos(right, top, z).color(yr, yg, yb, ya).endVertex();
			vb.pos(left, top, z).color(yr, yg, yb, ya).endVertex();
			vb.pos(left, bottom, z).color(zr, zg, zb, za).endVertex();
			vb.pos(right, bottom, z).color(zr, zg, zb, za).endVertex();
		}
		tes.draw();
	}

	private void drawFluidStack(int x, int y, int w, int h, FluidStack fs, int cap) {
		if (fs.isEmpty()) return;
		Fluid fl = fs.getFluid();
		if (fl == Fluids.EMPTY) return;
		int am = fs.getAmount();
		if (am < cap) {
			int nh = h * am / cap;
			y += h - nh;
			h = nh;
		}

		ResourceLocation still = fl.getAttributes().getStillTexture();
		int color = fl.getAttributes().getColor(fs);
		TextureAtlasSprite tas = minecraft.getTextureGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(still);
		minecraft.textureManager.bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);

		glColorInt(color);

		final int xTileCount = w / 16;
		final int xRemainder = w - (xTileCount * 16);
		final int yTileCount = h / 16;
		final int yRemainder = h - (yTileCount * 16);

		final int yStart = y + h;
		final float z = getBlitOffset();

		float uMin = tas.getMinU();
		float uMax = tas.getMaxU();
		float vMin = tas.getMinV();
		float vMax = tas.getMaxV();
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();

		RenderSystem.enableBlend();
		RenderSystem.enableAlphaTest();
		for (int xt = 0; xt <= xTileCount; xt++) {
			for (int yt = 0; yt <= yTileCount; yt++) {
				int width = (xt == xTileCount) ? xRemainder : 16;
				int height = (yt == yTileCount) ? yRemainder : 16;
				int tx = x + (xt * 16);
				int ty = yStart - ((yt + 1) * 16);
				if (width > 0 && height > 0) {
					int maskTop = 16 - height;
					int maskRight = 16 - width;

					uMax = uMax - (maskRight / 16.0f * (uMax - uMin));
					vMax = vMax - (maskTop / 16.0f * (vMax - vMin));

					buf.begin(7, DefaultVertexFormats.POSITION_TEX);
					buf.pos(tx, ty + 16, z).tex(uMin, vMax).endVertex();
					buf.pos(tx + 16 - maskRight, ty + 16, z).tex(uMax, vMax).endVertex();
					buf.pos(tx + 16 - maskRight, ty + maskTop, z).tex(uMax, vMin).endVertex();
					buf.pos(tx, ty + maskTop, z).tex(uMin, vMin).endVertex();
					tes.draw();
				}
			}
		}
		RenderSystem.disableAlphaTest();
		RenderSystem.disableBlend();
	}

	private static void glColorInt(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;
		float alpha = ((color >> 24) & 0xFF) / 255F;

		RenderSystem.color4f(red, green, blue, alpha);
	}
}
