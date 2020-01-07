package szewek.flux.tile;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.IIntArray;

@FunctionalInterface
public interface MenuFactory {
	Container create(int id, PlayerInventory player, IInventory inv, IIntArray data);
}
