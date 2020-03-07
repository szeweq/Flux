package szewek.flux.util;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;
import java.util.regex.Pattern;

// EXPERIMENTAL
public class FluxDataManager implements ISelectiveResourceReloadListener {
	private static final Logger LOGGER = LogManager.getLogger("FluxDataManager");
	private static final Pattern FLUXGEN_TYPES = Pattern.compile("^(catalyst|hot|cold)\\.json$");

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {

		for (ResourceLocation rl : resourceManager.getAllResourceLocations("values/fluxgen", s -> FLUXGEN_TYPES.matcher(s).matches())) {
			if ("flux".equals(rl.getNamespace())) {
				LOGGER.debug("Found value list for Flux Generator: {}", rl);
			}
		}
	}
}
