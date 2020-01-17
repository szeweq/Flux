package szewek.flux.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public final class ItemsUtil {
	public static void trySendingItems(List<ItemStack> items, World world, BlockPos pos) {
		long ns = System.nanoTime();
		List<IItemHandler> inv = new ArrayList<>();
		for (Direction dir : Direction.values()) {
			if (dir != Direction.DOWN) {
				TileEntity te = world.getTileEntity(pos.offset(dir));
				if (te != null) {
					IItemHandler iih;
					LazyOptional<IItemHandler> il = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
					if (il.isPresent()) {
						iih = il.orElse(null);
					} else {
						if (!(te instanceof IInventory)) continue;
						iih = new InvWrapper((IInventory)te);
					}
					inv.add(iih);
				}
			}
		}

		itemloop: for (ItemStack stack : items) {
			for (IItemHandler iih : inv) {
				int l = iih.getSlots();
				for(int i = 0; i < l; ++i) {
					if (iih.isItemValid(i, stack)) {
						stack = iih.insertItem(i, stack, false);
					}
				}
				if (stack.isEmpty()) continue itemloop;
			}
			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY()+1, pos.getZ(), stack);
		}
	}

	private ItemsUtil() {}
}
