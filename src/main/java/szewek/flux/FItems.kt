package szewek.flux

import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraftforge.registries.IForgeRegistry
import szewek.flux.block.FluxOreBlock
import szewek.flux.block.MetalBlock
import szewek.flux.item.FluxToolItem
import szewek.flux.item.GiftItem
import szewek.flux.item.MetalItem
import szewek.flux.util.Metal
import szewek.ktutils.asStack
import java.util.*

object FItems {
    @JvmField val MF_ITEMS: ItemGroup = object : ItemGroup("flux.items") {
        override fun createIcon() = FLUXGEN.asStack
    }
    @JvmField val GRITS = metalMap("grit", Metal::always)
    @JvmField val DUSTS = metalMap("dust", Metal::always)
    @JvmField val INGOTS = metalMap("ingot", Metal::nonVanilla)
    @JvmField val FLUXTOOL = create(::FluxToolItem, "mftool", Item.Properties().maxStackSize(1))
    @JvmField val GIFT = create(::GiftItem, "gift", Item.Properties().maxStackSize(1))
    @JvmField val MACHINE_BASE = create(::Item, "machine_base", Item.Properties())
    @JvmField val FLUXGEN = fromBlock(FBlocks.FLUXGEN, "fluxgen")
    @JvmField val GRINDING_MILL = fromBlock(FBlocks.GRINDING_MILL, "grinding_mill")
    @JvmField val ALLOY_CASTER = fromBlock(FBlocks.ALLOY_CASTER, "alloy_caster")
    @JvmField val WASHER = fromBlock(FBlocks.WASHER, "washer")
    @JvmField val COMPACTOR = fromBlock(FBlocks.COMPACTOR, "compactor")
    @JvmField val ENERGY_CABLE = fromBlock(FBlocks.ENERGY_CABLE, "energy_cable")
    @JvmField val DIGGER = fromBlock(FBlocks.DIGGER, "digger")

    @JvmStatic
    fun register(reg: IForgeRegistry<Item?>) {
        GRITS.values.forEach(reg::register)
        DUSTS.values.forEach(reg::register)
        INGOTS.values.forEach(reg::register)
        FBlocks.ORES.forEach { (name: Metal, b: FluxOreBlock) -> reg.register(fromBlock(b, name.metalName + "_ore")) }
        FBlocks.METAL_BLOCKS.forEach { (name: Metal, b: MetalBlock) -> reg.register(fromBlock(b, name.metalName + "_block")) }
        reg.registerAll(
                FLUXTOOL, GIFT, MACHINE_BASE,
                FLUXGEN, GRINDING_MILL, ALLOY_CASTER, WASHER, COMPACTOR, ENERGY_CABLE, DIGGER
        )
    }

    private fun <T : Item?> create(factory: (Item.Properties) -> T, name: String, props: Item.Properties): T {
        val item = factory.invoke(props.group(MF_ITEMS))
        item!!.setRegistryName(FluxMod.MODID, name)
        return item
    }

    private fun metalMap(type: String, filter: (Metal) -> Boolean): Map<Metal, MetalItem> {
        val m: MutableMap<Metal, MetalItem> = EnumMap(Metal::class.java)
        for (metal in Metal.values()) {
            if (filter.invoke(metal)) {
                m[metal] = create(::MetalItem, metal.metalName + '_' + type, Item.Properties()).withMetal(metal)
            }
        }
        return m
    }

    private fun fromBlock(b: Block, name: String): BlockItem {
        val item = BlockItem(b, Item.Properties().group(MF_ITEMS))
        item.setRegistryName(FluxMod.MODID, name)
        return item
    }
}