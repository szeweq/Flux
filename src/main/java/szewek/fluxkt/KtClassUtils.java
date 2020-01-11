package szewek.fluxkt;

import kotlin.reflect.KClass;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.util.*;

import static net.minecraftforge.fml.Logging.LOADING;

public final class KtClassUtils {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Type EVENT_BUS_SUBSCRIBER = Type.getType(Mod.EventBusSubscriber.class);

	static Object getModInstance(Class<?> cl) {
		KClass<?> kcl = kotlin.jvm.internal.Reflection.getOrCreateKotlinClass(cl);
		Object o = kcl.getObjectInstance();
		if (o == null) {
			o = cl;
		}
		return o;
	}

	@SuppressWarnings("unchecked")
	static void injectEvents(KtModContainer mod, ModFileScanData scanData, ClassLoader classLoader) {
		if (scanData == null) return;
		LOGGER.debug(LOADING,"Attempting to inject @EventBusSubscriber classes into the eventbus for {}", mod.getModId());
		for (ModFileScanData.AnnotationData data : scanData.getAnnotations()) {
			if (EVENT_BUS_SUBSCRIBER.equals(data.getAnnotationType())) {
				Map<String, Object> ad = data.getAnnotationData();
				List<ModAnnotation.EnumHolder> sidesValue = (List<ModAnnotation.EnumHolder>) ad.getOrDefault("value", Arrays.asList(new ModAnnotation.EnumHolder(null, "CLIENT"), new ModAnnotation.EnumHolder(null, "DEDICATED_SERVER")));
				EnumSet<Dist> sides = EnumSet.noneOf(Dist.class);
				for (ModAnnotation.EnumHolder side : sidesValue) {
					sides.add(Dist.valueOf(side.getValue()));
				}
				Object modId = ad.getOrDefault("modid", mod.getModId());
				ModAnnotation.EnumHolder busTargetHolder = (ModAnnotation.EnumHolder) ad.getOrDefault("bus", new ModAnnotation.EnumHolder(null, "FORGE"));
				Mod.EventBusSubscriber.Bus busTarget = Mod.EventBusSubscriber.Bus.valueOf(busTargetHolder.getValue());
				if (Objects.equals(mod.getModId(), modId) && sides.contains(FMLEnvironment.dist)) {
					try {
						String className = data.getClassType().getClassName();
						LOGGER.debug(Logging.LOADING, "Subscribing class/object ${} to {}", className, busTarget);
						Object modInstance = getModInstance(Class.forName(className, true, classLoader));
						(busTarget == Mod.EventBusSubscriber.Bus.MOD ? mod.getEventBus() : MinecraftForge.EVENT_BUS).register(modInstance);
					} catch (ClassNotFoundException e) {
						LOGGER.fatal(LOADING, "Failed to load mod class/object {} for @EventBusSubscriber annotation", data.getClassType(), e);
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private KtClassUtils() {}
}
