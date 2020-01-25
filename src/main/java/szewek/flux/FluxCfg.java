package szewek.flux;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import java.util.HashSet;
import java.util.Set;

public class FluxCfg {
	private static Set<IConfigChangeListener> listeners = new HashSet<>();

	public static class Common {
		public final IntValue
				fluxGenBaseEnergyValue,
				basicMachineEU,
				diggerEU, farmerEU, butcherEU, mobPounderEU, itemAbsorberEU;

		Common(ForgeConfigSpec.Builder bld) {
			fluxGenBaseEnergyValue = bld
					.comment("Base energy generation for Flux Generator")
					.translation("flux.configgui.fluxGenBaseEnergyValue")
					.defineInRange("fluxGenBaseEnergyValue", 40, 0, Integer.MAX_VALUE);
			basicMachineEU = energyUsage(bld,
					"basic machines (Grinding Mill, Washer, etc.)", "basicMachine", 40
			);
			diggerEU = energyUsage(bld, "Digger", "digger", 200);
			farmerEU = energyUsage(bld, "Farmer", "farmer", 100);
			butcherEU = energyUsage(bld, "Butcher", "butcher", 700);
			mobPounderEU = energyUsage(bld, "Mob Pounder", "mobPounder", 1000);
			itemAbsorberEU = energyUsage(bld, "Item Absorber", "itemAbsorber", 100);
		}

		private static IntValue energyUsage(ForgeConfigSpec.Builder bld, String comment, String name, int def) {
			final String euName = name + "EnergyUsage";
			return bld.comment("Energy usage for " + comment).translation("flux.configgui." + euName)
					.defineInRange(euName, def, 1, 1000);
		}
	}

	public interface IConfigChangeListener {
		void onConfigChanged();
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
		for (IConfigChangeListener l : listeners) l.onConfigChanged();
	}

	public static void addListener(final IConfigChangeListener listener) {
		listeners.add(listener);
	}

	public static void removeListener(final IConfigChangeListener listener) {
		listeners.remove(listener);
	}
}
