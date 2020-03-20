package szewek.fl.type;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Container type used in Flux mod(s).
 * This makes it easy to assign a specific type to different instances of same container class.
 * @param <T> Container class
 */
public class FluxContainerType<T extends Container> extends ContainerType<T> {
	private final IContainerBuilder<T> builder;

	public FluxContainerType(IContainerBuilder<T> builder) {
		//noinspection ConstantConditions
		super(null);
		this.builder = builder;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public T create(int windowId, PlayerInventory player) {
		return builder.create(this, windowId, player, null);
	}

	@Override
	public T create(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
		return builder.create(this, windowId, playerInv, extraData);
	}

	public interface IContainerBuilder<T extends Container> {
		T create(FluxContainerType<T> type, int windowId, PlayerInventory inv, @Nullable PacketBuffer data);
	}
}
