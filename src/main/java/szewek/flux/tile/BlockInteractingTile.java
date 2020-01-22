package szewek.flux.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

abstract class BlockInteractingTile extends PoweredTile {
	protected int offsetX;
	protected int offsetY;
	protected int offsetZ;
	protected boolean disabled = true;

	public BlockInteractingTile(TileEntityType tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		offsetX = compound.getInt("OffX");
		offsetY = compound.getInt("OffY");
		offsetZ = compound.getInt("OffZ");
		disabled = compound.getBoolean("Disabled");
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("OffX", offsetX);
		compound.putInt("OffY", offsetY);
		compound.putInt("OffZ", offsetZ);
		compound.putBoolean("Disabled", disabled);
		return compound;
	}
}
