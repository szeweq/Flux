package szewek.flux;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

public class FluxConfig {
	public static class Common {
		public final ForgeConfigSpec.IntValue fluxGenBaseEnergyValue;

		Common(ForgeConfigSpec.Builder bld) {
			fluxGenBaseEnergyValue = bld
					.comment("Base energy generation for Flux Generator")
					.translation("flux.configgui.fluxGenBaseEnergyValue")
					.defineInRange("fluxGenBaseEnergyValue", 40, 0, Integer.MAX_VALUE);
		}
	}

	static final ForgeConfigSpec commonSpec;
	public static final Common COMMON;

	static {
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {
		LogManager.getLogger().debug("Loaded flux config file: {}", configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfig.ConfigReloading configEvent) {
		LogManager.getLogger().fatal("Flux config just got changed on the file system!");
	}
}
