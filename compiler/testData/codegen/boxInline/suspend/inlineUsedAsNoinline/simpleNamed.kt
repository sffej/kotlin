// FILE: inlined.kt
// WITH_RUNTIME
// NO_CHECK_LAMBDA_INLINING

inline suspend fun inlineMe() {
    suspendHere()
    suspendHere()
    suspendHere()
    suspendHere()
}

// FILE: inlineSite.kt

import kotlin.coroutines.experimental.*
import kotlin.coroutines.experimental.intrinsics.*

suspend fun suspendHere() = suspendCoroutineOrReturn<Unit> {
    it.resume(Unit)
    COROUTINE_SUSPENDED
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(object: Continuation<Unit> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resume(value: Unit) {
        }

        override fun resumeWithException(exception: Throwable) {
            throw exception
        }
    })
}

suspend fun inlineSite() {
    inlineMe()
    inlineMe()
}

fun box(): String {
    builder {
        inlineSite()
    }
    return "OK"
}