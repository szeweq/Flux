package szewek.fl.type;

import com.mojang.datafixers.types.Type;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import java.util.Set;
import java.util.function.Function;

/**
 * Tile entity type used in Flux mod(s).
 * This makes it easy to assign a specific type to different instances of same tile entity class.
 * @param <T>
 */
public class FluxTileType<T extends TileEntity> extends TileEntityType<T> {
	private final Function<FluxTileType<T>, T> builder;

	public FluxTileType(Function<FluxTileType<T>, T> builder, Set<Block> validBlocksIn, Type<?> dataFixerType) {
		super(null, validBlocksIn, dataFixerType);
		this.builder = builder;
	}

	@Override
	public T create() {
		return builder.apply(this);
	}
}
