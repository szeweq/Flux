package szewek.flux.tile.part;

import net.minecraft.util.Direction;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.flux.energy.EnergyCache;

public class GeneratorEnergy extends EnergyPart {
    private int tickCount;

    public GeneratorEnergy(int max) {
        super(max);
    }

    public boolean generate(int amount) {
        boolean b = stored + amount <= max;
        if (b) {
            stored += amount;
        }
        return b;
    }

    public void share(EnergyCache cache) {
        tickCount++;
        if (tickCount <= 3 && stored <= 0) {
            return;
        }
        tickCount = 0;
        try {
            for (Direction d : Direction.values()) {
                IEnergyStorage ie = cache.getCached(d);
                if (ie == null || !ie.canReceive()) {
                    continue;
                }
                int r = 40000;
                if (r >= stored) r = stored;
                r = ie.receiveEnergy(r, true);
                if (r > 0) {
                    stored -= r;
                    ie.receiveEnergy(r, false);
                }
            }
        } catch (Exception ignored) {
            // Keep garbage "integrations" away!
            cache.clear();
            // A good mod developer ALWAYS invalidates LazyOptional instances!
        }
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (maxExtract <= 0) return 0;
        int r = Math.min(maxExtract, stored);
        if (r > 0 && !simulate) {
            stored -= r;
        }
        return r;
    }
}
