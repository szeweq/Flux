package szewek.flux.tile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import szewek.flux.F;
import szewek.flux.FluxCfg;
import szewek.flux.container.FluxGenContainer;
import szewek.flux.energy.EnergyCache;
import szewek.flux.recipe.FluxGenRecipes;
import szewek.flux.util.IntPair;

import javax.annotation.Nullable;

public class FluxGenTile extends LockableTileEntity implements IInventory, IItemHandler, IFluidHandler, ITickableTileEntity, INamedContainerProvider, IEnergyStorage {
	public static final int fluidCap = 4000;
	private final EnergyCache energyCache = new EnergyCache(this);
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
				case 5: return ((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getID(fluids[0].getFluid());
				case 6: return fluids[0].getAmount();
				case 7: return ((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getID(fluids[1].getFluid());
				case 8: return fluids[1].getAmount();
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
				case 5: fluids[0] = new FluidStack(((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getValue(v), fluids[0].getAmount());
				case 6: if (!fluids[0].isEmpty()) fluids[0].setAmount(v);
				case 7: fluids[1] = new FluidStack(((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getValue(v), fluids[0].getAmount());
				case 8: if (!fluids[1].isEmpty()) fluids[0].setAmount(v);
			}
		}

		@Override
		public int size() {
			return 9;
		}
	};
	private final LazyOptional<FluxGenTile> selfHandler = LazyOptional.of(() -> this);

	public FluxGenTile() {
		super(F.T.FLUXGEN);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		energy = MathHelper.clamp(compound.getInt("E"), 0, 1000000);
		workTicks = compound.getInt("WorkTicks");
		maxWork = compound.getInt("MaxWork");
		energyGen = compound.getInt("Gen");
		workSpeed = compound.getInt("WorkSpeed");
		ItemStackHelper.loadAllItems(compound, items);

		ListNBT fluidNBT = compound.getList("Fluids", 10);
		for (int i = 0; i < fluidNBT.size(); i++) {
			CompoundNBT nbt = fluidNBT.getCompound(i);
			int j = nbt.getByte("Slot") & 255;
			if (j >= 0 && j < fluids.length) {
				fluids[j] = FluidStack.loadFluidStackFromNBT(nbt);
			}
		}
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

		ListNBT fluidNBT = new ListNBT();
		for (int i = 0; i < fluids.length; i++) {
			FluidStack fs = fluids[i];
			if (!fs.isEmpty()) {
				CompoundNBT nbt = new CompoundNBT();
				nbt.putByte("Slot", (byte) i);
				fs.writeToNBT(nbt);
				fluidNBT.add(nbt);
			}
		}
		compound.put("Fluids", fluidNBT);

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
			} else if (energy + energyGen <= 1000000) {
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
				IEnergyStorage ie = energyCache.getCached(d);
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
		IntPair genCat = FluxGenRecipes.getCatalyst(catalyst.getItem());
		IntPair genHot = FluxGenRecipes.getHotFluid(fluids[0]);
		IntPair genCold = FluxGenRecipes.getColdFluid(fluids[1]);
		energyGen = FluxCfg.COMMON.fluxGenBaseEnergyValue.get();
		if (genCat.r <= catalyst.getCount()) {
			energyGen *= genCat.l;
			if (genCat.r > 0) catalyst.grow(-genCat.r);
		}
		if (genHot.r <= fluids[0].getAmount()) {
			f *= genHot.l;
			if (genHot.r > 0) fluids[0].grow(-genHot.r);
		}
		if (genCold.r <= fluids[1].getAmount()) {
			workSpeed = genCold.l < genCat.l ? genCold.l - genCat.l : 1;
			if (genCold.r > 0) fluids[1].grow(-genCold.r);
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
		return 1000000;
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
		if (maxExtract <= 0) return 0;
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
		int s;
		if (FluxGenRecipes.isHotFluid(resource)) s = 0;
		else if (FluxGenRecipes.isColdFluid(resource)) s = 1;
		else return 0;
		FluidStack fs = fluids[s];
		if (!fs.isEmpty() && !fs.isFluidEqual(resource)) return 0;
		int l = fluidCap - fs.getAmount();
		if (l > resource.getAmount())
			l = resource.getAmount();
		if (l > 0 && action.execute()) {
			if (fs.isEmpty())
				fluids[s] = resource.copy();
			else fs.grow(l);
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
		return new FluxGenContainer(id, playerInv, this, fluxGenData);
	}
}
