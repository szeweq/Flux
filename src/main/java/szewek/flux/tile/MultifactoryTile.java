package szewek.flux.tile;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import szewek.flux.F;

public class MultifactoryTile extends PoweredDeviceTile {
	public MultifactoryTile() {
		super(F.T.MULTIFACTORY);
	}

	@Override
	protected void serverTick(World w) {

	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("container.flux.multifactory");
	}

	@Override
	protected Container createMenu(int id, PlayerInventory player) {
		return null;
	}

	@Override
	public int getSizeInventory() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return null;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {

	}

	@Override
	public void clear() {

	}
}
