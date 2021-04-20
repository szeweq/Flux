package szewek.flux.tile;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IRecipeHelperPopulator;
import net.minecraft.inventory.IRecipeHolder;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import szewek.fl.recipe.RecipeCompat;
import szewek.flux.FluxCfg;
import szewek.flux.block.MachineBlock;
import szewek.flux.item.ChipItem;
import szewek.flux.recipe.AbstractMachineRecipe;
import szewek.flux.util.inventory.IInventoryIO;
import szewek.flux.util.inventory.IOSize;
import szewek.flux.util.inventory.MachineInventory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractMachineTile extends PoweredDeviceTile implements ISidedInventory, IInventoryIO, IRecipeHolder, IRecipeHelperPopulator, ITickableTileEntity {
	private final IOSize ioSize;
	protected final Process process = new Process();
	protected int compatState;
	protected boolean lazyCheck, wasLit;
	protected final MachineInventory inv;
	protected final IRecipeType<?> recipeType;
	private final Object2IntMap<ResourceLocation> recipesCount = new Object2IntOpenHashMap<>();
	protected IRecipe<?> cachedRecipe;
	protected final IIntArray machineData = new IIntArray() {
		@Override
		public int get(int index) {
			if (index < 2) {
				return energy.getEnergy16Bit(index == 1);
			}
			switch (index) {
				case 2: return process.current;
				case 3: return process.total;
				case 4: return energyUse;
				case 5: return process.speed;
				case 6: return compatState;
				default: return 0;
			}
		}

		@Override
		public void set(int index, int value) {
			if (index < 2) {
				energy.setEnergy16Bit(index == 1, value);
				return;
			}
			switch (index) {
				case 2: process.current = value; break;
				case 3: process.total = value; break;
				case 4: energyUse = value; break;
				case 5: process.speed = value; break;
				case 6: compatState = value; break;
				default:
			}
		}

		@Override
		public int getCount() {
			return 7;
		}
	};

	protected AbstractMachineTile(TileEntityType<?> typeIn, final IRecipeType<?> recipeTypeIn, IOSize ioSize) {
		super(typeIn);
		recipeType = recipeTypeIn;
		this.ioSize = ioSize;
		inv = new MachineInventory(ioSize, 1);
		energyUse = FluxCfg.ENERGY.basicMachine.get();
		FluxCfg.addListener(this::updateValues);
	}

	@Override
	public void load(BlockState blockState, CompoundNBT compound) {
		super.load(blockState, compound);
		inv.readNBT(compound);
		energy.readNBT(compound);
		process.current = compound.getInt("Process");
		process.total = compound.getInt("Total");
		int i = compound.getShort("RSize");
		for (int j = 0; j < i; j++) {
			ResourceLocation location = new ResourceLocation(compound.getString("RLoc" + j));
			int c = compound.getInt("RCount" + j);
			recipesCount.put(location, c);
		}
		updateValues();
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		super.save(compound);
		energy.writeNBT(compound);
		compound.putInt("Process", process.current);
		compound.putInt("Total", process.total);
		inv.writeNBT(compound, true);
		compound.putShort("RSize", (short) recipesCount.size());
		int i = 0;
		for (Map.Entry<ResourceLocation, Integer> entry : recipesCount.object2IntEntrySet()) {
			compound.putString("RLoc" + i, entry.getKey().toString());
			compound.putInt("RCount" + i, entry.getValue());
			++i;
		}
		return compound;
	}

	@Override
	protected void serverTick(World w) {
		boolean workState = process.current > 0;
		if (inv.inputHasStacks() && canProcess() && energy.use(energyUse)) {
			if (process.update()) {
				produceResult();
				isDirty = true;
			}
		} else {
			process.current = 0;
		}
		boolean currState = process.current > 0;
		if (lazyCheck) {
			if (workState == currState && wasLit != currState) {
				w.setBlock(worldPosition, w.getBlockState(worldPosition).setValue(MachineBlock.LIT, currState), 3);
			}
			lazyCheck = false;
		} else if (workState != currState) {
			lazyCheck = true;
			wasLit = getBlockState().getValue(MachineBlock.LIT);
		}
	}

	protected boolean canProcess() {
		if (cachedRecipe == null) {
			//noinspection ConstantConditions
			setCachedRecipe(RecipeCompat.getCompatRecipe(recipeType, level, this).orElse(null));
		}
		if (cachedRecipe != null) {
			ItemStack result = RecipeCompat.getCompatOutput(cachedRecipe, this);
			if (result.isEmpty()) {
				return false;
			}
			return inv.checkResult(result);
		}
		return false;
	}

	protected void produceResult() {
		if (canProcess()) {
			ItemStack result = RecipeCompat.getCompatOutput(cachedRecipe, this);
			if (!result.isEmpty()) {
				inv.placeResult(result);
				RecipeCompat.getRecipeItemsConsumer(cachedRecipe).accept(inv.iterableInput());
				setRecipeUsed(cachedRecipe);
				setCachedRecipe(null);
			}
		}
	}

	protected void setCachedRecipe(@Nullable final IRecipe<?> recipe) {
		cachedRecipe = recipe;
		compatState = recipe != null && recipe.getType() != recipeType ? 1 : 0;
		process.total = recipe instanceof AbstractMachineRecipe ? ((AbstractMachineRecipe) cachedRecipe).processTime * 100 : 20000;
	}

	@Override
	public IOSize getIOSize() {
		return ioSize;
	}

	@Override
	public int getContainerSize() {
		return inv.size();
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
		return canPlaceItem(index, itemStackIn);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return index >= ioSize.in && index < ioSize.in + ioSize.out;
	}

	@Override
	public boolean isEmpty() {
		return inv.isEmpty();
	}

	@Override
	public ItemStack getItem(int index) {
		return inv.get(index);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		return inv.getAndSplit(index, count);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return inv.getAndRemove(index);
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		ItemStack inputStack = inv.get(index);
		boolean same = !stack.isEmpty() && stack.sameItem(inputStack) && ItemStack.tagMatches(stack, inputStack);
		inv.set(index, stack);
		if (stack.getCount() > 64) {
			stack.setCount(64);
		}
		if (index < ioSize.in && !same) {
			assert level != null;
			setCachedRecipe(RecipeCompat.getCompatRecipe(recipeType, level, this).orElse(null));
			//processTotal = getProcessTime() * 100;
			process.current = 0;
			lazyCheck = true;
			setChanged();
		}
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return index < ioSize.in;
	}

	@Override
	public void clearContent() {
		inv.clear();
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return level.getBlockEntity(worldPosition) == this && worldPosition.distSqr(player.position(), true) <= 64.0;
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
	public void fillStackedContents(RecipeItemHelper helper) {
		for (ItemStack item : inv) {
			helper.accountStack(item);
		}
	}

	public void updateRecipes(PlayerEntity player) {
		final RecipeManager recipeManager = player.level.getRecipeManager();
		List<IRecipe<?>> recipes = recipesCount.object2IntEntrySet().stream()
				.map(e -> {
					IRecipe<?> recipe = recipeManager.byKey(e.getKey()).orElse(null);
					if (recipe instanceof AbstractMachineRecipe) {
						collectExperience(player, e.getIntValue(), ((AbstractMachineRecipe) recipe).experience);
					}
					return recipe;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		player.awardRecipes(recipes);
		recipesCount.clear();
	}

	public void updateValues() {
		energyUse = FluxCfg.ENERGY.basicMachine.get();
		process.speed = 100;
		ItemStack chipStack = inv.get(ioSize.in + ioSize.out);
		if (!chipStack.isEmpty() && chipStack.getItem() instanceof ChipItem) {
			CompoundNBT tag = chipStack.getTag();
			if (tag != null) {
				process.speed = ChipItem.countValue(tag, "speed", process.speed);
				energyUse = ChipItem.countValue(tag, "energy", energyUse) * process.speed / 100;
			}

		}
	}

	private static void collectExperience(PlayerEntity player, int count, float xp) {
		if (xp == 0) {
			return;
		}
		if (xp < 1) {
			float f = (float) count * xp;
			int i = MathHelper.floor(f);
			if (i < MathHelper.ceil(f) && Math.random() < (double)(f - (float) i)) {
				++i;
			}
			count = i;
		}
		while (count > 0) {
			int x = ExperienceOrbEntity.getExperienceValue(count);
			count -= x;
			player.level.addFreshEntity(new ExperienceOrbEntity(
					player.level,
					player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D,
					x
			));
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		FluxCfg.removeListener(this::updateValues);
	}

	private static class Process {
		int current = -1, total, speed = 100;

		private boolean update() {
			boolean b = current >= total;
			current = b ? 0 : current + speed;
			return b;
		}
	}
}