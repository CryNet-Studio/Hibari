package com.huanli233.hibari.runtime.effects

import android.util.Log
import com.huanli233.hibari.runtime.DisallowTunableCalls
import com.huanli233.hibari.runtime.RememberObserver
import com.huanli233.hibari.runtime.Tunable
import com.huanli233.hibari.runtime.currentTuner
import com.huanli233.hibari.runtime.remember
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Cancels the coroutine a [LaunchedEffect] slot owns. Without this the `Job` is just a remembered
 * value, so a key change or a node leaving the tree drops it while the coroutine keeps running —
 * upstream Compose cancels in both cases.
 */
private class LaunchedEffectCanceller(val job: Job) : RememberObserver {
    override fun onRemembered() {}

    override fun onForgotten() {
        job.cancel(CancellationException("LaunchedEffect forgotten"))
    }

    override fun onAbandoned() {
        job.cancel(CancellationException("LaunchedEffect abandoned"))
    }
}

@Tunable
fun LaunchedEffect(vararg keys: Any? = emptyArray(), block: suspend CoroutineScope.() -> Unit) {
    val coroutineScope = currentTuner.coroutineScope
    remember(*keys) { LaunchedEffectCanceller(coroutineScope.launch(block = block)) }
}

@PublishedApi
internal class CompositionScopedCoroutineScopeCanceller(
    val coroutineScope: CoroutineScope
) : RememberObserver {
    override fun onRemembered() {
        // Nothing to do
    }

    override fun onForgotten() {
        coroutineScope.cancel(CancellationException())
    }

    override fun onAbandoned() {
        coroutineScope.cancel(CancellationException())
    }
}

@Tunable
inline fun rememberCoroutineScope(
    crossinline getContext: @DisallowTunableCalls () -> CoroutineContext =
        { EmptyCoroutineContext }
): CoroutineScope {
    val tuner = currentTuner
    val wrapper = remember {
        CompositionScopedCoroutineScopeCanceller(
            CoroutineScope(tuner.coroutineScope.coroutineContext + Job(tuner.coroutineScope.coroutineContext[Job]) + getContext())
        )
    }
    return wrapper.coroutineScope
}
