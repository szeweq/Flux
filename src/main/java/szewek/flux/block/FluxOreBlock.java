package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import szewek.flux.util.Metal;

public class FluxOreBlock extends Block {
	private final Metal metal;
	public FluxOreBlock(Metal metal) {
		super(Block.Properties.create(Material.ROCK).harvestTool(ToolType.PICKAXE).hardnessAndResistance(3, 3).harvestLevel(metal.harvestLevel));
		this.metal = metal;
	}
}
