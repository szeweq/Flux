package szewek.fl.recipe;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.util.RecipeMatcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Utility class for modded recipes
 */
public final class RecipeCompat {
	private static final Map<IRecipeType<?>, Set<IRecipeType<?>>> compatMap = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public static <C extends IInventory, T extends IRecipe<C>> Optional<IRecipe<C>> getCompatRecipe(IRecipeType<?> rtype, World w, IInventory inv) {
		RecipeManager rm = w.getRecipeManager();
		return compatMap.getOrDefault(rtype, Collections.emptySet())
				.stream().map(rt -> (IRecipeType<? extends T>) rt)
				.flatMap(rt -> rm.getRecipes(rt).values().stream())
				.filter(r -> ((IRecipe<IInventory>) r).matches(inv, w))
				.findFirst();
	}

	public static void registerCompatRecipeTypes(IRecipeType<?> rtype, Set<String> compats) {
		ResourceLocation loc = Registry.RECIPE_TYPE.getKey(rtype);
		if (loc == null || !"flux".equals(loc.getNamespace())) return;

		Set<IRecipeType<?>> result = new LinkedHashSet<>();
		result.add(rtype);
		for (String compat : compats) {
			Optional<IRecipeType<?>> value = Registry.RECIPE_TYPE.getValue(new ResourceLocation(compat));
			value.ifPresent(result::add);
		}
		compatMap.put(rtype, result);
	}

	public static Consumer<List<ItemStack>> getRecipeItemsConsumer(final IRecipe<?> recipe) {
		Consumer<List<ItemStack>> consumer;
		if (recipe instanceof Consumer<?>) {
			//noinspection unchecked
			consumer = (Consumer<List<ItemStack>>) recipe;
		} else {
			final NonNullList<Ingredient> ingredients = recipe.getIngredients();
			consumer = stacks -> {
				ArrayList<ItemStack> filledInputs = new ArrayList<>();

				for (ItemStack stack : stacks) {
					if (!stack.isEmpty()) filledInputs.add(stack);
				}

				int[] match = RecipeMatcher.findMatches(filledInputs, ingredients);
				if (match != null) {
					for(int i = 0; i < match.length; ++i) {
						filledInputs.get(i).grow(-1);
					}
				}
			};
		}
		return consumer;
	}

	private RecipeCompat() {}
}
