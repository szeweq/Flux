package szewek.flux.item;

import net.minecraft.item.Item;
import szewek.flux.util.metals.Metal;

public final class MetalItem extends Item {
	public Metal metal;

	public MetalItem(Properties properties, Metal metal) {
		super(properties);
		this.metal = metal;
	}
}
