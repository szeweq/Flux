package szewek.fluxkt

import net.minecraftforge.fml.ModLoadingContext

class KtModLoadingContext(private val container: KtModContainer) {
    fun getEventBus() = container.eventBus

    companion object {
        fun get() = ModLoadingContext.get().extension<KtModLoadingContext>()
    }
}