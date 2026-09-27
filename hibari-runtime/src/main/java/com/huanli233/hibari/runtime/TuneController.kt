package com.huanli233.hibari.runtime

import android.app.Activity
import android.view.LayoutInflater
import com.huanli233.hibari.ui.HibariFactory

object TuneController {

    fun tune(session: Tunation, factories: List<HibariFactory> = emptyList()) {
        val beganAt = TuneStats.now()
        val beganAllocs = TuneStats.allocNow()
        val tuner = session.tuner ?: Tuner(session)
        try {
            session.tuner = tuner
            tuner.roundChangedStates = session.takeChangedStates()

            val newNodeTree = tuner.run {
                startComposition()
                runTunable(session)
                endComposition()
            }
            val composedAt = TuneStats.now()

            val patcher = session.patcher ?: Patcher(
                Renderer(
                    factories + HibariFactory(
                        (session.hostView.context as? Activity)?.layoutInflater
                            ?: LayoutInflater.from(session.hostView.context)
                    ),
                    session.hostView
                )
            ).also { session.patcher = it }

            val oldNodeTree = session.lastTree
            patcher.patch(session.hostView, oldNodeTree, newNodeTree)

            session.lastTree = newNodeTree
            TuneStats.recordTune(
                compositionNanos = composedAt - beganAt,
                patchNanos = TuneStats.now() - composedAt,
                allocsBefore = beganAllocs,
                allocsAfter = TuneStats.allocNow()
            )
        } finally {
            session.tuneData = tuner.getTuneData()
        }
    }
}