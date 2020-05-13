package szewek.flux;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import szewek.flux.config.Common;
import szewek.flux.config.ConfigChangeListener;

import java.util.HashSet;
import java.util.Set;

public class FluxCfg {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Set<ConfigChangeListener> listeners = new HashSet<>();
	static final ForgeConfigSpec commonSpec;
	public static final Common COMMON;

	static {
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {
		LOGGER.debug("Loaded flux config file: {}", configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfig.Reloading cfgEvent) {
		if (cfgEvent.getConfig().getModId().equals(Flux.MODID)) {
			LOGGER.warn("Flux config just got changed on the file system!");
			for (ConfigChangeListener l : listeners) {
				l.onConfigChanged();
			}
		}
	}

	public static void addListener(final ConfigChangeListener listener) {
		listeners.add(listener);
	}

	public static void removeListener(final ConfigChangeListener listener) {
		listeners.remove(listener);
	}
}
