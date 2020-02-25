package szewek.flux.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import szewek.fl.util.SpatialWalker;

abstract class BlockInteractingTile extends PoweredTile {
	protected final SpatialWalker walker;
	protected boolean disabled = false;

	public BlockInteractingTile(TileEntityType teType, SpatialWalker walker) {
		super(teType);
		this.walker = walker;
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		walker.read(compound);
		disabled = compound.getBoolean("Disabled");
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		walker.write(compound);
		compound.putBoolean("Disabled", disabled);
		return compound;
	}
}
