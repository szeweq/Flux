package szewek.flux.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import szewek.fl.recipe.CountedIngredient;
import szewek.flux.F;
import szewek.flux.FluxMod;
import szewek.flux.util.IInventoryIO;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class CopyingRecipe implements IRecipe<IInventoryIO>, Consumer<Iterable<ItemStack>> {
	public static final IRecipeSerializer<CopyingRecipe> SERIALIZER = new Serializer();

	private final ResourceLocation id;
	private final String group;
	private final Ingredient source, material;
	public final int processTime;

	public CopyingRecipe(ResourceLocation id, String group, Ingredient src, Ingredient mat, int process) {
		this.id = id;
		this.group = group;
		source = src;
		material = mat;
		processTime = process;
	}

	@Override
	public IRecipeType<?> getType() {
		return F.R.COPYING;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(F.B.COPIER);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.from(Ingredient.EMPTY, material, source);
	}

	public Ingredient getSource() {
		return source;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean matches(IInventoryIO inv, World worldIn) {
		List<ItemStack> inputs = inv.getInputs();
		return material.test(inputs.get(0)) && source.test(inputs.get(1));
	}

	@Override
	public ItemStack getCraftingResult(IInventoryIO inv) {
		return inv.getStackInSlot(1).copy();
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height == 2;
	}

	@Override
	public void accept(Iterable<ItemStack> stacks) {
		Iterator<ItemStack> iter = stacks.iterator();
		if (iter.hasNext()) {
			ItemStack matStack = iter.next();
			int count = material instanceof CountedIngredient ? ((CountedIngredient) material).getCount() : 1;
			matStack.grow(-count);
		}
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CopyingRecipe> {

		private Serializer() {
			setRegistryName(FluxMod.MODID, "copying");
		}

		@Override
		public CopyingRecipe read(ResourceLocation recipeId, JsonObject json) {
			if (!json.has("source") || !json.has("material")) {
				throw new JsonSyntaxException("Missing source and/or material key");
			}
			Ingredient source = Ingredient.deserialize(json.get("source"));
			Ingredient material = Ingredient.deserialize(json.get("material"));
			int process = JSONUtils.getInt(json, "processtime", 200);
			String group = JSONUtils.getString(json, "group", "");
			return new CopyingRecipe(recipeId, group, source, material, process);
		}

		@Nullable
		@Override
		public CopyingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			String grp = buffer.readString(32767);
			Ingredient source = Ingredient.read(buffer);
			Ingredient material = Ingredient.read(buffer);
			int process = buffer.readVarInt();
			return new CopyingRecipe(recipeId, grp, source, material, process);
		}

		@Override
		public void write(PacketBuffer buffer, CopyingRecipe recipe) {
			buffer.writeString(recipe.getGroup());
			recipe.source.write(buffer);
			recipe.material.write(buffer);
			buffer.writeVarInt(recipe.processTime);
		}
	}
}
