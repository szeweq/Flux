package szewek.fl.util;

@FunctionalInterface
public interface GeoFunction<T> {
	T apply(int x, int y, int z);
}
