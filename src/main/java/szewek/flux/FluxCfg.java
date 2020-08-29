package szewek.flux;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import szewek.flux.config.Common;
import szewek.flux.config.ConfigChangeListener;
import szewek.flux.config.Energy;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class FluxCfg {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Set<ConfigChangeListener> listeners = new HashSet<>();
	private static final Path CONFIG_PATH;
	static final ForgeConfigSpec commonSpec, energySpec;
	public static final Common COMMON;
	public static final Energy ENERGY;

	static {
		CONFIG_PATH = FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve("flux"), "flux");

		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();

		final Pair<Energy, ForgeConfigSpec> energyPair = new ForgeConfigSpec.Builder().configure(Energy::new);
		energySpec = energyPair.getRight();
		ENERGY = energyPair.getLeft();
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

	public static void addConfig(final ModLoadingContext mlc) {
		mlc.registerConfig(ModConfig.Type.COMMON, commonSpec, "flux/common.toml");
		mlc.registerConfig(ModConfig.Type.COMMON, energySpec, "flux/energy.toml");
	}

	public static void addListener(final ConfigChangeListener listener) {
		listeners.add(listener);
	}

	public static void removeListener(final ConfigChangeListener listener) {
		listeners.remove(listener);
	}
}
