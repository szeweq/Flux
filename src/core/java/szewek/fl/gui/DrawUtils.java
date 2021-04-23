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
import net.minecraft.util.math.vector.Matrix4f;
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
	 * @param rect Rectangle
	 * @param c color
	 * @param z Z position
	 */
	public static void drawRectBatchOnly(Matrix4f matrix, GuiRect rect, int c, float z) {
		int ya = c >> 24 & 255;
		int yr = c >> 16 & 255;
		int yg = c >> 8 & 255;
		int yb = c & 255;
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder vb = tes.getBuilder();
		vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
		vb.vertex(matrix, rect.x1, rect.y2, z).color(yr, yg, yb, ya).endVertex();
		vb.vertex(matrix, rect.x2, rect.y2, z).color(yr, yg, yb, ya).endVertex();
		vb.vertex(matrix, rect.x2, rect.y1, z).color(yr, yg, yb, ya).endVertex();
		vb.vertex(matrix, rect.x1, rect.y1, z).color(yr, yg, yb, ya).endVertex();
		tes.end();
	}

	/**
	 * Draws gradient with angle based on rectangle size. This method does not change rendering properties.
	 * If width is larger than height then the gradient is vertical. Otherwise it is horizontal.
	 * @param rect Rectangle
	 * @param color1 gradient starting color
	 * @param color2 gradient ending color
	 * @param z Z position
	 */
	public static void drawGradientRectBatchOnly(Matrix4f matrix, GuiRect rect, int color1, int color2, float z) {
		int ya = color1 >> 24 & 255;
		int yr = color1 >> 16 & 255;
		int yg = color1 >> 8 & 255;
		int yb = color1 & 255;
		int za = color2 >> 24 & 255;
		int zr = color2 >> 16 & 255;
		int zg = color2 >> 8 & 255;
		int zb = color2 & 255;
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder vb = tes.getBuilder();
		vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
		if (rect.x2 - rect.x1 > rect.y2 - rect.y1) {
			vb.vertex(matrix, rect.x2, rect.y1, z).color(yr, yg, yb, ya).endVertex();
			vb.vertex(matrix, rect.x1, rect.y1, z).color(zr, zg, zb, za).endVertex();
			vb.vertex(matrix, rect.x1, rect.y2, z).color(zr, zg, zb, za).endVertex();
			vb.vertex(matrix, rect.x2, rect.y2, z).color(yr, yg, yb, ya).endVertex();
		} else {
			vb.vertex(matrix, rect.x2, rect.y1, z).color(yr, yg, yb, ya).endVertex();
			vb.vertex(matrix, rect.x1, rect.y1, z).color(yr, yg, yb, ya).endVertex();
			vb.vertex(matrix, rect.x1, rect.y2, z).color(zr, zg, zb, za).endVertex();
			vb.vertex(matrix, rect.x2, rect.y2, z).color(zr, zg, zb, za).endVertex();
		}
		tes.end();
	}

	/**
	 * Draws fluid texture in a rectangular area from bottom to top.
	 * @param rect Rectangle
	 * @param fs fluid stack
	 * @param cap internal capacity
	 * @param z Z position
	 */
	public static void drawFluidStack(Matrix4f matrix, GuiRect rect, FluidStack fs, int cap, float z) {
		final TextureAtlasSprite tas = applyFluidTexture(fs);
		if (tas == null) {
			return;
		}
		final int w = rect.x2 - rect.x1;
		final int nh = (rect.y2 - rect.y1) * Math.min(fs.getAmount() / cap, 1);

		final int xTiles = w >> 4;
		final int xRemainder = w & 15;
		final int yTiles = nh >> 4;
		final int yRemainder = nh & 15;

		float uMin = tas.getU0();
		float uMax = tas.getU1();
		float vMin = tas.getV0();
		float vMax = tas.getV1();
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuilder();

		RenderSystem.enableBlend();
		RenderSystem.enableAlphaTest();
		for (int xt = 0; xt <= xTiles; xt++) {
			int width = (xt == xTiles) ? xRemainder : 16;
			int tx = xt * 16 + rect.x1;

			for (int yt = 0; yt <= yTiles; yt++) {
				int height = (yt == yTiles) ? yRemainder : 16;
				int ty = rect.y2 - ((yt + 1) * 16);
				if (width > 0 && height > 0) {
					int maskTop = 16 - height;
					int maskRight = 16 - width;

					uMax = uMax - (maskRight / 16.0f * (uMax - uMin));
					vMax = vMax - (maskTop / 16.0f * (vMax - vMin));

					buf.begin(7, DefaultVertexFormats.POSITION_TEX);
					buf.vertex(matrix, tx, ty + 16, z).uv(uMin, vMax).endVertex();
					buf.vertex(matrix, tx + 16 - maskRight, ty + 16, z).uv(uMax, vMax).endVertex();
					buf.vertex(matrix, tx + 16 - maskRight, ty + maskTop, z).uv(uMax, vMin).endVertex();
					buf.vertex(matrix, tx, ty + maskTop, z).uv(uMin, vMin).endVertex();
					tes.end();
				}
			}
		}
		RenderSystem.disableAlphaTest();
		RenderSystem.disableBlend();
	}

	private static TextureAtlasSprite applyFluidTexture(FluidStack fs) {
		if (fs.isEmpty()) {
			return null;
		}
		Fluid fl = fs.getFluid();
		if (fl == Fluids.EMPTY) {
			return null;
		}

		ResourceLocation still = fl.getAttributes().getStillTexture();
		Minecraft minecraft = Minecraft.getInstance();
		TextureAtlasSprite tas = minecraft.getTextureAtlas(PlayerContainer.BLOCK_ATLAS).apply(still);
		minecraft.textureManager.bind(PlayerContainer.BLOCK_ATLAS);
		glColorInt(fl.getAttributes().getColor(fs));
		return tas;
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
