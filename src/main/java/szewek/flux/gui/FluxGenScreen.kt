package szewek.flux.gui

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.ITextComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import szewek.flux.FluxMod
import szewek.flux.container.FluxGenContainer
import szewek.ktutils.draw
import szewek.ktutils.newMatrix

@OnlyIn(Dist.CLIENT)
class FluxGenScreen(
        screenContainer: FluxGenContainer,
        inv: PlayerInventory,
        titleIn: ITextComponent
) : ContainerScreen<FluxGenContainer>(screenContainer, inv, titleIn) {
    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        renderBackground(0)
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        minecraft!!.textureManager.bindTexture(BG_TEX)
        blit(guiLeft, guiTop, 0, 0, xSize, ySize)
        newMatrix {
            RenderSystem.translatef(guiLeft.toFloat(), guiTop.toFloat(), 0f)
            drawGuiBar(86, 34, 90, 52, container.workFill, -0x575758, -0x101011, false)
            drawGuiBar(68, 63, 108, 71, container.energyFill, -0x57dedf, -0x10bdbe, true)
        }
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        val s = title.formattedText
        font.drawString(s, (xSize / 2 - font.getStringWidth(s) / 2).toFloat(), 5.0f, 0x404040)
        font.drawString(playerInventory.displayName.formattedText, 8.0f, (ySize - 96 + 2).toFloat(), 0x404040)
        val mx = mouseX - guiLeft
        val my = mouseY - guiTop
        if (mx in 68..107 && my in 63..70) renderTooltip(listOf(container.energyText(), container.genText()), mx, my)
        renderHoveredToolTip(mx, my)
    }

    private fun drawGuiBar(x: Int, y: Int, x2: Int, y2: Int, fill: Float, c1: Int, c2: Int, reverse: Boolean) {
        var x = x
        var y = y
        var x2 = x2
        var y2 = y2
        RenderSystem.disableTexture()
        RenderSystem.enableBlend()
        RenderSystem.disableAlphaTest()
        RenderSystem.defaultBlendFunc()
        RenderSystem.shadeModel(7425)
        //
        drawRectBatchOnly(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble(), -0xcdcdce)
        x++
        y++
        x2--
        y2--
        val f: Int
        if (fill > 0) {
            drawGradientRectBatchOnly(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble(), c1, c2)
            if (x2 - x > y2 - y) {
                f = MathHelper.ceil((x2 - x) * fill)
                if (reverse) x += f else x2 -= f
            } else {
                f = MathHelper.ceil((y2 - y) * fill)
                if (reverse) y += f else y2 -= f
            }
        }
        drawRectBatchOnly(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble(), -0xeaeaeb)
        //
        RenderSystem.shadeModel(7424)
        RenderSystem.disableBlend()
        RenderSystem.enableAlphaTest()
        RenderSystem.enableTexture()
    }

    private fun drawRectBatchOnly(left: Double, top: Double, right: Double, bottom: Double, c: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        var p = 0.0
        if (left < right) {
            p = left
            left = right
            right = p
        }
        if (top < bottom) {
            p = top
            top = bottom
            bottom = p
        }
        val ya = (c shr 24 and 255).toFloat() / 255f
        val yr = (c shr 16 and 255).toFloat() / 255f
        val yg = (c shr 8 and 255).toFloat() / 255f
        val yb = (c and 255).toFloat() / 255f
        val z = blitOffset.toDouble()
        val tes = Tessellator.getInstance()
        tes.draw(7, DefaultVertexFormats.POSITION_COLOR) { vb ->
            // func_225582_a_ == pos
            vb.func_225582_a_(left, bottom, z).func_227885_a_(yr, yg, yb, ya).endVertex()
            vb.func_225582_a_(right, bottom, z).func_227885_a_(yr, yg, yb, ya).endVertex()
            vb.func_225582_a_(right, top, z).func_227885_a_(yr, yg, yb, ya).endVertex()
            vb.func_225582_a_(left, top, z).func_227885_a_(yr, yg, yb, ya).endVertex()
        }
    }

    private fun drawGradientRectBatchOnly(left: Double, top: Double, right: Double, bottom: Double, color1: Int, color2: Int) {
        val ya = (color1 shr 24 and 255).toFloat() / 255f
        val yr = (color1 shr 16 and 255).toFloat() / 255f
        val yg = (color1 shr 8 and 255).toFloat() / 255f
        val yb = (color1 and 255).toFloat() / 255f
        val za = (color2 shr 24 and 255).toFloat() / 255f
        val zr = (color2 shr 16 and 255).toFloat() / 255f
        val zg = (color2 shr 8 and 255).toFloat() / 255f
        val zb = (color2 and 255).toFloat() / 255f
        val z = blitOffset.toDouble()
        val tes = Tessellator.getInstance()
        tes.draw(7, DefaultVertexFormats.POSITION_COLOR) { vb ->
            if (right - left > bottom - top) {
                vb.func_225582_a_(right, top, z).func_227885_a_(yr, yg, yb, ya).endVertex()
                vb.func_225582_a_(left, top, z).func_227885_a_(zr, zg, zb, za).endVertex()
                vb.func_225582_a_(left, bottom, z).func_227885_a_(zr, zg, zb, za).endVertex()
                vb.func_225582_a_(right, bottom, z).func_227885_a_(yr, yg, yb, ya).endVertex()
            } else {
                vb.func_225582_a_(right, top, z).func_227885_a_(yr, yg, yb, ya).endVertex()
                vb.func_225582_a_(left, top, z).func_227885_a_(yr, yg, yb, ya).endVertex()
                vb.func_225582_a_(left, bottom, z).func_227885_a_(zr, zg, zb, za).endVertex()
                vb.func_225582_a_(right, bottom, z).func_227885_a_(zr, zg, zb, za).endVertex()
            }
        }
    }

    companion object {
        private val BG_TEX = FluxMod.location("textures/gui/fluxgen.png")
    }
}