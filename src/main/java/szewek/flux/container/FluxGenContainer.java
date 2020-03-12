package szewek.flux.container;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import szewek.flux.F;
import szewek.flux.recipe.FluxGenRecipes;

public class FluxGenContainer extends Container {
	private final IInventory fluxGenInv;
	private final IIntArray data;

	public FluxGenContainer(int i, PlayerInventory pinv, PacketBuffer data) {
		this(i, pinv, new Inventory(2), new IntArray(10));
	}

	public FluxGenContainer(int i, PlayerInventory pinv, IInventory iinv, IIntArray extra) {
		super(F.C.FLUXGEN, i);
		fluxGenInv = iinv;
		data = extra;

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
		final int maxWork = data.get(3);
		return maxWork == 0 ? 0 : (float) data.get(2) / (float) maxWork;
	}

	private int getEnergy() {
		return (data.get(0) << 16) + data.get(1);
	}

	public float getEnergyFill() {
		return (float) getEnergy() / (float) 1e6;
	}

	public String energyText() {
		return getEnergy() + " / 1000000 F";
	}

	public String genText() {
		return I18n.format("flux.gen", data.get(4));
	}

	public FluidStack getHotFluid() {
		return new FluidStack(((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getValue(data.get(6)), data.get(7));
	}
	public FluidStack getColdFluid() {
		return new FluidStack(((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getValue(data.get(8)), data.get(9));
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