package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import szewek.flux.util.metals.Metal;

public final class MetalBlock extends Block {
	public MetalBlock(Metal metal, Material mat) {
		super(Block.Properties.create(mat).harvestTool(ToolType.PICKAXE).hardnessAndResistance(3f, 3f).harvestLevel(metal.harvestLevel));
	}
}
