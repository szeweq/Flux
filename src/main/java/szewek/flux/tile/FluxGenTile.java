package szewek.flux.tile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import szewek.fl.util.FluidsUtil;
import szewek.fl.util.IntPair;
import szewek.flux.F;
import szewek.flux.FluxCfg;
import szewek.flux.container.FluxGenContainer;
import szewek.flux.data.FluxGenValues;
import szewek.flux.energy.EnergyCache;
import szewek.flux.tile.part.GeneratorEnergy;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FluxGenTile extends LockableTileEntity implements ITickableTileEntity {
	public static final int fluidCap = 4000;
	private final EnergyCache energyCache = new EnergyCache(this);
	private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
	private final AtomicBoolean isDirty = new AtomicBoolean();
	private final Tank tank = new Tank(isDirty);
	private final GeneratorEnergy energy = new GeneratorEnergy(1_000_000);
	private final WorkValues work = new WorkValues(isDirty);
	private int redstoneState = -1;
	protected final IIntArray tileData = new IIntArray() {
		@Override
		public int get(int i) {
			if (i < 2) return energy.getEnergy16Bit(i == 1);
			if (i < 6) return tank.getData(i - 2);
			return work.getData(i - 6);
		}

		@Override
		public void set(int i, int v) {
			if (i < 2) energy.setEnergy16Bit(i == 1, v);
			else if (i < 6) tank.setData(i - 2, v);
			else work.setData(i - 6, v);
		}

		@Override
		public int getCount() {
			return 10;
		}
	};

	public FluxGenTile() {
		super(F.T.FLUXGEN);
	}

	@Override
	public void load(BlockState blockState, CompoundNBT compound) {
		super.load(blockState, compound);
		energy.readNBT(compound);
		work.readNBT(compound);
		ItemStackHelper.loadAllItems(compound, items);
		List<FluidStack> fluidList = NonNullList.withSize(tank.fluids.length, FluidStack.EMPTY);
		FluidsUtil.loadAllFluids(compound, fluidList);
		for (int i = 0; i < tank.fluids.length; i++) {
			tank.fluids[i] = fluidList.get(i);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		super.save(compound);
		energy.writeNBT(compound);
		work.writeNBT(compound);
		ItemStackHelper.saveAllItems(compound, items);
		FluidsUtil.saveAllFluids(compound, Arrays.asList(tank.fluids), true);

		return compound;
	}

	public static void tick(World w, BlockPos bp, BlockState state, FluxGenTile it) {
		if (w.isClientSide) return;
		if (it.redstoneState == -1) {
			it.redstoneState = w.getBestNeighborSignal(bp) > 0 ? 1 : 0;
		}
		final GeneratorEnergy en = it.energy;
		if (it.redstoneState == 0) {
			final WorkValues wv = it.work;
			if (wv.canBegin(ForgeHooks.getBurnTime(it.items.get(0)))) {
				wv.update(it.items, it.tank.fluids);
			} else if (en.generate(it.work.gen)) {
				wv.tick();
				it.setChanged();
			} else if (wv.gen > en.getMaxEnergyStored()) {
				// BLOW UP
				w.explode(null, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5, 9.0F, Explosion.Mode.DESTROY);
			}
		}
		en.share(it.energyCache);
		if (it.isDirty.getAndSet(false)) it.setChanged();
	}

	@Override
	public void tick() {
		tick(level, worldPosition, /* unused */ null, this);
	}

	public void setRedstoneState(boolean state) {
		redstoneState = state ? 1 : 0;
	}

	public Tank getTank() {
		return tank;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction dir) {
		if (!remove) {
			if (cap == CapabilityEnergy.ENERGY) {
				return energy.lazyCast();
			} else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
				return tank.lazy.cast();
			}
		}
		return super.getCapability(cap, dir);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		energyCache.clear();
		energy.invalidate();
		tank.lazy.invalidate();
	}

	@Override
	public int getContainerSize() {
		return 2;
	}

	@Override
	public boolean isEmpty() {
		return items.get(0).isEmpty() && items.get(1).isEmpty();
	}

	@Override
	public ItemStack getItem(int i) {
		if (i < 0 || i >= items.size())
			throw new IndexOutOfBoundsException("Getting slot " + i + " outside range [0," + items.size() + ")");
		return items.get(i);
	}

	@Override
	public ItemStack removeItem(int i, int count) {
		return i >= 0 && i <= items.size() && count > 0 && !items.get(i).isEmpty() ? items.get(i).split(count) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		if (i >= 0 && i <= items.size()) {
			ItemStack stack = items.get(i);
			items.set(i, ItemStack.EMPTY);
			return stack;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int i, ItemStack stack) {
		if (i >= 0 && i <= items.size()) {
			if (stack.getCount() > 64) stack.setCount(64);
			items.set(i, stack);
		}
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return player.level.getBlockEntity(worldPosition) == this && worldPosition.distSqr(player.position(), true) <= 64.0;
	}

	@Override
	public void clearContent() {
		items.clear();
	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("container.flux.fluxgen");
	}

	@Override
	protected Container createMenu(int id, PlayerInventory playerInv) {
		return new FluxGenContainer(id, playerInv, this, tileData);
	}

	static class Tank implements IFluidHandler, NonNullSupplier<IFluidHandler> {
		private final FluidStack[] fluids = {FluidStack.EMPTY, FluidStack.EMPTY};
		private final LazyOptional<IFluidHandler> lazy = LazyOptional.of(this);
		private final AtomicBoolean isDirty;

		Tank(AtomicBoolean dirty) {
			isDirty = dirty;
		}

		private int getData(int i) {
			if (i >= 4) return 0;
			FluidStack fs = fluids[i >> 1];
			if (i % 2 == 0) {
				return ((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getID(fs.getFluid());
			}
			return fs.getAmount();
		}
		private void setData(int i, int v) {
			if (i >= 4) return;
			FluidStack fs = fluids[i >> 1];
			if (i % 2 == 0) {
				fluids[i >> 1] = new FluidStack(((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getValue(v), fs.getAmount());
			} else {
				if (!fs.isEmpty()) {
					fs.setAmount(v);
				}
			}
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
			if (resource.getAmount() <= 0) {
				return 0;
			}
			int s;
			if (FluxGenValues.HOT_FLUIDS.has(resource.getFluid())) {
				s = 0;
			} else if (FluxGenValues.COLD_FLUIDS.has(resource.getFluid())) {
				s = 1;
			} else {
				return 0;
			}
			FluidStack fs = fluids[s];
			if (!fs.isEmpty() && !fs.isFluidEqual(resource)) {
				return 0;
			}
			int l = fluidCap - fs.getAmount();
			if (l > resource.getAmount()) {
				l = resource.getAmount();
			}
			if (l > 0 && action.execute()) {
				if (fs.isEmpty()) {
					fluids[s] = resource.copy();
				} else {
					fs.grow(l);
				}
				isDirty.set(true);
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
		public IFluidHandler get() {
			return this;
		}
	}

	static class WorkValues {
		private final AtomicBoolean isDirty;
		private int ticks, max, gen, speed;

		WorkValues(AtomicBoolean isDirty) {
			this.isDirty = isDirty;
		}

		private int getData(int i) {
			switch (i) {
				case 0: return ticks;
				case 1: return max;
				case 2: return gen;
				case 3: return speed;
			}
			return 0;
		}

		private void setData(int i, int v) {
			switch (i) {
				case 0: ticks = v; break;
				case 1: max = v; break;
				case 2: gen = v; break;
				case 3: speed = v; break;
			}
		}

		private void readNBT(CompoundNBT nbt) {
			ticks = nbt.getInt("WorkTicks");
			max = nbt.getInt("MaxWork");
			gen = nbt.getInt("Gen");
			speed = nbt.getInt("WorkSpeed");
		}

		private void writeNBT(CompoundNBT nbt) {
			nbt.putInt("WorkTicks", ticks);
			nbt.putInt("MaxWork", max);
			nbt.putInt("Gen", gen);
			nbt.putInt("WorkSpeed", speed);
		}

		private boolean canBegin(int burnValue) {
			return (max == 0 && burnValue > 0) || ticks >= max;
		}

		private void tick() {
			ticks += speed;
			if (max <= ticks) {
				max = 0;
				gen = 0;
			}
		}

		private void update(NonNullList<ItemStack> items, FluidStack[] fluids) {
			ticks = 0;
			ItemStack fuel = items.get(0);
			int f = ForgeHooks.getBurnTime(fuel);
			if (f == 0) {
				max = 0;
				return;
			}
			ItemStack catalyst = items.get(1);
			gen = FluxCfg.ENERGY.fluxGenBaseEnergy.get();
			IntPair genCat = FluxGenValues.CATALYSTS.get(catalyst.getItem());
			if (genCat.r <= catalyst.getCount()) {
				gen *= genCat.l;
				if (genCat.r > 0) catalyst.grow(-genCat.r);
			}
			IntPair genHot = FluxGenValues.HOT_FLUIDS.get(fluids[0].getFluid());
			if (genHot.r <= fluids[0].getAmount()) {
				f *= genHot.l;
				if (genHot.r > 0) fluids[0].grow(-genHot.r);
			}
			IntPair genCold = FluxGenValues.COLD_FLUIDS.get(fluids[1].getFluid());
			if (genCold.r <= fluids[1].getAmount()) {
				speed = genCold.l < genCat.l ? genCat.l - genCold.l : 1;
				gen *= speed;
				if (genCold.r > 0) fluids[1].grow(-genCold.r);
			} else {
				speed = 1;
			}
			fuel.grow(-1);
			max = f;
			isDirty.set(true);
		}
	}
}
