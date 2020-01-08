package szewek.flux;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.registries.IForgeRegistry;
import szewek.flux.block.*;
import szewek.flux.util.Metal;

import java.util.EnumMap;
import java.util.Map;

import static szewek.flux.FluxMod.MODID;

public final class FBlocks {
	static final Map<Metal, FluxOreBlock> ORES = makeOres();
	static final Map<Metal, MetalBlock> METAL_BLOCKS = makeBlocks();

	public static final FluxGenBlock FLUXGEN = new FluxGenBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(1f));
	public static final EnergyCableBlock ENERGY_CABLE = new EnergyCableBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(0.3f));
	public static final MachineBlock
			GRINDING_MILL = new MachineBlock(() -> FTiles.GRINDING_MILL),
			ALLOY_CASTER = new MachineBlock(() -> FTiles.ALLOY_CASTER),
			WASHER = new MachineBlock(() -> FTiles.WASHER),
			COMPACTOR = new MachineBlock(() -> FTiles.COMPACTOR);

	static void register(final IForgeRegistry<Block> reg) {
		ORES.values().forEach(reg::register);
		METAL_BLOCKS.values().forEach(reg::register);
		reg.registerAll(
				FLUXGEN.setRegistryName(MODID, "fluxgen"),
				ENERGY_CABLE.setRegistryName(MODID, "energy_cable"),
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
