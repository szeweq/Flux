package szewek.flux.gui

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.minecraft.client.gui.recipebook.AbstractRecipeBookGui
import net.minecraft.client.gui.recipebook.RecipeList
import net.minecraft.client.util.SearchTreeManager
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.IRecipeType
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.util.*
import java.util.function.Consumer

@OnlyIn(Dist.CLIENT)
class MachineRecipeGui internal constructor(private val recipeType: IRecipeType<*>, name: String) : AbstractRecipeBookGui() {
    private val filterName: String = "gui.flux.recipebook.toggleRecipes.$name"

    override fun func_212962_b(): Boolean = recipeBook.isFilteringCraftable
    override fun func_212959_a(v: Boolean) {
        recipeBook.isFilteringCraftable = v
    }

    override fun func_212963_d(): Boolean = recipeBook.isGuiOpen
    override fun func_212957_c(v: Boolean) {
        recipeBook.isGuiOpen = v
    }

    override fun func_212960_g(): String = filterName
    override fun func_212958_h(): Set<Item> {
        return setOf(Items.AIR)
    }

    override fun updateCollections(resetPage: Boolean) {
        val list = recipeBook.getRecipes(currentTab.func_201503_d())
        list.forEach(Consumer { recipeList: RecipeList -> recipeList.canCraft(stackedContents, field_201522_g.width, field_201522_g.height, recipeBook) })
        val list1: MutableList<RecipeList> = ArrayList(list)
        list1.removeIf { recipeList: RecipeList -> !recipeList.isNotEmpty || !recipeList.containsValidRecipes() }
        list1.removeIf { recipeList: RecipeList -> recipeList.recipes.stream().noneMatch { recipe: IRecipe<*> -> recipe.type === recipeType } }
        val s = searchBar.text
        if (s.isNotEmpty()) {
            val objectset: ObjectSet<RecipeList> = ObjectLinkedOpenHashSet(mc.getSearchTree(SearchTreeManager.RECIPES).search(s.toLowerCase(Locale.ROOT)))
            list1.removeIf { recipeList: RecipeList? -> !objectset.contains(recipeList) }
        }
        if (recipeBook.isFilteringCraftable(field_201522_g)) {
            list1.removeIf { recipeList: RecipeList -> !recipeList.containsCraftableRecipes() }
        }
        recipeBookPage.updateLists(list1, resetPage)
    }

}