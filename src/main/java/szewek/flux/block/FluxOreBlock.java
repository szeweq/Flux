package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import szewek.flux.util.metals.Metal;

public final class FluxOreBlock extends Block {
	public FluxOreBlock(Metal metal) {
		super(Block.Properties.of(Material.METAL).harvestTool(ToolType.PICKAXE).strength(3f, 3f).harvestLevel(metal.harvestLevel));
	}
}
