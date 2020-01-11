package szewek.flux

import com.google.common.collect.ImmutableSet
import com.mojang.datafixers.types.Type
import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraftforge.registries.IForgeRegistry
import szewek.flux.container.AlloyCasterContainer
import szewek.flux.container.CompactorContainer
import szewek.flux.container.GrindingMillContainer
import szewek.flux.container.WasherContainer
import szewek.flux.tile.DiggerTile
import szewek.flux.tile.EnergyCableTile
import szewek.flux.tile.FluxGenTile
import szewek.flux.tile.Machine2For1Tile

object FTiles {
    @JvmField
    val FLUXGEN = create(::FluxGenTile, "fluxgen", FBlocks.FLUXGEN)
    @JvmField
    val ENERGY_CABLE = create(::EnergyCableTile, "energy_cable", FBlocks.ENERGY_CABLE)
    @JvmField
    val DIGGER = create(::DiggerTile, "digger", FBlocks.DIGGER)
    @JvmField
    val GRINDING_MILL = createTyped(Machine2For1Tile.make(FRecipes.GRINDING, ::GrindingMillContainer, "grinding_mill"), "grinding_mill", FBlocks.GRINDING_MILL)
    @JvmField
    val ALLOY_CASTER = createTyped(Machine2For1Tile.make(FRecipes.ALLOYING, ::AlloyCasterContainer, "alloy_caster"), "alloy_caster", FBlocks.ALLOY_CASTER)
    @JvmField
    val WASHER = createTyped(Machine2For1Tile.make(FRecipes.WASHING, ::WasherContainer, "washer"), "washer", FBlocks.WASHER)
    @JvmField
    val COMPACTOR = createTyped(Machine2For1Tile.make(FRecipes.COMPACTING, ::CompactorContainer, "compactor"), "compactor", FBlocks.COMPACTOR)

    @JvmStatic
    fun register(reg: IForgeRegistry<TileEntityType<*>?>) {
        reg.registerAll(FLUXGEN, ENERGY_CABLE, DIGGER, GRINDING_MILL, ALLOY_CASTER, WASHER, COMPACTOR)
    }

    private fun <T : TileEntity> create(factory: () -> T, name: String, b: Block): TileEntityType<T> {
        val type = TileEntityType(factory, ImmutableSet.of(b), null)
        type.setRegistryName(FluxMod.MODID, name)
        return type
    }

    private fun <T : TileEntity> createTyped(builder: (TileType<T>) -> T, name: String, b: Block): TileType<T> {
        val type = TileType(builder, ImmutableSet.of(b), null)
        type.setRegistryName(FluxMod.MODID, name)
        return type
    }

    class TileType<T : TileEntity>(private val builder: (TileType<T>) -> T, validBlocksIn: MutableSet<Block>, dataFixerType: Type<*>?) : TileEntityType<T>(null, validBlocksIn, dataFixerType) {
        override fun create() = builder.invoke(this)

    }
}