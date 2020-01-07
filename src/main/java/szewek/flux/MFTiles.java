package szewek.flux;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.types.Type;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.IForgeRegistry;
import szewek.flux.container.AlloyCasterContainer;
import szewek.flux.container.CompactorContainer;
import szewek.flux.container.GrindingMillContainer;
import szewek.flux.container.WasherContainer;
import szewek.flux.tile.*;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static szewek.flux.FluxMod.MODID;

public final class MFTiles {
	public static final TileEntityType<FluxGenTile> FLUXGEN = create(FluxGenTile::new, "fluxgen", MFBlocks.FLUXGEN);
	public static final TileType<Machine2For1Tile>
			GRINDING_MILL = create(Machine2For1Tile.make(MFRecipes.GRINDING, GrindingMillContainer::new, "grinding_mill"), "grinding_mill", MFBlocks.GRINDING_MILL),
			ALLOY_CASTER = create(Machine2For1Tile.make(MFRecipes.ALLOYING, AlloyCasterContainer::new, "alloy_caster"), "alloy_caster", MFBlocks.ALLOY_CASTER),
			WASHER = create(Machine2For1Tile.make(MFRecipes.WASHING, WasherContainer::new, "washer"), "washer", MFBlocks.WASHER),
			COMPACTOR = create(Machine2For1Tile.make(MFRecipes.COMPACTING, CompactorContainer::new, "compactor"), "compactor", MFBlocks.COMPACTOR);

	static void register(final IForgeRegistry<TileEntityType<?>> reg) {
		reg.registerAll(FLUXGEN, GRINDING_MILL, ALLOY_CASTER, WASHER, COMPACTOR);
	}

	private static <T extends TileEntity> TileEntityType<T> create(Supplier<T> factory, String name, Block b) {
		TileEntityType<T> type = new TileEntityType<>(factory, ImmutableSet.of(b), null);
		type.setRegistryName(MODID, name);
		return type;
	}

	private static <T extends TileEntity> TileType<T> create(Function<TileType<T>, T> builder, String name, Block b) {
		TileType<T> type = new TileType<>(builder, ImmutableSet.of(b), null);
		type.setRegistryName(MODID, name);
		return type;
	}

	public static class TileType<T extends TileEntity> extends TileEntityType<T> {
		private final Function<TileType<T>, T> builder;

		public TileType(Function<TileType<T>, T> builder, Set<Block> validBlocksIn, Type<?> dataFixerType) {
			super(null, validBlocksIn, dataFixerType);
			this.builder = builder;
		}

		@Nullable
		@Override
		public T create() {
			return builder.apply(this);
		}
	}
}
