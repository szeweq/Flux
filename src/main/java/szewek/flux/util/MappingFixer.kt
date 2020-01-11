package szewek.flux.util

import net.minecraftforge.event.RegistryEvent.MissingMappings
import net.minecraftforge.registries.IForgeRegistryEntry
import szewek.flux.FluxMod

object MappingFixer {
    @JvmStatic
    fun <T : IForgeRegistryEntry<T>?> fixMapping(m: MissingMappings.Mapping<T>) {
        val key = m.key
        if (key.namespace == "mcflux") {
            val o = m.registry.getValue(FluxMod.location(key.path))
            if (o != null) m.remap(o)
        }
    }
}