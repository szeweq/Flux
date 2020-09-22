package szewek.flux.util.inventory;

public final class IOSize {
	public final int in, out, all;

	public IOSize(int i, int o) {
		in = i;
		out = o;
		all = i + o;
	}
}
