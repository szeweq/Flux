package szewek.flux.tile;

import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import szewek.fl.type.FluxContainerType;
import szewek.fl.type.FluxTileType;
import szewek.flux.container.Machine2For1Container;
import szewek.flux.recipe.AbstractMachineRecipe;

import java.util.function.Function;

public final class Machine2For1Tile extends AbstractMachineTile {
	private final String titleId;
	private static final int[] SLOTS_TOP_BOTTOM = {0, 2};
	private static final int[] SLOTS_SIDE = {1, 2};

	public Machine2For1Tile(TileEntityType<?> typeIn, IRecipeType<? extends AbstractMachineRecipe> recipeTypeIn, MenuFactory factory, String titleId) {
		super(typeIn, recipeTypeIn, factory, 2, 1);
		this.titleId = titleId;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		switch (side) {
			case UP:
			case DOWN:
				return SLOTS_TOP_BOTTOM;
			default:
				return SLOTS_SIDE;
		}
	}

	@Override
	public ITextComponent getDefaultName() {
		return new TranslationTextComponent(this.titleId);
	}

	public static Function<FluxTileType<Machine2For1Tile>, Machine2For1Tile> make(final IRecipeType<? extends AbstractMachineRecipe> recipeType, final FluxContainerType<Machine2For1Container> ctype, String titleName) {
		final String titleId = "container.flux." + titleName;
		return type -> new Machine2For1Tile(type, recipeType, (id, player, inv, data) -> new Machine2For1Container(ctype, recipeType, id, player, inv, data), titleId);
	}
}
