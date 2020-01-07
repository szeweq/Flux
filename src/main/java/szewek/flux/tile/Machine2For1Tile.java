package szewek.flux.tile;

import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import szewek.flux.MFTiles;
import szewek.flux.recipe.AbstractMachineRecipe;

import java.util.function.Function;

public class Machine2For1Tile extends AbstractMachineTile {
	private static final int[] SLOTS_UP = new int[]{0}, SLOTS_SIDE = new int[]{1}, SLOTS_DOWN = new int[]{2};

	private final String titleId;

	public static Function<MFTiles.TileType<Machine2For1Tile>, Machine2For1Tile> make(IRecipeType<? extends AbstractMachineRecipe> recipeType, MenuFactory factory, String titleName) {
		final String titleId = "container.flux." + titleName;
		return (type) -> new Machine2For1Tile(type, recipeType, factory, titleId);
	}

	public Machine2For1Tile(TileEntityType<?> typeIn, IRecipeType<? extends AbstractMachineRecipe> recipeTypeIn, MenuFactory factory, String titleIdIn) {
		super(typeIn, recipeTypeIn, factory, 2, 1);
		titleId = titleIdIn;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		switch (side) {
			case UP: return SLOTS_UP;
			case DOWN: return SLOTS_DOWN;
			default: return SLOTS_SIDE;
		}
	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent(titleId);
	}
}
