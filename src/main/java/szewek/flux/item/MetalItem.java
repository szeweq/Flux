package szewek.flux.item;

import net.minecraft.item.Item;
import szewek.flux.util.Metal;

public class MetalItem extends Item {
	private Metal metal = null;

	public MetalItem(Properties properties) {
		super(properties);
	}

	public MetalItem withMetal(Metal metal) {
		if (this.metal == null)
			this.metal = metal;
		return this;
	}

	public Metal getMetal() {
		return metal;
	}
}
