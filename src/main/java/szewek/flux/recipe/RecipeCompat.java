package szewek.flux.recipe;

import javax.annotation.Nullable;

public final class RecipeCompat {

	// TODO Make modded recipe compatibility. Find approperiate (grinding, washing etc.) recipes from other mods.
	@Nullable
	public static ICompatRecipe findCompatibleRecipe(FluxRecipeType<?> fluxType) {
		return null;
	}

	private RecipeCompat() {}
}
