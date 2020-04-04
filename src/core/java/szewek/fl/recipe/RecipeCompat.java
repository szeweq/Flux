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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import szewek.fl.network.FluxPlus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Utility class for modded recipes
 */
public final class RecipeCompat {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<IRecipeType<?>, Set<IRecipeType<?>>> compatMap = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public static <C extends IInventory, T extends IRecipe<C>> Optional<IRecipe<C>> getCompatRecipe(IRecipeType<?> rtype, World w, IInventory inv) {
		RecipeManager rm = w.getRecipeManager();
		for (IRecipeType<?> rt : compatMap.getOrDefault(rtype, Collections.emptySet())) {
			IRecipeType<? extends T> recipeType = (IRecipeType<? extends T>) rt;
			for (IRecipe<C> r : rm.getRecipes(recipeType).values()) {
				try {
					if (((IRecipe<IInventory>) r).matches(inv, w)) {
						return Optional.of(r);
					}
				} catch (RuntimeException e) {
					LOGGER.warn("An exception occurred while checking recipe ({}). Sending report!", r.getId());
					FluxPlus.reportRecipeCompatError(recipeType.toString(), r.getClass().getName(), e.getMessage());
				}
			}
		}
		return Optional.empty();
	}

	public static void registerCompatRecipeTypes(IRecipeType<?> rtype, Set<String> compats) {
		ResourceLocation loc = Registry.RECIPE_TYPE.getKey(rtype);
		if (loc == null || !"flux".equals(loc.getNamespace())) {
			return;
		}

		Set<IRecipeType<?>> result = new LinkedHashSet<>();
		result.add(rtype);
		for (String compat : compats) {
			Optional<IRecipeType<?>> value = Registry.RECIPE_TYPE.getValue(new ResourceLocation(compat));
			value.ifPresent(result::add);
		}
		compatMap.put(rtype, result);
	}

	public static ItemStack getCompatOutput(final IRecipe<?> recipe, IInventory inv) {
		ItemStack stack = recipe.getRecipeOutput();
		if (stack.isEmpty()) {
			FluxPlus.reportRecipeCompatError(recipe.getType().toString(), recipe.getClass().getName(), "ItemStack is EMPTY");
		}
		return stack;
	}

	public static Consumer<Iterable<ItemStack>> getRecipeItemsConsumer(final IRecipe<?> recipe) {
		Consumer<Iterable<ItemStack>> consumer;
		if (recipe instanceof Consumer<?>) {
			//noinspection unchecked
			consumer = (Consumer<Iterable<ItemStack>>) recipe;
		} else {
			final NonNullList<Ingredient> ingredients = recipe.getIngredients();
			if (ingredients.isEmpty()) {
				consumer = stacks -> {
					for (ItemStack stack : stacks) {
						if (!stack.isEmpty()) {
							stack.grow(-1);
						}
					}
				};
			} else {
				consumer = stacks -> {
					ArrayList<ItemStack> filledInputs = new ArrayList<>();

					for (ItemStack stack : stacks) {
						if (!stack.isEmpty()) {
							filledInputs.add(stack);
						}
					}

					int[] match = RecipeMatcher.findMatches(filledInputs, ingredients);
					if (match != null) {
						for(int i = 0; i < match.length; ++i) {
							filledInputs.get(i).grow(-1);
						}
					}
				};
			}

		}
		return consumer;
	}

	private RecipeCompat() {}
}
