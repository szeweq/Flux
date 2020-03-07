package szewek.fl.util;

/**
 * Functional interface for obtaining an object using 3D coordinates.
 * @param <T>
 */
@FunctionalInterface
public interface GeoFunction<T> {
	T apply(int x, int y, int z);
}
