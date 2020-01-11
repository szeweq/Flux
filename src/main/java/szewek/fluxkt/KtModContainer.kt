package szewek.fluxkt

import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.*
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

class KtModContainer(
        info: IModInfo?,
        className: String?,
        classLoader: ClassLoader?,
        private val scanData: ModFileScanData
) : ModContainer(info) {
    private val logger = LogManager.getLogger()
    private lateinit var mod: Any
    private var modClass: Class<*>
    val eventBus: IEventBus

    init {
        logger.debug("Creating KotlinModContainer instance for {} with classLoader {} & {}", className, classLoader, javaClass.classLoader)

        val eventConsumer = Consumer(::fireEvent).andThen(::afterEvent)

        triggerMap[ModLoadingStage.CONSTRUCT] = Consumer(::constructMod).andThen(::afterEvent)
        triggerMap[ModLoadingStage.CREATE_REGISTRIES] = eventConsumer
        triggerMap[ModLoadingStage.LOAD_REGISTRIES] = eventConsumer
        triggerMap[ModLoadingStage.COMMON_SETUP] = eventConsumer
        triggerMap[ModLoadingStage.SIDED_SETUP] = eventConsumer
        triggerMap[ModLoadingStage.ENQUEUE_IMC] = eventConsumer
        triggerMap[ModLoadingStage.PROCESS_IMC] = eventConsumer
        triggerMap[ModLoadingStage.COMPLETE] = eventConsumer
        triggerMap[ModLoadingStage.GATHERDATA] = eventConsumer
        eventBus = BusBuilder.builder().setExceptionHandler(::onEventFailed).setTrackPhases(false).build()
        configHandler = Optional.of(Consumer { eventBus.post(it) })
        val ctx = KtModLoadingContext(this)
        contextExtension = Supplier { ctx }
        try {
            modClass = Class.forName(className, true, classLoader)
            logger.info(Logging.LOADING, "Loaded modclass {} with {}", modClass.name, modClass.classLoader)
        } catch (e: Throwable) {
            logger.error(Logging.LOADING, "Failed to load class {}", className, e)
            throw ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", e)
        }
    }

    private fun onEventFailed(eventBus: IEventBus, event: Event, iEventListeners: Array<IEventListener>, i: Int, throwable: Throwable) {
        logger.error(EventBusErrorMessage(event, i, iEventListeners, throwable))
    }

    private fun fireEvent(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
        val event = lifecycleEvent.getOrBuildEvent(this)
        logger.debug(Logging.LOADING, "Firing event for modid ${getModId()} : $event")

        try {
            eventBus.post(event)
            logger.debug(Logging.LOADING, "Fired event for modid ${getModId()} : $event")

        } catch (e: Throwable) {
            logger.error(Logging.LOADING,"An error occurred while dispatching event ${lifecycleEvent.fromStage()} to $modId")
            throw ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.errorduringevent", e)
        }
    }

    private fun afterEvent(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
        if (currentState == ModLoadingStage.ERROR) {
            logger.error(Logging.LOADING, "An error occurred while dispatching event ${lifecycleEvent.fromStage()} to $modId")
        }
    }

    private fun constructMod(event: LifecycleEventProvider.LifecycleEvent) {
        try {
            logger.info(Logging.LOADING, "Loading mod instance {} of type {}", getModId(), modClass.name)
            mod = modClass.kotlin.objectInstance ?: modClass.newInstance()
            logger.info(Logging.LOADING, "Loaded mod instance {} of type {}", getModId(), modClass.name)
        } catch (e: Throwable) {
            logger.error(Logging.LOADING, "Failed to create mod instance. ModID: {}, class {}", getModId(), modClass.name, e)
            throw ModLoadingException(modInfo, event.fromStage(), "fml.modloading.failedtoloadmod", e, modClass)
        }
        try {
            logger.info(Logging.LOADING, "Injecting Automatic event subscribers for {}", getModId())
            KtClassUtils.injectEvents(this, scanData, modClass.classLoader)
            logger.info(Logging.LOADING, "Completed Automatic event subscribers for {}", getModId())
        } catch (e: Throwable) {
            logger.error(Logging.LOADING, "Failed to register automatic subscribers. ModID: {}, class {}", getModId(), modClass.name, e)
            throw ModLoadingException(modInfo, event.fromStage(), "fml.modloading.failedtoloadmod", e, modClass)
        }
    }

    override fun getMod() = mod

    override fun matches(mod: Any?) = this.mod == mod

}