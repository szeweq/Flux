package szewek.fl.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A set of rectangles to check if a specified point is contained
 */
public class HoverSet {
	private final Set<GuiRect> rects = new HashSet<>();
	private HoverListener hoverListener;

	public HoverSet(HoverListener onHover) {
		hoverListener = onHover;
	}

	public void addAll(GuiRect ...rect) {
		Collections.addAll(rects, rect);
	}

	public void checkCoords(MatrixStack matrixStack, int x, int y) {
		if (hoverListener != null) {
			for (GuiRect r : rects) {
				if (x >= r.x1 && x < r.x2 && y >= r.y1 && y < r.y2) {
					hoverListener.onHover(r, matrixStack, x, y);
				}
			}
		}
	}

	public interface HoverListener {
		void onHover(GuiRect rect, MatrixStack matrixStack, int mx, int my);
	}
}
