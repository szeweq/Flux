package szewek.fluxkt;

import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider;
import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FluxKtLanguageProvider implements IModLanguageProvider {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public String name() {
		return "fluxkt";
	}

	@Override
	public Consumer<ModFileScanData> getFileVisitor() {
		return scanData -> {
			Map<String, KtModLoader> map = new HashMap<>();
			for(ModFileScanData.AnnotationData ad : scanData.getAnnotations()) {
				if (ad.getAnnotationType().equals(FMLJavaModLanguageProvider.MODANNOTATION)) {
					String className = ad.getClassType().getClassName();
					String modId = (String) ad.getAnnotationData().get("value");
					LOGGER.debug(Logging.SCAN, "Found @Mod class {} with id {}", className, modId);
					map.put(modId, new KtModLoader(className, modId));
				}
			}
			scanData.addLanguageLoader(map);
		};
	}

	@Override
	public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(Supplier<R> consumeEvent) {

	}

	private static class KtModLoader implements IModLanguageLoader {
		private static final Logger LOGGER = FluxKtLanguageProvider.LOGGER;
		private final String className, modId;

		KtModLoader(String className, String modId) {
			this.className = className;
			this.modId = modId;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T loadMod(IModInfo info, ClassLoader modClassLoader, ModFileScanData modFileScanResults) {
			try {
				final Class<?> ktContainer = Class.forName("szewek.fluxkt.KtModContainer", true, Thread.currentThread().getContextClassLoader());
				LOGGER.debug(Logging.LOADING, "Loading KtModContainer from classloader {} - got {}", Thread.currentThread().getContextClassLoader(), ktContainer.getClassLoader());
				final Constructor<?> c = ktContainer.getConstructor(IModInfo.class, String.class, ClassLoader.class, ModFileScanData.class);
				return (T) c.newInstance(info, className, modClassLoader, modFileScanResults);
			} catch (Exception e) {
				LOGGER.fatal(Logging.LOADING, "Could not create a Kotlin mod container");
				throw new RuntimeException(e);
			}
		}

		public String getModId() {
			return modId;
		}
	}
}
