package szewek.flux.container;

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
		this(containerTypeIn, recipeType, id, playerInventoryIn, inputSize, outputSize, new Inventory(inputSize+outputSize), new IntArray(4));
	}

	protected AbstractMachineContainer(ContainerType containerTypeIn, IRecipeType<? extends AbstractMachineRecipe> recipeType, int id, PlayerInventory playerInventoryIn, int inputSize, int outputSize, IInventory machineInventoryIn, IIntArray dataIn) {
		super(containerTypeIn, id);
		this.recipeType = recipeType;
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		Container.assertInventorySize(machineInventoryIn, this.inputSize + this.outputSize);
		Container.assertIntArraySize(dataIn, 4);
		machineInventory = machineInventoryIn;
		data = dataIn;
		world = playerInventoryIn.player.world;
		initSlots(playerInventoryIn);
		trackIntArray(dataIn);
	}

	protected abstract void initSlots(PlayerInventory var1);

	protected final void initPlayerSlotsAt(PlayerInventory playerInventory, int x, int y) {
		int xBase = x;
		int i = 0;

		byte var8;
		for(var8 = 2; i <= var8; ++i) {
			int j = 0;

			for(byte var10 = 8; j <= var10; ++j) {
				this.addSlot(new Slot(playerInventory, 9 * i + j + 9, x, y));
				x += 18;
			}

			x = xBase;
			y += 18;
		}

		y += 4;
		i = 0;

		for(var8 = 8; i <= var8; ++i) {
			this.addSlot(new Slot(playerInventory, i, x, y));
			x += 18;
		}

	}

	public void fillStackedContents(RecipeItemHelper helper) {
		if (this.machineInventory instanceof IRecipeHelperPopulator) {
			((IRecipeHelperPopulator) machineInventory).fillStackedContents(helper);
		}
	}

	public void clear() {
		this.machineInventory.clear();
	}

	public void func_217056_a(boolean placeAll, IRecipe<?> recipe, ServerPlayerEntity player) {
		//noinspection unchecked
		new ServerRecipePlacerMachine<>(this, this.inputSize, this.outputSize).place(player, (IRecipe<IInventory>) recipe, placeAll);
	}

	public boolean matches(IRecipe<? super IInventory> recipeIn) {
		return recipeIn.getType() == this.recipeType && recipeIn.matches(this.machineInventory, this.world);
	}

	public int getOutputSlot() {
		return this.inputSize;
	}

	public int getWidth() {
		return this.inputSize;
	}

	public int getHeight() {
		return 1;
	}

	@OnlyIn(Dist.CLIENT)
	public int getSize() {
		return this.inputSize + 1;
	}

	public boolean canInteractWith(PlayerEntity playerIn) {
		return this.machineInventory.isUsableByPlayer(playerIn);
	}

	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			stack = slotStack.copy();
			int s = inputSize + outputSize;
			int e = s + 36;
			if (index >= s) {
				e = s;
				s = 0;
			}

			ItemStack var8;
			if (!this.mergeItemStack(slotStack, s, e, false)) {
				var8 = ItemStack.EMPTY;
				return var8;
			}

			if (index >= inputSize && index < inputSize + outputSize) {
				slot.onSlotChange(slotStack, stack);
			}

			if (slotStack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			int var10000 = slotStack.getCount();
			if (var10000 == stack.getCount()) {
				var8 = ItemStack.EMPTY;
				return var8;
			}

			slot.onTake(playerIn, slotStack);
		}
		return stack;
	}

	public List<RecipeBookCategories> getRecipeBookCategories() {
		return Arrays.asList(RecipeBookCategories.SEARCH, RecipeBookCategories.EQUIPMENT, RecipeBookCategories.BUILDING_BLOCKS, RecipeBookCategories.MISC, RecipeBookCategories.REDSTONE);
	}

	@OnlyIn(Dist.CLIENT)
	public final int processScaled() {
		int i = this.data.get(1);
		int j = this.data.get(2);
		return j != 0 && i != 0 ? i * 24 / j : 0;
	}

	@OnlyIn(Dist.CLIENT)
	public final int energyScaled() {
		return this.data.get(0) * 54 / 1000000;
	}

	@OnlyIn(Dist.CLIENT)
	public final String energyText() {
		return this.data.get(0) + " / " + 1000000 + " F";
	}

}
