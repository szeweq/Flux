package szewek.flux.container;

import com.google.common.collect.Lists;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IRecipeHelperPopulator;
import net.minecraft.inventory.Inventory;
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
import szewek.flux.tile.AbstractMachineTile;
import szewek.flux.util.ServerRecipePlacerMachine;

import java.util.List;

public abstract class AbstractMachineContainer extends RecipeBookContainer<IInventory> {
	protected final IInventory machineInventory;
	private final int inputSize, outputSize;
	private final IIntArray data;
	public final IRecipeType<? extends AbstractMachineRecipe> recipeType;
	protected final World world;

	protected AbstractMachineContainer(ContainerType<?> containerTypeIn, IRecipeType<? extends AbstractMachineRecipe> recipeTypeIn, int id, PlayerInventory playerInventoryIn, int inSize, int outSize) {
		this(containerTypeIn, recipeTypeIn, id, playerInventoryIn, inSize, outSize, new Inventory(inSize + outSize), new IntArray(4));
	}

	protected AbstractMachineContainer(ContainerType<?> containerTypeIn, IRecipeType<? extends AbstractMachineRecipe> recipeTypeIn, int id, PlayerInventory playerInventoryIn, int inSize, int outSize, IInventory machineInventoryIn, IIntArray dataIn) {
		super(containerTypeIn, id);
		recipeType = recipeTypeIn;
		inputSize = inSize;
		outputSize = outSize;
		assertInventorySize(machineInventoryIn, inSize + outSize);
		assertIntArraySize(dataIn, 4);
		machineInventory = machineInventoryIn;
		data = dataIn;
		world = playerInventoryIn.player.world;

		initSlots(playerInventoryIn);
		trackIntArray(dataIn);
	}

	protected abstract void initSlots(PlayerInventory playerInventory);

	protected final void initPlayerSlotsAt(PlayerInventory playerInventory, int x, int y) {
		final int xBase = x;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlot(new Slot(playerInventory, 9 * i + j + 9, x, y));
				x += 18;
			}
			x = xBase;
			y += 18;
		}
		y += 4;
		for (int i = 0; i < 9; i++) {
			addSlot(new Slot(playerInventory, i, x, y));
			x += 18;
		}
	}

	@Override
	public void func_201771_a(RecipeItemHelper helper) {
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
			int s = inputSize + outputSize;
			int e = s + 36;
			if (index >= s) {
				e = s;
				s = 0;
			}
			if (!mergeItemStack(slotStack, s, e, false)) {
				return ItemStack.EMPTY;
			}
			if (index >= inputSize && index < inputSize + outputSize) {
				slot.onSlotChange(slotStack, stack);
			}
			if (slotStack.isEmpty()) slot.putStack(ItemStack.EMPTY);
			else slot.onSlotChanged();
			if (slotStack.getCount() == stack.getCount()) return ItemStack.EMPTY;
			slot.onTake(playerIn, slotStack);
		}
		return stack;
	}

	@Override
	public List<RecipeBookCategories> getRecipeBookCategories() {
		return Lists.newArrayList(RecipeBookCategories.SEARCH, RecipeBookCategories.EQUIPMENT, RecipeBookCategories.BUILDING_BLOCKS, RecipeBookCategories.MISC, RecipeBookCategories.REDSTONE);
	}

	@OnlyIn(Dist.CLIENT)
	public int processScaled() {
		int i = data.get(1);
		int j = data.get(2);
		return j != 0 && i != 0 ? i * 24 / j : 0;
	}

	@OnlyIn(Dist.CLIENT)
	public int energyScaled() {
		return data.get(0) * 54 / AbstractMachineTile.maxEnergy;
	}

	@OnlyIn(Dist.CLIENT)
	public String energyText() {
		return data.get(0) + " / " + AbstractMachineTile.maxEnergy + " F";
	}
}
