package szewek.flux.container;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IRecipeHelperPopulator;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import szewek.flux.item.ChipItem;
import szewek.flux.recipe.AbstractMachineRecipe;
import szewek.flux.util.ServerRecipePlacerMachine;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractMachineContainer extends RecipeBookContainer<IInventory> {
	protected final IInventory machineInventory;
	private final IIntArray data;
	protected final World world;
	public final IRecipeType<? extends AbstractMachineRecipe> recipeType;
	private final int inputSize;
	private final int outputSize;

	protected AbstractMachineContainer(ContainerType containerTypeIn, IRecipeType<? extends AbstractMachineRecipe> recipeType, int id, PlayerInventory playerInventoryIn, int inputSize, int outputSize) {
		this(containerTypeIn, recipeType, id, playerInventoryIn, inputSize, outputSize, new Inventory(inputSize+outputSize+1), new IntArray(6));
	}

	protected AbstractMachineContainer(ContainerType containerTypeIn, IRecipeType<? extends AbstractMachineRecipe> recipeType, int id, PlayerInventory playerInventoryIn, int inputSize, int outputSize, IInventory machineInventoryIn, IIntArray dataIn) {
		super(containerTypeIn, id);
		this.recipeType = recipeType;
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		Container.assertInventorySize(machineInventoryIn, inputSize + outputSize + 1);
		Container.assertIntArraySize(dataIn, 6);
		machineInventory = machineInventoryIn;
		data = dataIn;
		world = playerInventoryIn.player.world;
		initSlots(playerInventoryIn);
		trackIntArray(data);
	}

	protected abstract void initSlots(PlayerInventory var1);

	protected final void initPlayerSlotsAt(PlayerInventory playerInventory, int x, int y) {
		int xBase = x;
		int i;

		for(i = 0; i < 3; ++i) {
			for(int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, 9 * i + j + 9, x, y));
				x += 18;
			}
			x = xBase;
			y += 18;
		}

		y += 4;
		for(i = 0; i < 9; ++i) {
			this.addSlot(new Slot(playerInventory, i, x, y));
			x += 18;
		}

	}

	@Override
	public void fillStackedContents(RecipeItemHelper helper) {
		if (machineInventory instanceof IRecipeHelperPopulator) {
			((IRecipeHelperPopulator) machineInventory).fillStackedContents(helper);
		}
	}

	@Override
	public void clear() {
		machineInventory.clear();
	}

	@Override
	public void func_217056_a(boolean placeAll, IRecipe<?> recipe, ServerPlayerEntity player) {
		//noinspection unchecked
		new ServerRecipePlacerMachine<>(this, inputSize, outputSize).place(player, (IRecipe<IInventory>) recipe, placeAll);
	}

	@Override
	public boolean matches(IRecipe<? super IInventory> recipeIn) {
		return recipeIn.getType() == recipeType && recipeIn.matches(machineInventory, world);
	}

	@Override
	public int getOutputSlot() {
		return inputSize;
	}

	@Override
	public int getWidth() {
		return inputSize;
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getSize() {
		return inputSize + 1;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return machineInventory.isUsableByPlayer(playerIn);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			stack = slotStack.copy();
			int s = inputSize + outputSize + 1;
			int e = s + 36;
			if (index >= s) {
				if (slotStack.getItem() instanceof ChipItem) {
					s = inputSize + outputSize;
					e = s + 1;
				} else {
					e = s;
					s = 0;
				}
			}

			if (!mergeItemStack(slotStack, s, e, false)) {
				return ItemStack.EMPTY;
			}

			if (index >= inputSize && index < inputSize + outputSize + 1) {
				slot.onSlotChange(slotStack, stack);
			}

			if (slotStack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (slotStack.getCount() == stack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, slotStack);
		}
		return stack;
	}

	@Override
	public List<RecipeBookCategories> getRecipeBookCategories() {
		return Arrays.asList(RecipeBookCategories.SEARCH, RecipeBookCategories.EQUIPMENT, RecipeBookCategories.BUILDING_BLOCKS, RecipeBookCategories.MISC, RecipeBookCategories.REDSTONE);
	}

	@OnlyIn(Dist.CLIENT)
	public final int processScaled() {
		int i = data.get(2);
		int j = data.get(3);
		return j == 0 || i == 0 ? 0 : i * 24 / j;
	}

	@OnlyIn(Dist.CLIENT)
	public final int energyScaled() {
		return getEnergy() * 54 / 1000000;
	}

	@OnlyIn(Dist.CLIENT)
	public final List<String> energyText() {
		return Arrays.asList(getEnergy() + " / " + 1000000 + " F", I18n.format("flux.usage", data.get(3)));
	}

	private int getEnergy() {
		return (data.get(0) << 16) + data.get(1);
	}

}
