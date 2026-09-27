package com.huanli233.hibari.sample

import androidx.multidex.MultiDexApplication
import com.google.android.material.color.DynamicColors
import com.huanli233.hibari.runtime.TuneStats

class SampleApp: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        // logcat -s HibariStats:i — one line per 60 tunes. Flip to false to collect nothing.
        TuneStats.enabled = true
    }

}