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
import szewek.flux.F;
import szewek.flux.FluxCfg;
import szewek.flux.block.MachineBlock;
import szewek.flux.item.ChipItem;
import szewek.flux.recipe.AbstractMachineRecipe;
import szewek.flux.energy.IEnergyReceiver;
import szewek.flux.util.IInventoryIO;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractMachineTile extends LockableTileEntity implements IEnergyReceiver, ISidedInventory, IInventoryIO, IRecipeHolder, IRecipeHelperPopulator, ITickableTileEntity, FluxCfg.IConfigChangeListener {
	private final int inputSize, outputSize;
	protected int energy = 0, process = 0, processTotal = 0, energyUse, processSpeed = 100;
	protected boolean isDirty = false;
	protected NonNullList<ItemStack> items;
	protected final IRecipeType<? extends AbstractMachineRecipe> recipeType;
	private final Object2IntMap<ResourceLocation> recipesCount = new Object2IntOpenHashMap<>();
	private final MenuFactory menuFactory;
	protected final IIntArray machineData = new IIntArray() {
		@Override
		public int get(int index) {
			switch (index) {
				case 0: return energy;
				case 1: return process;
				case 2: return processTotal;
				case 3: return energyUse;
				case 4: return processSpeed;
				default: return 0;
			}
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0: energy = value; break;
				case 1: process = value; break;
				case 2: processTotal = value; break;
				case 3: energyUse = value; break;
				case 4: processSpeed = value; break;
			}
		}

		@Override
		public int size() {
			return 5;
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
		if (world != null && !world.isRemote) {
			boolean inputEmpty = true;
			for (ItemStack inputStack : getInputs()) {
				if (!inputStack.isEmpty()) {
					inputEmpty = false;
					break;
				}
			}
			if (isPowered() && !inputEmpty) {
				AbstractMachineRecipe recipe = world.getRecipeManager().getRecipe(recipeType, this, world).orElse(null);
				if (recipe != null && canProcess(recipe)) {
					energy -= energyUse;
					if (process >= processTotal) {
						process = 0;
						processTotal = recipe.processTime * 100;
						produceResult(recipe);
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

	protected boolean canProcess(@Nullable IRecipe<?> recipe) {
		if (recipe != null) {
			ItemStack result = recipe.getRecipeOutput();
			for (ItemStack outputStack : getOutputs()) {
				if (outputStack.isEmpty()) return true;
				if (!outputStack.isItemEqual(result)) return false;
				int minStackSize = Math.min(Math.min(64, outputStack.getMaxStackSize()), result.getMaxStackSize());
				if (outputStack.getCount() + result.getCount() <= minStackSize) return true;
			}
		}
		return false;
	}

	protected void produceResult(@Nullable AbstractMachineRecipe recipe) {
		if (recipe != null && canProcess(recipe)) {
			ItemStack result = recipe.getRecipeOutput();
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
			recipe.consumeItems(inputs);
			setRecipeUsed(recipe);
		}
	}

	private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> this);

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!removed && cap == CapabilityEnergy.ENERGY) return energyHandler.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
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
	public boolean canReceive() {
		return true;
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
		return true;
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
			processTotal = world.getRecipeManager().getRecipe(recipeType, this, world).map(r -> r.processTime).orElse(200) * 100;
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
	public IRecipe<?> getRecipeUsed() {
		return null;
	}

	@Override
	public void onCrafting(PlayerEntity player) {

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
					if (recipe != null) {
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
		if (!chipStack.isEmpty() && chipStack.getItem() instanceof ChipItem) {
			ChipItem ci = (ChipItem) chipStack.getItem();
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