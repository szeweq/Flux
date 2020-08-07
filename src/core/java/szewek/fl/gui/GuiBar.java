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
public class GuiBar extends GuiRect {
	private final int c1, c2;
	private final boolean reverse;

	public GuiBar(int x1, int y1, int x2, int y2, int c1, int c2, boolean reverse) {
		super(x1, y1, x2, y2);
		this.c1 = c1;
		this.c2 = c2;
		this.reverse = reverse;
	}

	public void draw(MatrixStack matrixStack, float fill, float z) {
		final Matrix4f matrix = matrixStack.getLast().getMatrix();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(7425);
		DrawUtils.drawRectBatchOnly(matrix, x1, y1, x2, y2, 0xff323232, z);
		int x1 = this.x1 + 1;
		int y1 = this.y1 + 1;
		int x2 = this.x2 - 1;
		int y2 = this.y2 - 1;
		if (fill > 0F) {
			DrawUtils.drawGradientRectBatchOnly(matrix, x1, y1, x2, y2, c1, c2, z);
			int f;
			if (x2 - x1 > y2 - y1) {
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

		DrawUtils.drawRectBatchOnly(matrix, x1, y1, x2, y2, 0xff151515, z);
		RenderSystem.shadeModel(7424);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableTexture();
	}
}
