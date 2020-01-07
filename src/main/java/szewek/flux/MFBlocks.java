package szewek.flux;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.registries.IForgeRegistry;
import szewek.flux.block.FluxGenBlock;
import szewek.flux.block.FluxOreBlock;
import szewek.flux.block.MachineBlock;
import szewek.flux.block.MetalBlock;
import szewek.flux.util.Metal;

import java.util.EnumMap;
import java.util.Map;

import static szewek.flux.FluxMod.MODID;

public final class MFBlocks {
	static final Map<Metal, FluxOreBlock> ORES = makeOres();
	static final Map<Metal, MetalBlock> METAL_BLOCKS = makeBlocks();

	public static final FluxGenBlock FLUXGEN = new FluxGenBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(1f));
	public static final MachineBlock
			GRINDING_MILL = new MachineBlock(() -> MFTiles.GRINDING_MILL),
			ALLOY_CASTER = new MachineBlock(() -> MFTiles.ALLOY_CASTER),
			WASHER = new MachineBlock(() -> MFTiles.WASHER),
			COMPACTOR = new MachineBlock(() -> MFTiles.COMPACTOR);

	static void register(final IForgeRegistry<Block> reg) {
		ORES.values().forEach(reg::register);
		METAL_BLOCKS.values().forEach(reg::register);
		reg.registerAll(
				FLUXGEN.setRegistryName(MODID, "fluxgen"),
				GRINDING_MILL.setRegistryName(MODID, "grinding_mill"),
				ALLOY_CASTER.setRegistryName(MODID, "alloy_caster"),
				WASHER.setRegistryName(MODID, "washer"),
				COMPACTOR.setRegistryName(MODID, "compactor")
		);
	}

	private static Map<Metal, FluxOreBlock> makeOres() {
		Map<Metal, FluxOreBlock> m = new EnumMap<>(Metal.class);
		for (Metal metal : Metal.values()) {
			if (Metal.nonVanilla(metal)) {
				FluxOreBlock b = new FluxOreBlock(metal);
				b.setRegistryName(MODID, metal.name + "_ore");
				m.put(metal, b);
			}
		}
		return m;
	}

	private static Map<Metal, MetalBlock> makeBlocks() {
		Map<Metal, MetalBlock> m = new EnumMap<>(Metal.class);
		for (Metal metal : Metal.values()) {
			if (Metal.nonVanilla(metal)) {
				MetalBlock b = new MetalBlock(Block.Properties.create(Material.IRON), metal);
				b.setRegistryName(MODID, metal.name + "_block");
				m.put(metal, b);
			}
		}
		return m;
	}
}
