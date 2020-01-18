package szewek.flux.tile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import szewek.flux.F;
import szewek.flux.container.FluxGenContainer;
import szewek.flux.energy.EnergyCache;
import szewek.flux.recipe.FluxGenRecipes;

import javax.annotation.Nullable;

public class FluxGenTile extends LockableTileEntity implements IInventory, IItemHandler, IFluidHandler, ITickableTileEntity, INamedContainerProvider, IEnergyStorage {
	public static final int maxEnergy = 1000000, fluidCap = 4000;
	private final EnergyCache energyCache = new EnergyCache();
	private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
	private final FluidStack[] fluids = new FluidStack[] {FluidStack.EMPTY, FluidStack.EMPTY};
	private int tickCount = 0, energy = 0, workTicks = 0, maxWork = 0, energyGen = 0, workSpeed = 0;
	private boolean isReady = false, isDirty = false;
	public boolean receivedRedstone = false;
	protected final IIntArray fluxGenData = new IIntArray() {
		@Override
		public int get(int i) {
			switch (i) {
				case 0: return energy;
				case 1: return workTicks;
				case 2: return maxWork;
				case 3: return energyGen;
				case 4: return workSpeed;
				default: return 0;
			}
		}

		@Override
		public void set(int i, int v) {
			switch (i) {
				case 0: energy = v;
				case 1: workTicks = v;
				case 2: maxWork = v;
				case 3: energyGen = v;
				case 4: workSpeed = v;
			}
		}

		@Override
		public int size() {
			return 5;
		}
	};
	private final LazyOptional<FluxGenTile> selfHandler = LazyOptional.of(() -> this);

	public FluxGenTile() {
		super(F.Tiles.FLUXGEN);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		energy = compound.getInt("E");
		if (energy >= maxEnergy) energy = maxEnergy;
		workTicks = compound.getInt("WorkTicks");
		maxWork = compound.getInt("MaxWork");
		energyGen = compound.getInt("Gen");
		workSpeed = compound.getInt("WorkSpeed");
		ItemStackHelper.loadAllItems(compound, items);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("E", energy);
		compound.putInt("WorkTicks", workTicks);
		compound.putInt("MaxWork", maxWork);
		compound.putInt("Gen", energyGen);
		compound.putInt("WorkSpeed", workSpeed);
		ItemStackHelper.saveAllItems(compound, items);
		return compound;
	}

	@Override
	public void tick() {
		if (world == null || world.isRemote) return;
		if (!isReady) {
			if (world.getRedstonePowerFromNeighbors(pos) > 0)
				receivedRedstone = true;
			isReady = true;
		}
		if (!receivedRedstone) {
			if ((maxWork == 0 && ForgeHooks.getBurnTime(items.get(0)) > 0) || workTicks >= maxWork) {
				workTicks = 0;
				maxWork = updateWork();
			} else if (energy + energyGen <= maxEnergy) {
				energy += energyGen;
				workTicks += workSpeed;
				if (maxWork <= workTicks) {
					maxWork = 0;
					energyGen = 0;
				}
			}
		}
		tickCount++;
		if (tickCount > 3 && energy > 0) {
			tickCount = 0;
			for (Direction d : Direction.values()) {
				IEnergyStorage ie = energyCache.getCached(d, world, pos);
				if (ie != null && ie.canReceive()) {
					int r = 40000;
					if (r >= energy) r = energy;
					r = ie.receiveEnergy(r, true);
					if (r > 0) {
						energy -= r;
						ie.receiveEnergy(r, false);
					}
				}
			}
		}
		if (isDirty) markDirty();
	}

	private int updateWork() {
		ItemStack fuel = items.get(0);
		int f = ForgeHooks.getBurnTime(fuel);
		if (f == 0) return 0;
		ItemStack catalyst = items.get(1);
		FluxGenRecipes.Result genCat = FluxGenRecipes.getCatalyst(catalyst.getItem());
		FluxGenRecipes.Result genHot = FluxGenRecipes.getHotFluid(fluids[0]);
		FluxGenRecipes.Result genCold = FluxGenRecipes.getColdFluid(fluids[1]);
		energyGen = 40;
		if (genCat.usage <= catalyst.getCount()) {
			energyGen *= genCat.factor;
			catalyst.grow(-genCat.usage);
		}
		if (genHot.usage <= fluids[0].getAmount()) {
			f *= genHot.factor;
			fluids[0].grow(-genHot.usage);
		}
		if (genCold.usage <= fluids[1].getAmount()) {
			workSpeed = genCold.factor < genCat.factor ? genCold.factor - genCat.factor : 1;
			fluids[1].grow(-genCold.usage);
		} else {
			workSpeed = 1;
		}
		fuel.grow(-1);
		isDirty = true;
		return f;
	}

	@Nullable
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction dir) {
		if (!removed) {
			if (cap == CapabilityEnergy.ENERGY || cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
				return selfHandler.cast();
		}
		return super.getCapability(cap, dir);
	}

	@Override
	public void remove() {
		super.remove();
		selfHandler.invalidate();
	}

	@Override
	public int getEnergyStored() {
		return energy;
	}

	@Override
	public int getMaxEnergyStored() {
		return maxEnergy;
	}

	@Override
	public boolean canExtract() {
		return true;
	}

	@Override
	public boolean canReceive() {
		return false;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		return 0;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		int r = maxExtract;
		if (r > energy) r = energy;
		if (!simulate) {
			energy -= r;
		}
		return r;
	}

	@Override
	public int getSlots() {
		return 2;
	}

	@Override
	public int getSizeInventory() {
		return 2;
	}

	@Override
	public boolean isEmpty() {
		return items.get(0).isEmpty() && items.get(1).isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (i < 0 || i >= items.size())
			throw new RuntimeException("Getting slot " + i + " outside range [0," + items.size() + ")");
		return items.get(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int count) {
		return i >= 0 && i <= items.size() && count > 0 && !items.get(i).isEmpty() ? items.get(i).split(count) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStackFromSlot(int i) {
		if (i >= 0 && i <= items.size()) {
			ItemStack stack = items.get(i);
			items.set(i, ItemStack.EMPTY);
			return stack;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack stack) {
		if (i >= 0 && i <= items.size()) {
			if (stack.getCount() > 64) stack.setCount(64);
			items.set(i, stack);
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return player.world.getTileEntity(pos) == this && pos.distanceSq(player.getPositionVec(), true) <= 64.0;
	}

	@Override
	public void clear() {
		items.clear();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (slot < 0 || slot >= items.size())
			throw new RuntimeException("Getting slot " + slot + " outside range [0," + items.size() + ")");
		if (stack.isEmpty()) return ItemStack.EMPTY;
		if ((slot == 0 && ForgeHooks.getBurnTime(stack) == 0) || (slot == 1 && !FluxGenRecipes.isCatalyst(stack.getItem()))) {
			return stack;
		}
		int l = stack.getMaxStackSize();
		if (l > 64) l = 64;
		int sc = stack.getCount();
		ItemStack xis = items.get(slot);
		if (!xis.isEmpty()) {
			if (!ItemHandlerHelper.canItemStacksStack(stack, xis)) return stack;
			l -= xis.getCount();
		}
		if (0 >= l) return stack;
		boolean rl = sc > l;
		if (!simulate) {
			if (xis.isEmpty()) items.set(slot, rl ? ItemHandlerHelper.copyStackWithSize(stack, l) : stack);
			else xis.grow(rl ? l : sc);
			isDirty = true;
		}
		return rl ? ItemHandlerHelper.copyStackWithSize(stack, sc - l) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return false;
	}

	@Override
	public int getTanks() {
		return 2;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return fluids[tank];
	}

	@Override
	public int getTankCapacity(int tank) {
		return fluidCap;
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return false;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.getAmount() <= 0) return 0;
		int s = -1;
		if (FluxGenRecipes.isHotFluid(resource)) s = 0;
		else if (FluxGenRecipes.isColdFluid(resource)) s = 1;
		if (s == -1 || !fluids[s].isFluidEqual(resource)) return 0;
		int l = fluidCap - fluids[s].getAmount();
		if (l > resource.getAmount())
			l = resource.getAmount();
		if (l > 0 && action.execute()) {
			if (fluids[s].isEmpty())
				fluids[s] = resource.copy();
			else fluids[s].grow(l);
			isDirty = true;
		}
		return l;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return FluidStack.EMPTY;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return FluidStack.EMPTY;
	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("container.flux.fluxgen");
	}

	@Override
	protected Container createMenu(int id, PlayerInventory playerInv) {
		return new FluxGenContainer(id, playerInv, this, this.fluxGenData);
	}
}
