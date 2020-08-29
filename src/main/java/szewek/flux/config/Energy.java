package szewek.flux.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Energy {
	public final ForgeConfigSpec.IntValue
			fluxGenBaseEnergy,
			basicMachine,
			digger,
			farmer,
			butcher,
			mobPounder,
			itemAbsorber,
			onlineMarket;

	public Energy(ForgeConfigSpec.Builder bld) {
		fluxGenBaseEnergy = Common.translate(bld, "fluxGenBaseEnergyValue", "Base energy generation for Flux Generator")
				.defineInRange("fluxGen", 40, 0, Integer.MAX_VALUE);
		basicMachine = energyUsage(bld,
				"basic machines (Grinding Mill, Washer, etc.)", "basicMachine", 40
		);
		digger = energyUsage(bld, "Digger", "digger", 200);
		farmer = energyUsage(bld, "Farmer", "farmer", 100);
		butcher = energyUsage(bld, "Butcher", "butcher", 700);
		mobPounder = energyUsage(bld, "Mob Pounder", "mobPounder", 1000);
		itemAbsorber = energyUsage(bld, "Item Absorber", "itemAbsorber", 100);
		onlineMarket = energyUsage(bld, "Online Market", "onlineMarket", 200);
	}

	private static ForgeConfigSpec.IntValue energyUsage(ForgeConfigSpec.Builder bld, String comment, String name, int def) {
		return bld.comment("Energy usage for " + comment)
				.translation("flux.configgui.energy." + name)
				.defineInRange(name, def, 1, 1000);
	}
}
