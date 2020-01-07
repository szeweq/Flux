package szewek.flux.gui;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.gui.recipebook.AbstractRecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class MachineRecipeGui extends AbstractRecipeBookGui {
	private final IRecipeType<?> recipeType;
	private final String filterName;

	MachineRecipeGui(IRecipeType<?> rtype, String name) {
		recipeType = rtype;
		filterName = "gui.flux.recipebook.toggleRecipes." + name;
	}

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

	@Override
	protected String func_212960_g() {
		return filterName;
	}

	@Override
	protected Set<Item> func_212958_h() {
		return Collections.singleton(Items.AIR);
	}

	@Override
	protected void updateCollections(boolean resetPage) {
		List<RecipeList> list = recipeBook.getRecipes(this.currentTab.func_201503_d());
		list.forEach((recipeList) -> {
			recipeList.canCraft(this.stackedContents, this.field_201522_g.getWidth(), this.field_201522_g.getHeight(), this.recipeBook);
		});
		List<RecipeList> list1 = new ArrayList<>(list);
		list1.removeIf(recipeList -> !recipeList.isNotEmpty() || !recipeList.containsValidRecipes());
		list1.removeIf(recipeList -> recipeList.getRecipes().stream().noneMatch(recipe -> recipe.getType() == recipeType));
		String s = this.searchBar.getText();
		if (!s.isEmpty()) {
			ObjectSet<RecipeList> objectset = new ObjectLinkedOpenHashSet<>(this.mc.getSearchTree(SearchTreeManager.RECIPES).search(s.toLowerCase(Locale.ROOT)));
			list1.removeIf((recipeList) -> !objectset.contains(recipeList));
		}

		if (this.recipeBook.isFilteringCraftable(this.field_201522_g)) {
			list1.removeIf((recipeList) -> !recipeList.containsCraftableRecipes());
		}

		recipeBookPage.updateLists(list1, resetPage);
	}
}
