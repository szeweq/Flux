package szewek.flux.util;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import szewek.flux.FluxMod;

public final class MappingFixer {
	public static <T extends IForgeRegistryEntry<T>> void fixMapping(RegistryEvent.MissingMappings.Mapping<T> m) {
		ResourceLocation key = m.key;
		if ("mcflux".equals(key.getNamespace())) {
			T o = m.registry.getValue(FluxMod.location(key.getPath()));
			if (o != null) {
				m.remap(o);
			}
		}

	}
}