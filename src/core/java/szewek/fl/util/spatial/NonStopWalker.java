package szewek.fl.util.spatial;

public class NonStopWalker extends SpatialWalker {
    public NonStopWalker(int x, int y, int z) {
        super(-x, -y, -z, x, y, z);
    }

    public NonStopWalker(int ax, int ay, int az, int zx, int zy, int zz) {
        super(ax, ay, az, zx, zy, zz);
    }

    @Override
    public boolean canWalk() {
        return true;
    }
}
