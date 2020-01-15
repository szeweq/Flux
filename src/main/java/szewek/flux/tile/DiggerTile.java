package szewek.flux.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import szewek.flux.F;
import szewek.flux.block.ActiveTileBlock;

import java.util.ArrayList;
import java.util.List;

public final class DiggerTile extends PoweredTile {
	private int offsetX, offsetY, offsetZ;
	private boolean finished, lastFlag;

	public DiggerTile() {
		super(F.Tiles.DIGGER);
	}

	public void read(CompoundNBT compound) {
		super.read(compound);
		offsetX = compound.getInt("OffX");
		offsetY = compound.getInt("OffY");
		offsetZ = compound.getInt("OffZ");
		finished = compound.getBoolean("Finished");
	}

	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("OffX", offsetX);
		compound.putInt("OffY", offsetY);
		compound.putInt("OffZ", offsetZ);
		compound.putBoolean("Finished", finished);
		return compound;
	}

	public void tick() {
		assert world != null;
		if (!world.isRemote()) {
			boolean flag = !finished;
			if (flag) {
				if (offsetY == 0 || offsetX == 5 && offsetZ == 5) {
					offsetX = -5;
					offsetZ = -5;
					offsetY += -1;
				} else if (offsetX == 5) {
					offsetX = -5;
					offsetZ++;
				} else offsetX++;

				BlockPos bp = pos.add(offsetX, offsetY, offsetZ);
				if (bp.getY() < 0) {
					finished = true;
					world.setBlockState(pos, world.getBlockState(this.pos).with(ActiveTileBlock.LIT, false), 3);
					return;
				}

				BlockState bs = world.getBlockState(bp);
				Block b = bs.getBlock();
				if (!Tags.Blocks.DIRT.contains(b) && !Tags.Blocks.STONE.contains(b) && !Tags.Blocks.COBBLESTONE.contains(b)) {
					List<ItemStack> drops = bs.getDrops(new LootContext.Builder((ServerWorld)world).withParameter(LootParameters.POSITION, pos).withParameter(LootParameters.TOOL, ItemStack.EMPTY));
					List<IItemHandler> inv = new ArrayList<>();
					if (!drops.isEmpty()) {
						world.removeBlock(bp, false);

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
				energy -= 200;
			}

			if (flag != lastFlag) {
				world.setBlockState(pos, world.getBlockState(pos).with(ActiveTileBlock.LIT, flag), 3);
				lastFlag = flag;
			}
		}

	}


}
