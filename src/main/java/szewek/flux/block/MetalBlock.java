package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import szewek.flux.util.Metal;

public class MetalBlock extends Block {
	public MetalBlock(Metal metal) {
		super(Block.Properties.create(Material.IRON).harvestTool(ToolType.PICKAXE).hardnessAndResistance(3f, 3f).harvestLevel(metal.harvestLevel+1));
	}
}
