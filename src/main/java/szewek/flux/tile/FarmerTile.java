package szewek.flux.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import szewek.flux.F;

import java.util.ArrayList;
import java.util.List;

public class FarmerTile extends PoweredTile {
	private int offsetX, offsetZ;

	public FarmerTile() {
		super(F.Tiles.FARMER);
		offsetX = offsetZ = -5;
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		offsetX = compound.getInt("OffX");
		offsetZ = compound.getInt("OffZ");
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("OffX", offsetX);
		compound.putInt("OffZ", offsetZ);
		return compound;
	}

	@Override
	public void tick() {
		assert world != null;
		if (!world.isRemote()) {
			if (offsetX == 5 && offsetZ == 5) {
				offsetX = -5;
				offsetZ = -5;
			} else if (offsetX == 5) {
				offsetX = -5;
				offsetZ++;
			} else offsetX++;

			BlockPos bp = pos.add(offsetX, 0, offsetZ);
			BlockState bs = world.getBlockState(bp);
			Block b = bs.getBlock();
			if (b != F.Blocks.FARMER) {
				if (b instanceof CropsBlock) {
					CropsBlock crop = (CropsBlock) b;
					if (crop.isMaxAge(bs)) {
						List<ItemStack> drops = bs.getDrops(new LootContext.Builder((ServerWorld)world).withParameter(LootParameters.POSITION, pos).withParameter(LootParameters.TOOL, ItemStack.EMPTY));
						List<IItemHandler> inv = new ArrayList<>();
						if (!drops.isEmpty()) {
							world.setBlockState(bp, crop.withAge(0));

							for (Direction dir : Direction.values()) {
								if (dir != Direction.DOWN) {
									TileEntity te = world.getTileEntity(pos.offset(dir));
									if (te != null) {
										IItemHandler iih;
										LazyOptional<IItemHandler> il = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
										if (il.isPresent()) {
											iih = il.orElse(null);
										} else {
											if (!(te instanceof IInventory)) continue;
											iih = new InvWrapper((IInventory)te);
										}
										inv.add(iih);
									}
								}
							}

							itemloop: for (ItemStack stack : drops) {
								for (IItemHandler iih : inv) {
									int l = iih.getSlots();
									for(int i = 0; i < l; ++i) {
										if (iih.isItemValid(i, stack)) {
											stack = iih.insertItem(i, stack, false);
										}
									}
									if (stack.isEmpty()) continue itemloop;
								}
								InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY()+1, pos.getZ(), stack);
							}
						}
					}
				}
			}
			energy -= 100;
		}
	}
}
