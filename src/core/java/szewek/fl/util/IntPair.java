package szewek.fl.util;

/**
 * Pair of integers (primitive values).
 */
public final class IntPair implements Comparable<IntPair> {
	public static final IntPair ZERO = of(0, 0);

	public final int
			l,
			r;

	private IntPair(int l, int r) {
		this.l = l;
		this.r = r;
	}

	public static IntPair of(int l, int r) {
		return new IntPair(l, r);
	}

	@Override
	public int compareTo(IntPair o) {
		return l == o.l ? Integer.compare(r, o.r) : Integer.compare(l, o.l);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IntPair intPair = (IntPair) o;
		return l == intPair.l && r == intPair.r;
	}

	@Override
	public int hashCode() {
		return 31 * l + r;
	}

	@Override
	public String toString() {
		return "(" + l + ',' + r + ')';
	}
}
