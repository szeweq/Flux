package szewek.flux.gui;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.gui.recipebook.AbstractRecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public final class MachineRecipeGui extends AbstractRecipeBookGui {
	private final ITextComponent filterName;
	private final IRecipeType<?> recipeType;

	public MachineRecipeGui(IRecipeType<?> recipeType, String name) {
		super();
		this.recipeType = recipeType;
		filterName = new TranslationTextComponent("gui.flux.recipebook.toggleRecipes." + name);
	}
	/*
	@Override
	protected boolean func_212962_b() {
		return recipeBook.isFilteringCraftable();
	}

	@Override
	protected void func_212959_a(boolean v) {
		recipeBook.setFilteringCraftable(v);
	}

	@Override
	protected boolean func_212963_d() {
		return recipeBook.isGuiOpen();
	}

	@Override
	protected void func_212957_c(boolean v) {
		recipeBook.setGuiOpen(v);
	}

	 */

	@Override
	protected ITextComponent getRecipeFilterName() {
		return filterName;
	}

	@Override
	protected Set<Item> getFuelItems() {
		return Collections.singleton(Items.AIR);
	}

	@Override
	protected void updateCollections(boolean resetPage) {
		List<RecipeList> list = book.getCollection(selectedTab.getCategory());
		list.forEach(rl -> rl.canCraft(stackedContents, menu.getGridWidth(), menu.getGridHeight(), book));
		List<RecipeList> list1 = new ArrayList<>(list);
		list1.removeIf(rl -> !rl.hasFitting() || !rl.hasKnownRecipes());
		list1.removeIf(rl -> rl.getRecipes().stream().noneMatch(recipe -> recipe.getType() == recipeType));
		String s = searchBox.getValue();
		if (s.length() > 0) {
			final ObjectSet<RecipeList> objectset = new ObjectLinkedOpenHashSet<>(minecraft.getSearchTree(SearchTreeManager.RECIPE_COLLECTIONS).search(s.toLowerCase(Locale.ROOT)));
			list1.removeIf(rl -> !objectset.contains(rl));
		}

		if (book.isFiltering(menu)) {
			list1.removeIf(rl -> !rl.hasCraftable());
		}

		recipeBookPage.updateCollections(list1, resetPage);
	}
}
