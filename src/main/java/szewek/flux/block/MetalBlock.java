package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import szewek.flux.util.Metal;

public class MetalBlock extends Block {
	public MetalBlock(Properties properties, Metal metal) {
		super(properties.harvestLevel(metal.harvestLevel).sound(SoundType.METAL));
	}
}
