package szewek.fl.util;

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

/**
 * Utility class for interacting with items.
 */
public final class ItemsUtil {
	private static final Direction[] DIRS = Direction.values();

	public static void trySendingItems(final Iterable<ItemStack> items, World world, BlockPos pos) {
		final IItemHandler[] inv = new IItemHandler[6];
		int size = 0;
		for (Direction dir : DIRS) {
			TileEntity te = world.getTileEntity(pos.offset(dir));
			if (te != null) {
				IItemHandler iih = getItemHandlerCompat(te, dir.getOpposite());
				if (iih != null) {
					inv[size++] = iih;
				}
			}
		}

		itemloop: for (ItemStack stack : items) {
			ItemStack tempStack = stack;
			for (int z = 0; z < size; z++) {
				IItemHandler iih = inv[z];
				int l = iih.getSlots();
				for(int i = 0; i < l; ++i) {
					if (iih.isItemValid(i, tempStack)) {
						tempStack = iih.insertItem(i, tempStack, false);
						if (tempStack.isEmpty()) {
							continue itemloop;
						}
					}
				}
			}
			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY()+1, pos.getZ(), tempStack);
		}
	}

	public static IItemHandler getItemHandlerCompat(TileEntity tile, Direction dir) {
		final LazyOptional<IItemHandler> il = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
		IItemHandler iih = null;
		if (il.isPresent()) {
			//noinspection ConstantConditions
			iih = il.orElse(null);
		} else if (tile instanceof IInventory) {
			iih = new InvWrapper((IInventory) tile);
		}
		return iih;
	}

	private ItemsUtil() {}
}
