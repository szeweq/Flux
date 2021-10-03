package szewek.mcgen.util

import com.fasterxml.jackson.jr.ob.comp.ArrayComposer
import com.fasterxml.jackson.jr.ob.comp.ComposerBase
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer

inline fun <P : ComposerBase> ObjectComposer<P>.obj(k: String, fn: ObjComposable<ObjectComposer<P>>): ObjectComposer<P> =
    startObjectField(k).also(fn).end()

inline fun <P : ComposerBase> ArrayComposer<P>.obj(fn: ObjComposable<ArrayComposer<P>>): ArrayComposer<P> =
    startObject().also(fn).end()

inline fun <P : ComposerBase> ObjectComposer<P>.arr(k: String, fn: ArrComposable<ObjectComposer<P>>): ObjectComposer<P> =
    startArrayField(k).also(fn).end()

inline fun <P : ComposerBase> ObjectComposer<P>.putStrings(k: String, v: Array<out String>): ObjectComposer<P> =
    startArrayField(k).apply { for (s in v) add(s) }.end()

inline fun <P : ComposerBase> ObjectComposer<P>.singleObj(f: String, k: String, v: String): ObjectComposer<P> =
    startObjectField(f).put(k, v).end()

inline fun <P : ComposerBase> ArrayComposer<P>.singleObj(k: String, v: String): ArrayComposer<P> =
    startObject().put(k, v).end()

typealias ObjComposable<P> = ObjectComposer<P>.() -> Unit
typealias ArrComposable<P> = ArrayComposer<P>.() -> Unit

typealias WriteFunc = ObjectComposer<*>.() -> Unit