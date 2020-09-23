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
	protected ITextComponent func_230479_g_() {
		return filterName;
	}

	@Override
	protected Set<Item> func_212958_h() {
		return Collections.singleton(Items.AIR);
	}

	@Override
	protected void updateCollections(boolean resetPage) {
		List<RecipeList> list = recipeBook.getRecipes(currentTab.func_201503_d());
		list.forEach(rl -> rl.canCraft(stackedContents, field_201522_g.getWidth(), field_201522_g.getHeight(), recipeBook));
		List<RecipeList> list1 = new ArrayList<>(list);
		list1.removeIf(rl -> !rl.isNotEmpty() || !rl.containsValidRecipes());
		list1.removeIf(rl -> rl.getRecipes().stream().noneMatch(recipe -> recipe.getType() == recipeType));
		String s = searchBar.getText();
		if (s.length() > 0) {
			final ObjectSet<RecipeList> objectset = new ObjectLinkedOpenHashSet<>(mc.getSearchTree(SearchTreeManager.RECIPES).search(s.toLowerCase(Locale.ROOT)));
			list1.removeIf(rl -> !objectset.contains(rl));
		}

		if (recipeBook.func_242141_a(field_201522_g)) {
			list1.removeIf(rl -> !rl.containsCraftableRecipes());
		}

		recipeBookPage.updateLists(list1, resetPage);
	}
}
