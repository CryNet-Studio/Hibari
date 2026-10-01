package com.huanli233.hibari.wearsample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huanli233.hibari.foundation.attributes.matchParentSize
import com.huanli233.hibari.foundation.attributes.padding
import com.huanli233.hibari.foundation.attributes.size
import com.huanli233.hibari.runtime.HibariView
import com.huanli233.hibari.runtime.currentContext
import com.huanli233.hibari.runtime.effects.LaunchedEffect
import com.huanli233.hibari.runtime.getValue
import com.huanli233.hibari.runtime.mutableStateOf
import com.huanli233.hibari.runtime.remember
import com.huanli233.hibari.runtime.setValue
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.unit.DpSize
import com.huanli233.hibari.ui.unit.dp
import com.huanli233.hibari.wear.AppScaffold
import com.huanli233.hibari.wear.Button
import com.huanli233.hibari.wear.Card
import com.huanli233.hibari.wear.Checkbox
import com.huanli233.hibari.wear.CircularProgressIndicator
import com.huanli233.hibari.wear.ListHeader
import com.huanli233.hibari.wear.MaterialTheme
import com.huanli233.hibari.wear.RadioButton
import com.huanli233.hibari.wear.ScreenScaffold
import com.huanli233.hibari.wear.Switch
import com.huanli233.hibari.wear.Text
import com.huanli233.hibari.wear.TimeText
import com.huanli233.hibari.wear.lazy.ScalingLazyColumn
import com.huanli233.hibari.wear.lazy.ScalingLazyListState

/**
 * Runs the ported surface on a real screen: every item below is a component the Wear Compose port
 * has to reproduce, and each one is wired to state so a retune is visible rather than assumed.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            HibariView(this) {
                val context = currentContext
                val listState = remember { ScalingLazyListState() }
                var taps by remember { mutableStateOf(0) }
                var checked by remember { mutableStateOf(true) }
                var radio by remember { mutableStateOf(1) }
                var progress by remember { mutableStateOf(0.35f) }
                var indeterminate by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    Toast.makeText(context, "Hibari Wear", Toast.LENGTH_SHORT).show()
                }

                MaterialTheme {
                    // AppScaffold owns the background and the ambient content colour upstream gives
                    // it. Without this layer a Text that is not inside a Card/Button/ListHeader has no
                    // ambient colour to resolve against and keeps the raw TextView theme colour,
                    // because this port seeds LocalContentColor with Unspecified rather than
                    // upstream's White (material3/ContentColor.kt:34).
                    AppScaffold(
                        timeText = { TimeText() },
                    ) {
                        ScreenScaffold { contentPadding ->
                            // The clock comes in through `AppScaffold`'s own slot, as upstream emits it
                            // there (`material3/AppScaffold.kt:56`, default `{ TimeText() }`), instead of
                            // being hand-placed in the content — which, now that the slot has a default,
                            // would draw two clocks on top of each other.
                            ScalingLazyColumn(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(contentPadding),
                                state = listState,
                            ) {
                                item {
                                    ListHeader {
                                        Text("Hibari Wear")
                                    }
                                }
                                item {
                                    Button(onClick = { taps += 1 }) {
                                        Text("Taps $taps")
                                    }
                                }
                                item {
                                    Card {
                                        Text("Card")
                                        Text("Two lines of body text inside a filled card.")
                                    }
                                }
                                item {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { checked = it },
                                    )
                                }
                                item {
                                    Switch(
                                        checked = !checked,
                                        onCheckedChange = { checked = !it },
                                    )
                                }
                                pos(3) { index ->
                                    RadioButton(
                                        selected = radio == index,
                                        onClick = { radio = index },
                                        modifier = Modifier.padding(vertical = 2.dp),
                                    )
                                }
                                item {
                                    val ringSize = Modifier.size(DpSize(48.dp, 48.dp))
                                    if (indeterminate) {
                                        CircularProgressIndicator(modifier = ringSize)
                                    } else {
                                        CircularProgressIndicator(progress, ringSize)
                                    }
                                }
                                item {
                                    Button(onClick = {
                                        progress = (progress + 0.15f).let { if (it > 1f) 0f else it }
                                    }) {
                                        Text("Progress ${(progress * 100).toInt()}%")
                                    }
                                }
                                item {
                                    Button(onClick = { indeterminate = !indeterminate }) {
                                        Text(if (indeterminate) "Stop" else "Spin")
                                    }
                                }
                                item {
                                    Button(onClick = { listState.animateScrollToItem(0) }) {
                                        Text("Back to top")
                                    }
                                }
                                items(Notes) { note ->
                                    Card {
                                        Text(note.title)
                                        Text(note.body)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    private data class Note(val title: String, val body: String)

    private companion object {
        val Notes = listOf(
            Note("Scaling", "items shrink and fade toward the bezel"),
            Note("Curved", "text follows the screen radius"),
            Note("Container", "state colours tween on press"),
        )
    }
}
