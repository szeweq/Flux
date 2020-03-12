package szewek.flux;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FluxCfg {
	private static final Set<IConfigChangeListener> listeners = new HashSet<>();
	static final ForgeConfigSpec commonSpec;
	public static final Common COMMON;

	public static class Common {
		public final IntValue
				fluxGenBaseEnergy,
				basicMachineEU,
				diggerEU,
				farmerEU,
				butcherEU,
				mobPounderEU,
				itemAbsorberEU;
		public final ForgeConfigSpec.BooleanValue disableOres;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> preferModCompat, blacklistCompatRecipes;

		Common(ForgeConfigSpec.Builder bld) {
			fluxGenBaseEnergy = translate(bld, "fluxGenBaseEnergyValue", "Base energy generation for Flux Generator")
					.defineInRange("fluxGenBaseEnergyValue", 40, 0, Integer.MAX_VALUE);
			basicMachineEU = energyUsage(bld,
					"basic machines (Grinding Mill, Washer, etc.)", "basicMachine", 40
			);
			diggerEU = energyUsage(bld, "Digger", "digger", 200);
			farmerEU = energyUsage(bld, "Farmer", "farmer", 100);
			butcherEU = energyUsage(bld, "Butcher", "butcher", 700);
			mobPounderEU = energyUsage(bld, "Mob Pounder", "mobPounder", 1000);
			itemAbsorberEU = energyUsage(bld, "Item Absorber", "itemAbsorber", 100);
			preferModCompat = translate(bld, "preferModCompat", "Order of preferred mod names for item recipe results.",
					"Empty list means that first item available from specific tag is chosen.",
					"First mod name has the highest priority.",
					"Add \"jaopca\" to ignore all tag recipes.")
					.defineList("preferModCompat", Collections::emptyList, o -> true);
			disableOres = translate(bld, "disableOres", "Disable Ore Generation").define("disableOres", false);
			blacklistCompatRecipes = translate(bld, "blacklistCompatRecipes", "A blacklist for recipe compatibility with other mods.",
					"Put names of recipe types you don't want them to work with Flux machines (like \"minecraft:smelting\").")
					.defineList("blacklistCompatRecipes", Collections::emptyList, o -> true);
		}

		private static ForgeConfigSpec.Builder translate(ForgeConfigSpec.Builder bld, String name, String... comment) {
			return bld.comment(comment).translation("flux.configgui." + name);
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
	public static void onFileChange(final ModConfig.Reloading configEvent) {
		LogManager.getLogger().fatal("Flux config just got changed on the file system!");
		for (IConfigChangeListener l : listeners) {
			l.onConfigChanged();
		}
	}

	public static void addListener(final IConfigChangeListener listener) {
		listeners.add(listener);
	}

	public static void removeListener(final IConfigChangeListener listener) {
		listeners.remove(listener);
	}
}
