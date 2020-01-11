package szewek.flux

import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraftforge.registries.IForgeRegistry
import szewek.flux.block.*
import szewek.flux.util.Metal
import szewek.ktutils.blockProps
import java.util.*

object FBlocks {
    @JvmField val ORES = makeOres()
    @JvmField val METAL_BLOCKS = makeBlocks()
    @JvmField val FLUXGEN = FluxGenBlock(blockProps(Material.IRON) {
        hardnessAndResistance(1f)
        sound(SoundType.METAL)
    })
    @JvmField val ENERGY_CABLE = EnergyCableBlock(blockProps(Material.IRON) {
        hardnessAndResistance(0.3f)
    })
    @JvmField val DIGGER = DiggerBlock(blockProps(Material.IRON) {
        hardnessAndResistance(1f)
        sound(SoundType.METAL)
    })
    @JvmField val GRINDING_MILL = MachineBlock()
    @JvmField val ALLOY_CASTER = MachineBlock()
    @JvmField val WASHER = MachineBlock()
    @JvmField val COMPACTOR = MachineBlock()
    @JvmStatic
    fun register(reg: IForgeRegistry<Block?>) {
        ORES.values.forEach { value: FluxOreBlock? -> reg.register(value) }
        METAL_BLOCKS.values.forEach { value: MetalBlock? -> reg.register(value) }
        reg.registerAll(
                FLUXGEN.setRegistryName(FluxMod.MODID, "fluxgen"),
                ENERGY_CABLE.setRegistryName(FluxMod.MODID, "energy_cable"),
                DIGGER.setRegistryName(FluxMod.MODID, "digger"),
                GRINDING_MILL.setRegistryName(FluxMod.MODID, "grinding_mill"),
                ALLOY_CASTER.setRegistryName(FluxMod.MODID, "alloy_caster"),
                WASHER.setRegistryName(FluxMod.MODID, "washer"),
                COMPACTOR.setRegistryName(FluxMod.MODID, "compactor")
        )
    }

    private fun makeOres(): Map<Metal, FluxOreBlock> {
        val m: MutableMap<Metal, FluxOreBlock> = EnumMap<Metal, FluxOreBlock>(Metal::class.java)
        for (metal in Metal.values()) {
            if (metal.nonVanilla()) {
                val b = FluxOreBlock(metal)
                b.setRegistryName(FluxMod.MODID, metal.metalName + "_ore")
                m[metal] = b
            }
        }
        return m
    }

    private fun makeBlocks(): Map<Metal, MetalBlock> {
        val m: MutableMap<Metal, MetalBlock> = EnumMap<Metal, MetalBlock>(Metal::class.java)
        for (metal in Metal.values()) {
            if (metal.nonVanilla()) {
                val b = MetalBlock(metal)
                b.setRegistryName(FluxMod.MODID, metal.metalName + "_block")
                m[metal] = b
            }
        }
        return m
    }
}