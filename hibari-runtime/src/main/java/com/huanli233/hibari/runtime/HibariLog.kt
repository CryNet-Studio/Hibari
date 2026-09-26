package com.huanli233.hibari.runtime

import android.util.Log

/**
 * Gate for the composition/patch verbose logs. The old code called `Log.d`/`Log.i` directly in the
 * hot reconfigure path, so the message argument (e.g. `"$node"`, which stringifies the whole
 * modifier chain and children) was built on every emit even when the tag was filtered out.
 *
 * The verbose helpers take a `() -> String` so nothing is allocated unless [enabled] is on; error
 * stays unconditional because it only fires on a genuine failure and is the main field clue.
 */
internal object HibariLog {

    @Volatile
    var enabled: Boolean = false

    inline fun d(tag: String, message: () -> String) {
        if (enabled) Log.d(tag, message())
    }

    inline fun i(tag: String, message: () -> String) {
        if (enabled) Log.i(tag, message())
    }

    fun e(tag: String, message: () -> String) {
        Log.e(tag, message())
    }

    /** Separate overload rather than a defaulted trailing `cause` so the lambda stays last. */
    fun e(tag: String, message: () -> String, cause: Throwable) {
        Log.e(tag, message(), cause)
    }
}
