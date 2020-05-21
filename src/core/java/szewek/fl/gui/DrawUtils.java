package szewek.fl.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

/**
 * Utility class with drawing methods
 */
@OnlyIn(Dist.CLIENT)
public final class DrawUtils {
	private DrawUtils() {}

	/**
	 * Draws a solid gradient. This method does not change rendering properties.
	 * @param left left X position
	 * @param top top Y position
	 * @param right right X position
	 * @param bottom bottom Y position
	 * @param c color
	 * @param z Z position
	 */
	public static void drawRectBatchOnly(double left, double top, double right, double bottom, int c, double z) {
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

		int ya = c >> 24 & 255;
		int yr = c >> 16 & 255;
		int yg = c >> 8 & 255;
		int yb = c & 255;
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder vb = tes.getBuffer();
		vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
		vb.pos(left, bottom, z).color(yr, yg, yb, ya).endVertex();
		vb.pos(right, bottom, z).color(yr, yg, yb, ya).endVertex();
		vb.pos(right, top, z).color(yr, yg, yb, ya).endVertex();
		vb.pos(left, top, z).color(yr, yg, yb, ya).endVertex();
		tes.draw();
	}

	/**
	 * Draws gradient with angle based on rectangle size. This method does not change rendering properties.
	 * If width is larger than height then the gradient is vertical. Otherwise it is horizontal.
	 * @param left left X position
	 * @param top top Y position
	 * @param right right X position
	 * @param bottom bottom Y position
	 * @param color1 gradient starting color
	 * @param color2 gradient ending color
	 * @param z Z position
	 */
	public static void drawGradientRectBatchOnly(double left, double top, double right, double bottom, int color1, int color2, double z) {
		int ya = color1 >> 24 & 255;
		int yr = color1 >> 16 & 255;
		int yg = color1 >> 8 & 255;
		int yb = color1 & 255;
		int za = color2 >> 24 & 255;
		int zr = color2 >> 16 & 255;
		int zg = color2 >> 8 & 255;
		int zb = color2 & 255;
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

	/**
	 * Draws fluid texture in a rectangular area from bottom to top.
	 * @param x X position
	 * @param y Y position
	 * @param w width
	 * @param h height
	 * @param fs fluid stack
	 * @param cap internal capacity
	 * @param z Z position
	 */
	public static void drawFluidStack(int x, int y, int w, int h, FluidStack fs, int cap, float z) {
		if (fs.isEmpty()) {
			return;
		}
		Fluid fl = fs.getFluid();
		if (fl == Fluids.EMPTY) {
			return;
		}
		ResourceLocation still = fl.getAttributes().getStillTexture();
		Minecraft minecraft = Minecraft.getInstance();
		TextureAtlasSprite tas = minecraft.getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(still);
		minecraft.textureManager.bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
		glColorInt(fl.getAttributes().getColor(fs));

		final int ny = y + h;
		final int nh = Math.min(h * fs.getAmount() / cap, h);

		final int xTiles = w >> 4;
		final int xRemainder = w & 15;
		final int yTiles = nh >> 4;
		final int yRemainder = nh & 15;

		float uMin = tas.getMinU();
		float uMax = tas.getMaxU();
		float vMin = tas.getMinV();
		float vMax = tas.getMaxV();
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();

		RenderSystem.enableBlend();
		RenderSystem.enableAlphaTest();
		for (int xt = 0; xt <= xTiles; xt++) {
			int width = (xt == xTiles) ? xRemainder : 16;
			int tx = x + (xt * 16);

			for (int yt = 0; yt <= yTiles; yt++) {
				int height = (yt == yTiles) ? yRemainder : 16;
				int ty = ny - ((yt + 1) * 16);
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

	/**
	 * Converts integer bits into color values and applies them.
	 * @param color Formatted color (hex 0xAARRGGBB)
	 */
	public static void glColorInt(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;
		float alpha = ((color >> 24) & 0xFF) / 255F;

		RenderSystem.color4f(red, green, blue, alpha);
	}
}
