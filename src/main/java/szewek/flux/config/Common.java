package szewek.flux.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;

public final class Common {
	public final ForgeConfigSpec.BooleanValue disableOres;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> preferModCompat, blacklistCompatRecipes;

	public Common(ForgeConfigSpec.Builder bld) {
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

	static ForgeConfigSpec.Builder translate(ForgeConfigSpec.Builder bld, String name, String... comment) {
		return bld.comment(comment).translation("flux.configgui." + name);
	}
}
