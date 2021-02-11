package szewek.flux.compat.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import szewek.fl.util.IntPair;
import szewek.flux.F;
import szewek.flux.data.FluxGenValues;
import szewek.flux.tile.FluxGenTile;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static szewek.flux.Flux.MODID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluxGenCategory implements IRecipeCategory<FluxGenCategory.Product> {
	private static final ResourceLocation BG_TEX = F.loc("textures/gui/fluxgen.png");
	private final IDrawable background;
	private final IDrawable icon;
	private final String localizedName = I18n.format("block.flux.fluxgen");

	public FluxGenCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(BG_TEX, 46, 14, 84, 58);
		icon = guiHelper.createDrawableIngredient(new ItemStack(F.B.FLUXGEN));
	}

	@Override
	public ResourceLocation getUid() {
		return JEIFluxPlugin.FLUXGEN;
	}

	@Override
	public Class<? extends Product> getRecipeClass() {
		return Product.class;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setIngredients(Product product, IIngredients ingredients) {
		if (product.type == 2) {
			ingredients.setInput(VanillaTypes.ITEM, product.item);
		} else {
			ingredients.setInput(VanillaTypes.FLUID, product.fluid);
		}
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, Product product, IIngredients iIngredients) {
		if (product.type == 2) {
			IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
			guiItemStacks.init(1, true, 46, 20);
			guiItemStacks.set(1, product.item);
		} else {
			IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
			int s, x;
			if (product.type == 1) {
				s = 0;
				x = 1;
			} else {
				s = 1;
				x = 67;
			}
			guiFluidStacks.init(s, true, x, 1, 16, 56, FluxGenTile.fluidCap, true, null);
			guiFluidStacks.set(s, product.fluid);
		}
	}

	@Override
	public void draw(Product product, MatrixStack matrixStack, double mouseX, double mouseY) {
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer fontRenderer = minecraft.fontRenderer;

		int strW = fontRenderer.getStringWidth(product.typeStr);
		fontRenderer.drawString(matrixStack, product.typeStr, (float)(background.getWidth() - strW) / 2, 1, 0xff404040);

		strW = fontRenderer.getStringWidth(product.factorStr);
		fontRenderer.drawString(matrixStack, product.factorStr, (float)(background.getWidth() - strW) / 2, 51, 0xff404040);
	}

	static class Product {
		private final int type;
		private final ItemStack item;
		private final FluidStack fluid;
		private final String factorStr, typeStr;

		public Product(Item item, IntPair val) {
			type = 2;
			typeStr = I18n.format("flux.fluxgen.factor");
			this.item = new ItemStack(item, val.r);
			fluid = FluidStack.EMPTY;
			factorStr = "x" + val.l;
		}

		public Product(Fluid fluid, boolean hot, IntPair val) {
			type = hot ? 1 : 0;
			typeStr = I18n.format("flux.fluxgen." + (hot ? "time" : "speed"));
			this.fluid = new FluidStack(fluid, val.r);
			item = ItemStack.EMPTY;
			factorStr = "x" + val.l;
		}

		static Collection<Product> getAll() {
			Collection<Product> products = new ArrayList<>();
			for (Map.Entry<Item, IntPair> e : FluxGenValues.CATALYSTS.all().entrySet()) {
				products.add(new Product(e.getKey(), e.getValue()));
			}
			for (Map.Entry<Fluid, IntPair> e : FluxGenValues.HOT_FLUIDS.all().entrySet()) {
				products.add(new Product(e.getKey(), true, e.getValue()));
			}
			for (Map.Entry<Fluid, IntPair> e : FluxGenValues.COLD_FLUIDS.all().entrySet()) {
				products.add(new Product(e.getKey(), false, e.getValue()));
			}
			return products;
		}
	}
}
