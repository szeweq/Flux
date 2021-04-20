package szewek.flux.container;

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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import szewek.fl.util.ConsumerUtil;
import szewek.flux.F;
import szewek.flux.data.FluxGenValues;

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
		ConsumerUtil.addPlayerSlotsAt(pinv, 8, 84, this::addSlot);
		addDataSlots(extra);
	}

	public float getWorkFill() {
		final int maxWork = data.get(7);
		return maxWork == 0 ? 0 : (float) data.get(6) / (float) maxWork;
	}

	private int getEnergy() {
		return (data.get(0) << 16) + data.get(1);
	}

	public float getEnergyFill() {
		return (float) getEnergy() / (float) 1e6;
	}

	public ITextComponent energyText() {
		return new StringTextComponent(getEnergy() + " / 1000000 F");
	}

	public ITextComponent genText() {
		return new TranslationTextComponent("flux.gen", data.get(8));
	}

	public FluidStack getHotFluid() {
		return new FluidStack(((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getValue(data.get(2)), data.get(3));
	}
	public FluidStack getColdFluid() {
		return new FluidStack(((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getValue(data.get(4)), data.get(5));
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return fluxGenInv.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity p, int index) {
		ItemStack nis = ItemStack.EMPTY;
		Slot sl = slots.get(index);
		if (sl != null && sl.hasItem()) {
			ItemStack bis = sl.getItem();
			nis = bis.copy();
			int s = 2;
			int e = 38;
			if (index > 1) {
				if (ForgeHooks.getBurnTime(bis) > 0) {
					s = 0;
					e = 1;
				} else if (FluxGenValues.CATALYSTS.has(bis.getItem())) {
					s = 1;
					e = 2;
				} else if (index < 29) {
					s = 29;
				} else if (index < 38) {
					e = 29;
				}
			}

			if (!moveItemStackTo(bis, s, e, false)) {
				return ItemStack.EMPTY;
			}

			if (nis.isEmpty()) {
				sl.set(ItemStack.EMPTY);
			} else {
				sl.setChanged();
			}

			if (nis.getCount() == bis.getCount()) {
				return ItemStack.EMPTY;
			}

			sl.onTake(p, nis);
		}
		return nis;
	}
}