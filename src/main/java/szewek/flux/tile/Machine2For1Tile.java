package szewek.flux.tile;

import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import szewek.fl.type.FluxTileType;
import szewek.fl.type.FluxContainerType;
import szewek.flux.container.Machine2For1Container;
import szewek.flux.recipe.AbstractMachineRecipe;

import java.util.function.Function;

public final class Machine2For1Tile extends AbstractMachineTile {
	private final String titleId;
	private static final int[] SLOTS_UP = new int[]{0};
	private static final int[] SLOTS_SIDE = new int[]{1};
	private static final int[] SLOTS_DOWN = new int[]{2};

	public Machine2For1Tile(TileEntityType<?> typeIn, IRecipeType<? extends AbstractMachineRecipe> recipeTypeIn, MenuFactory factory, String titleId) {
		super(typeIn, recipeTypeIn, factory, 2, 1);
		this.titleId = titleId;
	}

	public int[] getSlotsForFace(Direction side) {
		switch (side) {
			case UP:
				return SLOTS_UP;
			case DOWN:
				return SLOTS_DOWN;
			default:
				return SLOTS_SIDE;
		}
	}

	public ITextComponent getDefaultName() {
		return new TranslationTextComponent(this.titleId);
	}

	public static Function<FluxTileType<Machine2For1Tile>, Machine2For1Tile> make(final IRecipeType<? extends AbstractMachineRecipe> recipeType, final FluxContainerType<Machine2For1Container> ctype, String titleName) {
		final String titleId = "container.flux." + titleName;
		return type -> new Machine2For1Tile(type, recipeType, (id, player, inv, data) -> new Machine2For1Container(ctype, recipeType, id, player, inv, data), titleId);
	}
}
