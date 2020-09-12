package szewek.fl.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * SpatialWalker is a programmable block position iterator.
 */
public abstract class SpatialWalker {
	private int x, y, z;
	protected final int minX, minY, minZ;
	protected final int maxX, maxY, maxZ;
	private Action[] actions;

	public SpatialWalker(int ax, int ay, int az, int zx, int zy, int zz) {
		minX = ax;
		minY = ay;
		minZ = az;
		maxX = zx;
		maxY = zy;
		maxZ = zz;
	}

	public abstract boolean canWalk();

	public void startFrom(boolean sx, boolean sy, boolean sz) {
		x = sx ? minX : maxX;
		y = sy ? minY : maxY;
		z = sz ? minZ : maxZ;
	}

	public void putActions(Action... actions) {
		this.actions = actions.clone();
	}

	public boolean walk() {
		if (canWalk()) {
			for(int i = 0; i < actions.length; i++) {
				Action a = actions[i];
				switch (a) {
					case X_POS:
						if (++x > maxX) {
							x = minX;
							continue;
						}
						break;
					case X_NEG:
						if (--x < minX) {
							x = maxX;
							continue;
						}
						break;
					case Y_POS:
						if (++y > maxY) {
							y = minY;
							continue;
						}
						break;
					case Y_NEG:
						if (--y < minY) {
							y = maxY;
							continue;
						}
						break;
					case Z_POS:
						if (++z > maxZ) {
							z = minZ;
							continue;
						}
						break;
					case Z_NEG:
						if (--z < minZ) {
							z = maxZ;
							continue;
						}
						break;
					case LOOP:
						i = -1;
						continue;
				}
				return true;
			}
		}
		return false;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public void read(CompoundNBT compound) {
		x = compound.getInt("OffX");
		y = compound.getInt("OffY");
		z = compound.getInt("OffZ");
	}

	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("OffX", x);
		compound.putInt("OffY", y);
		compound.putInt("OffZ", z);
		return compound;
	}

	public BlockPos getPosOffset(BlockPos pos) {
		return pos.add(x, y, z);
	}

	public static class NonStop extends SpatialWalker {
		public NonStop(int x, int y, int z) {
			super(-x, -y, -z, x, y, z);
		}

		public NonStop(int ax, int ay, int az, int zx, int zy, int zz) {
			super(ax, ay, az, zx, zy, zz);
		}

		@Override
		public boolean canWalk() {
			return true;
		}
	}

	public enum Action {
		X_POS, X_NEG, Y_POS, Y_NEG, Z_POS, Z_NEG, LOOP
	}
}
