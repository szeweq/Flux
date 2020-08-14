package szewek.flux.util.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.AbstractList;
import java.util.Arrays;

public class MachineInventory extends AbstractList<ItemStack> {
	public final int in, out;
	public final ItemStack[] stacks;

	public MachineInventory(IOSize ioSize, int extra) {
		in = ioSize.in;
		out = ioSize.out;
		stacks = new ItemStack[in + out + extra];
		Arrays.fill(stacks, ItemStack.EMPTY);
	}

	public void readNBT(CompoundNBT tag) {
		Arrays.fill(stacks, ItemStack.EMPTY);
		ListNBT listnbt = tag.getList("Items", 10);

		for(int i = 0; i < listnbt.size(); ++i) {
			CompoundNBT compoundnbt = listnbt.getCompound(i);
			int j = compoundnbt.getByte("Slot") & 255;
			if (j < stacks.length) {
				stacks[j] = ItemStack.read(compoundnbt);
			}
		}
	}

	public void writeNBT(CompoundNBT tag, boolean saveEmpty) {
		ListNBT listnbt = new ListNBT();

		for (int i = 0; i < stacks.length; ++i) {
			ItemStack itemstack = stacks[i];
			if (!itemstack.isEmpty()) {
				CompoundNBT compoundnbt = new CompoundNBT();
				compoundnbt.putByte("Slot", (byte)i);
				itemstack.write(compoundnbt);
				listnbt.add(compoundnbt);
			}
		}

		if (!listnbt.isEmpty() || saveEmpty) {
			tag.put("Items", listnbt);
		}
	}

	@Override
	public ItemStack get(int index) {
		return stacks[index];
	}

	@Override
	public ItemStack set(int index, ItemStack element) {
		ItemStack old = stacks[index];
		stacks[index] = element;
		return old;
	}

	@Override
	public int size() {
		return stacks.length;
	}

	@Override
	public void clear() {
		Arrays.fill(stacks, ItemStack.EMPTY);
	}

	public ItemStack getAndSplit(int index, int amount) {
		return index >= 0 && index < stacks.length && !stacks[index].isEmpty() && amount > 0 ? stacks[index].split(amount) : ItemStack.EMPTY;
	}

	public ItemStack getAndRemove(int index) {
		if (index >= 0 && index < stacks.length) {
			ItemStack stack = stacks[index];
			stacks[index] = ItemStack.EMPTY;
			return stack;
		}
		return ItemStack.EMPTY;
	}

	public boolean isEmpty() {
		for (ItemStack stack : stacks) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean inputHasStacks() {
		for (int i = 0; i < in; i++) {
			if (!stacks[i].isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public boolean checkResult(ItemStack result) {
		for (int i = in; i < in + out; i++) {
			ItemStack outputStack = stacks[i];
			if (outputStack.isEmpty()) {
				return true;
			}
			if (!outputStack.isItemEqual(result)) {
				return false;
			}
			int minStackSize = Math.min(Math.min(64, outputStack.getMaxStackSize()), result.getMaxStackSize());
			if (outputStack.getCount() + result.getCount() <= minStackSize) {
				return true;
			}
		}
		return false;
	}

	public void placeResult(ItemStack result) {
		for (int i = in; i < in + out; i++) {
			ItemStack outputStack = stacks[i];
			if (outputStack.isEmpty()) {
				ItemStack copyResult = result.copy();
				stacks[i] = copyResult;
				break;
			} else if (outputStack.getItem() == result.getItem()) {
				outputStack.grow(result.getCount());
				break;
			}
		}
	}

	public Iterable<ItemStack> iterableInput() {
		return subList(0, in);
	}
}
