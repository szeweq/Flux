package szewek.flux.tile;

import com.mojang.datafixers.types.Type;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import java.util.Set;
import java.util.function.Function;

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
