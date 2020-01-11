package szewek.ktutils

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.VertexFormat

inline fun newMatrix(f: () -> Unit) {
    RenderSystem.pushMatrix()
    f()
    RenderSystem.popMatrix()
}

inline fun Tessellator.draw(glMode: Int, format: VertexFormat, f: (BufferBuilder) -> Unit) {
    buffer.also {
        it.begin(glMode, format)
        f(it)
    }
    draw()
}