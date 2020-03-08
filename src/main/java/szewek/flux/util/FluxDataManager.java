package szewek.flux.util;

import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

// EXPERIMENTAL
public class FluxDataManager implements IResourceManagerReloadListener {
	private static final Logger LOGGER = LogManager.getLogger("FluxDataManager");
	private static final Pattern FLUXGEN_TYPES = Pattern.compile("^(catalyst|hot|cold)\\.json$");

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		for (ResourceLocation rl : resourceManager.getAllResourceLocations("values/fluxgen", s -> FLUXGEN_TYPES.matcher(s).matches())) {
			if ("flux".equals(rl.getNamespace())) {
				LOGGER.debug("Found value list for Flux Generator: {}", rl);
			}
		}
	}
}
