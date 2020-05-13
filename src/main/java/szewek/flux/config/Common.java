package szewek.flux.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;

public class Common {
	public final ForgeConfigSpec.IntValue
			fluxGenBaseEnergy,
			basicMachineEU,
			diggerEU,
			farmerEU,
			butcherEU,
			mobPounderEU,
			itemAbsorberEU,
			onlineMarketEU;
	public final ForgeConfigSpec.BooleanValue disableOres;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> preferModCompat, blacklistCompatRecipes;

	public Common(ForgeConfigSpec.Builder bld) {
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
		onlineMarketEU = energyUsage(bld, "Online Market", "onlineMarket", 200);
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

	private static ForgeConfigSpec.IntValue energyUsage(ForgeConfigSpec.Builder bld, String comment, String name, int def) {
		final String euName = name + "EnergyUsage";
		return bld.comment("Energy usage for " + comment).translation("flux.configgui." + euName)
				.defineInRange(euName, def, 1, 1000);
	}
}
