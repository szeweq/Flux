package szewek.flux.tile;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IRecipeHelperPopulator;
import net.minecraft.inventory.IRecipeHolder;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import szewek.fl.energy.IEnergyReceiver;
import szewek.fl.recipe.RecipeCompat;
import szewek.flux.FluxCfg;
import szewek.flux.block.MachineBlock;
import szewek.flux.item.ChipItem;
import szewek.flux.recipe.AbstractMachineRecipe;
import szewek.flux.util.IInventoryIO;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractMachineTile extends LockableTileEntity implements IEnergyReceiver, ISidedInventory, IInventoryIO, IRecipeHolder, IRecipeHelperPopulator, ITickableTileEntity, FluxCfg.IConfigChangeListener {
	private final int inputSize, outputSize;
	protected int energy, process, processTotal, energyUse, processSpeed = 100;
	protected boolean isDirty;
	protected final NonNullList<ItemStack> items;
	protected final IRecipeType<? extends AbstractMachineRecipe> recipeType;
	private final Object2IntMap<ResourceLocation> recipesCount = new Object2IntOpenHashMap<>();
	private final MenuFactory menuFactory;
	private IRecipe<?> cachedRecipe;
	protected final IIntArray machineData = new IIntArray() {
		@Override
		public int get(int index) {
			switch (index) {
				case 0: return energy >> 16;
				case 1: return energy & 0xFFFF;
				case 2: return process;
				case 3: return processTotal;
				case 4: return energyUse;
				case 5: return processSpeed;
				default: return 0;
			}
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0: energy = (energy & 0xFFFF) + (value << 16); break;
				case 1: energy = (energy & 0xFFFF0000) + value; break;
				case 2: process = value; break;
				case 3: processTotal = value; break;
				case 4: energyUse = value; break;
				case 5: processSpeed = value; break;
				default:
			}
		}

		@Override
		public int size() {
			return 6;
		}
	};

	protected AbstractMachineTile(TileEntityType<?> typeIn, final IRecipeType<? extends AbstractMachineRecipe> recipeTypeIn, MenuFactory factory, int inSize, int outSize) {
		super(typeIn);
		recipeType = recipeTypeIn;
		menuFactory = factory;
		items = NonNullList.withSize(inSize + outSize + 1, ItemStack.EMPTY);
		inputSize = inSize;
		outputSize = outSize;
		energyUse = FluxCfg.COMMON.basicMachineEU.get();
		FluxCfg.addListener(this);
	}

	@Override
	public void onConfigChanged() {
		updateValues();
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		items.clear();
		ItemStackHelper.loadAllItems(compound, items);
		energy = MathHelper.clamp(compound.getInt("E"), 0, 1000000);
		process = compound.getInt("Process");
		processTotal = compound.getInt("Total");
		int i = compound.getShort("RSize");
		for (int j = 0; j < i; j++) {
			ResourceLocation location = new ResourceLocation(compound.getString("RLoc" + j));
			int c = compound.getInt("RCount" + j);
			recipesCount.put(location, c);
		}
		updateValues();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("E", energy);
		compound.putInt("Process", process);
		compound.putInt("Total", processTotal);
		ItemStackHelper.saveAllItems(compound, items);
		compound.putShort("RSize", (short) recipesCount.size());
		int i = 0;
		for (Map.Entry<ResourceLocation, Integer> entry : recipesCount.object2IntEntrySet()) {
			compound.putString("RLoc" + i, entry.getKey().toString());
			compound.putInt("RCount" + i, entry.getValue());
			++i;
		}
		return compound;
	}

	private boolean isPowered() {
		return energy >= energyUse;
	}

	@Override
	public void tick() {
		boolean workState = isWorking();
		assert world != null;
		if (!world.isRemote) {
			boolean inputEmpty = true;
			for (ItemStack inputStack : getInputs()) {
				if (!inputStack.isEmpty()) {
					inputEmpty = false;
					break;
				}
			}
			if (isPowered() && !inputEmpty) {
				if (cachedRecipe == null) {
					cachedRecipe = RecipeCompat.getCompatRecipe(recipeType, world, this).orElse(null);
				}
				if (canProcess()) {
					energy -= energyUse;
					if (process >= processTotal) {
						process = 0;
						processTotal = getProcessTime() * 100;
						produceResult();
						isDirty = true;
					} else {
						process += processSpeed;
					}
				} else {
					process = 0;
				}
			}
		}
		if (workState != isWorking()) {
			world.setBlockState(pos, world.getBlockState(pos).with(MachineBlock.LIT, isWorking()), 3);
		}
		if (isDirty) {
			markDirty();
			isDirty = false;
		}
	}

	protected boolean isWorking() {
		return process > 0;
	}

	protected boolean canProcess() {
		if (cachedRecipe != null) {
			ItemStack result = cachedRecipe.getRecipeOutput();
			for (ItemStack outputStack : getOutputs()) {
				if (outputStack.isEmpty()) return true;
				if (!outputStack.isItemEqual(result)) return false;
				int minStackSize = Math.min(Math.min(64, outputStack.getMaxStackSize()), result.getMaxStackSize());
				if (outputStack.getCount() + result.getCount() <= minStackSize) return true;
			}
		}
		return false;
	}

	protected int getProcessTime() {
		return cachedRecipe instanceof AbstractMachineRecipe ? ((AbstractMachineRecipe) cachedRecipe).processTime : 200;
	}

	protected void produceResult() {
		if (canProcess()) {
			ItemStack result = cachedRecipe.getRecipeOutput();
			List<ItemStack> outputs = getOutputs();
			for (int i = 0; i < outputs.size(); i++) {
				ItemStack outputStack = outputs.get(i);
				if (outputStack.isEmpty()) {
					ItemStack copyResult = result.copy();
					outputs.set(i, copyResult);
					break;
				} else if (outputStack.getItem() == result.getItem()) {
					outputStack.grow(result.getCount());
					break;
				}
			}
			List<ItemStack> inputs = getInputs();
			RecipeCompat.getRecipeItemsConsumer(cachedRecipe).accept(inputs);
			setRecipeUsed(cachedRecipe);
			cachedRecipe = null;
		}
	}

	private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> this);
	private final LazyOptional<? extends IItemHandler>[] sideHandlers = SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!removed) {
			if (cap == CapabilityEnergy.ENERGY)
				return energyHandler.cast();
			if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				if (side == Direction.UP) {
					return sideHandlers[0].cast();
				} else if (side == Direction.DOWN) {
					return sideHandlers[1].cast();
				} else {
					return sideHandlers[2].cast();
				}
		}
		return super.getCapability(cap, side);
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if (maxReceive <= 0) return 0;
		int r = maxReceive;
		if (r > 1000000 - energy) r = 1000000 - energy;
		if (!simulate) {
			energy += r;
			isDirty = true;
		}
		return r;
	}

	@Override
	public int getEnergyStored() {
		return energy;
	}

	@Override
	public int getMaxEnergyStored() {
		return 1000000;
	}

	@Override
	public List<ItemStack> getInputs() {
		return items.subList(0, inputSize);
	}

	@Override
	public List<ItemStack> getOutputs() {
		int size = items.size() - 1;
		return items.subList(size - outputSize, size);
	}

	@Override
	public int getSizeInventory() {
		return items.size();
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction) {
		return isItemValidForSlot(index, itemStackIn);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
		return index >= inputSize && index < inputSize + outputSize;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack item : items) {
			if (!item.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return items.get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(items, index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(items, index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		ItemStack inputStack = items.get(index);
		boolean same = !stack.isEmpty() && stack.isItemEqual(inputStack) && ItemStack.areItemStackTagsEqual(stack, inputStack);
		items.set(index, stack);
		if (stack.getCount() > 64) stack.setCount(64);
		if (index < inputSize && !same) {
			assert world != null;
			cachedRecipe = RecipeCompat.getCompatRecipe(recipeType, world, this).orElse(null);
			processTotal = getProcessTime() * 100;
			process = 0;
			markDirty();
		}
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return player.world.getTileEntity(pos) == this && pos.distanceSq(player.getPositionVec(), true) <= 64.0;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return index < inputSize;
	}

	@Override
	public void clear() {
		items.clear();
	}

	@Override
	public void setRecipeUsed(@Nullable IRecipe<?> recipe) {
		if (recipe != null) {
			recipesCount.compute(recipe.getId(), (key, integer) -> 1 + (integer == null ? 0 : integer));
		}
	}

	@Nullable
	@Override
	public final IRecipe<?> getRecipeUsed() {
		return null;
	}

	@Override
	public final void onCrafting(PlayerEntity player) {

	}

	@Override
	public void fillStackedContents(RecipeItemHelper helper) {
		for (ItemStack item : items) {
			helper.accountStack(item);
		}
	}

	public void updateRecipes(PlayerEntity player) {
		final RecipeManager recipeManager = player.world.getRecipeManager();
		List<IRecipe<?>> recipes = recipesCount.object2IntEntrySet().stream()
				.map(e -> {
					IRecipe<?> recipe = recipeManager.getRecipe(e.getKey()).orElse(null);
					if (recipe instanceof AbstractMachineRecipe) {
						collectExperience(player, e.getIntValue(), ((AbstractMachineRecipe) recipe).experience);
					}
					return recipe;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		player.unlockRecipes(recipes);
		recipesCount.clear();
	}

	public void updateValues() {
		energyUse = FluxCfg.COMMON.basicMachineEU.get();
		processSpeed = 100;
		ItemStack chipStack = items.get(inputSize + outputSize);
		Item item = chipStack.getItem();
		if (!chipStack.isEmpty() && item instanceof ChipItem) {
			ChipItem ci = (ChipItem) item;
			CompoundNBT tag = chipStack.getTag();
			if (tag != null) {
				processSpeed = ci.countValue(tag, "speed", processSpeed);
				energyUse = ci.countValue(tag, "energy", energyUse) * processSpeed / 100;
			}

		}
	}

	private static void collectExperience(PlayerEntity player, int count, float xp) {
		if (xp == 0) {
			count = 0;
		} else if (xp < 1) {
			float f = (float) count * xp;
			int i = MathHelper.floor(f);
			if (i < MathHelper.ceil(f) && Math.random() < (double)(f - (float) i)) {
				++i;
			}
			count = i;
		}
		while (count > 0) {
			int x = ExperienceOrbEntity.getXPSplit(count);
			count -= x;
			player.world.addEntity(new ExperienceOrbEntity(
					player.world,
					player.getPosX(), player.getPosY() + 0.5D, player.getPosZ() + 0.5D,
					x
			));
		}
	}

	@Override
	protected Container createMenu(int id, PlayerInventory player) {
		return menuFactory.create(id, player, this, this.machineData);
	}

	@Override
	public void remove() {
		super.remove();
		energyHandler.invalidate();
		FluxCfg.removeListener(this);
	}
}