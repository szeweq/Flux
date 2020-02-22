package szewek.flux.util.metals;

public class Metal {
	public final int color;
	public final String metalName;
	public final int harvestLevel;

	Metal(int color, String metalName, int harvestLevel) {
		this.color = color;
		this.metalName = metalName;
		this.harvestLevel = harvestLevel;
		Metals.allMetals.add(this);
	}

	public final boolean nonVanilla() {
		return !(this instanceof VanillaMetal);
	}

	public boolean nonAlloy() {
		return !(this instanceof AlloyMetal);
	}

	public boolean notVanillaOrAlloy() {
		return nonVanilla() && nonAlloy();
	}

}
