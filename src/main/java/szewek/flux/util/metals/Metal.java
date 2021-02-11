package szewek.flux.util.metals;

public class Metal {
	public static final int VANILLA = 1, ALLOY = 2, NO_ORE = 4;

	public final int color;
	public final String metalName;
	public final int harvestLevel;
	public final int flags;

	Metal(int color, String metalName, int harvestLevel, int flags) {
		this.color = color;
		this.metalName = metalName;
		this.harvestLevel = harvestLevel;
		this.flags = flags;
		Metals.allMetals.add(this);
	}

	Metal(int color, String metalName, int harvestLevel) {
		this(color, metalName, harvestLevel, 0);
	}

	static Metal vanilla(int color, String metalName, int harvestLevel) {
		return new Metal(color, metalName, harvestLevel, VANILLA);
	}

	static Metal alloy(int color, String metalName, int harvestLevel) {
		return new Metal(color, metalName, harvestLevel, ALLOY);
	}
}
