package szewek.fl.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Bar for displaying relative values. Starting point is determined by its size and "reverse" value.
 */
@OnlyIn(Dist.CLIENT)
public class GuiBar {

	public final GuiRect rect;
	private final GuiRect rectShrink;
	private final int c1, c2;
	private final boolean reverse;

	public GuiBar(GuiRect rect, int c1, int c2, boolean reverse) {
		this.rect = rect;
		this.rectShrink = rect.grow(-1);
		this.c1 = c1;
		this.c2 = c2;
		this.reverse = reverse;
	}

	public void draw(MatrixStack matrixStack, float fill, float z) {
		final Matrix4f matrix = matrixStack.last().pose();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(7425);
		DrawUtils.drawRectBatchOnly(matrix, rect, 0xff323232, z);
		int x1 = rectShrink.x1;
		int y1 = rectShrink.y1;
		int x2 = rectShrink.x2;
		int y2 = rectShrink.y2;
		if (fill > 0F) {
			DrawUtils.drawGradientRectBatchOnly(matrix, rectShrink, c1, c2, z);
			int f;
			if (rect.x2 - rect.x1 > rect.y2 - rect.y1) {
				f = MathHelper.ceil((float)(x2 - x1) * fill);
				if (reverse) {
					x1 += f;
				} else {
					x2 -= f;
				}
			} else {
				f = MathHelper.ceil((float)(y2 - y1) * fill);
				if (reverse) {
					y1 += f;
				} else {
					y2 -= f;
				}
			}
		}

		DrawUtils.drawRectBatchOnly(matrix, new GuiRect(x1, y1, x2, y2), 0xff151515, z);
		RenderSystem.shadeModel(7424);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableTexture();
	}
}
