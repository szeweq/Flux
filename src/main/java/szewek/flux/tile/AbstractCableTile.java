package szewek.flux.tile;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public abstract class AbstractCableTile extends TileEntity implements ITickableTileEntity {
	private int cooldown;
	private byte sideFlag;

	public AbstractCableTile(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void tick() {
		assert world != null;
		if (!world.isRemote) {
			if (--cooldown > 0) {
				return;
			}
			cooldown = 4;
			byte sf = (byte) (sideFlag ^ 63);
			sideFlag = 0;
			int i = 0;
			final Direction[] dirs = Direction.values();
			while (i < 6 && sf != 0) {
				if ((sf & 1) != 0) {
					updateSide(dirs[i]);
				}
				sf >>= 1;
				i++;
			}
		}
	}

	protected abstract void updateSide(Direction dir);
}
