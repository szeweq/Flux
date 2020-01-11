package szewek.flux.item

import net.minecraft.item.Item
import szewek.flux.util.Metal

class MetalItem(properties: Properties) : Item(properties) {
    var metal: Metal? = null
        private set

    fun withMetal(metal: Metal?): MetalItem {
        if (this.metal == null) this.metal = metal
        return this
    }

}