@file:kotlin.jvm.JvmName("LogTimerKt")
package szewek.mcgen.util

import kotlin.system.measureNanoTime

inline fun logNanoTime(name: String, crossinline fn: () -> Unit) {
    val dur = measureNanoTime(fn) / 1_000_000.0
    println("[$name] $dur ms")
}