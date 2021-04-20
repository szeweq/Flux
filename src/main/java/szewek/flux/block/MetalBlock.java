package szewek.flux.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import szewek.flux.util.metals.Metal;

public final class MetalBlock extends Block {
	public MetalBlock(AbstractBlock.Properties props, Metal metal) {
		super(props.harvestTool(ToolType.PICKAXE).strength(3f, 3f).harvestLevel(metal.harvestLevel));
	}
}
