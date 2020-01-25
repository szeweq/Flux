package szewek.flux.container;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraftforge.common.ForgeHooks;
import szewek.flux.F;
import szewek.flux.recipe.FluxGenRecipes;

public class FluxGenContainer extends Container {
	private final IInventory fluxGenInv;
	private final IIntArray extraData;

	public FluxGenContainer(int i, PlayerInventory pinv, PacketBuffer data) {
		this(i, pinv, new Inventory(2), new IntArray(5));
	}

	public FluxGenContainer(int i, PlayerInventory pinv, IInventory iinv, IIntArray extra) {
		super(F.C.FLUXGEN, i);
		fluxGenInv = iinv;
		extraData = extra;

		addSlot(new Slot(fluxGenInv, 0, 67, 35));
		addSlot(new Slot(fluxGenInv, 1, 93, 35));
		int xBase;
		int yBase = 84;
		for (int y = 0; y < 3; y++) {
			xBase = 8;
			for (int x = 0; x < 9; x++) {
				addSlot(new Slot(pinv, x + 9 * y + 9, xBase, yBase));
				xBase += 18;
			}
			yBase += 18;
		}
		xBase = 8;
		yBase += 4;
		for (int w = 0; w < 9; w++) {
			addSlot(new Slot(pinv, w, xBase, yBase));
			xBase += 18;
		}
		trackIntArray(extra);
	}

	public float getWorkFill() {
		int maxWork = extraData.get(2);
		return maxWork == 0 ? 0 : (float) extraData.get(1) / (float) maxWork;
	}

	public float getEnergyFill() {
		return (float) extraData.get(0) / (float) 1e6;
	}

	public String energyText() {
		return extraData.get(0) + " / 1000000 F";
	}

	public String genText() {
		return I18n.format("flux.gen", extraData.get(3));
	}

	@Override
	public boolean canInteractWith(PlayerEntity player) {
		return fluxGenInv.isUsableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity p, int index) {
		ItemStack nis = ItemStack.EMPTY;
		Slot sl = inventorySlots.get(index);
		if (sl != null && sl.getHasStack()) {
			ItemStack bis = sl.getStack();
			nis = bis.copy();
			int s = 2;
			int e = 38;
			if (index > 1) {
				if (ForgeHooks.getBurnTime(bis) > 0) {
					s = 0;
					e = 1;
				} else if (FluxGenRecipes.isCatalyst(bis.getItem())) {
					s = 1;
					e = 2;
				} else if (index < 29) {
					s = 29;
				} else if (index < 38) {
					e = 29;
				}
			}

			if (!this.mergeItemStack(bis, s, e, false)) {
				return ItemStack.EMPTY;
			}

			if (nis.isEmpty()) {
				sl.putStack(ItemStack.EMPTY);
			} else {
				sl.onSlotChanged();
			}

			if (nis.getCount() == bis.getCount()) {
				return ItemStack.EMPTY;
			}

			sl.onTake(p, nis);
		}
		return nis;
	}
}