package com.huanli233.hibari.wearsample

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.huanli233.hibari.foundation.Box
import com.huanli233.hibari.foundation.attributes.matchParentSize
import com.huanli233.hibari.foundation.attributes.padding
import com.huanli233.hibari.foundation.attributes.size
import com.huanli233.hibari.runtime.HibariView
import com.huanli233.hibari.runtime.getValue
import com.huanli233.hibari.runtime.mutableStateOf
import com.huanli233.hibari.runtime.remember
import com.huanli233.hibari.runtime.setValue
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.unit.DpSize
import com.huanli233.hibari.ui.unit.dp
import com.huanli233.hibari.wear.ArcProgressIndicator
import com.huanli233.hibari.wear.ButtonGroup
import com.huanli233.hibari.wear.CurvedRow
import com.huanli233.hibari.wear.EdgeButton
import com.huanli233.hibari.wear.EdgeButtonDefaults
import com.huanli233.hibari.wear.EdgeButtonSize
import com.huanli233.hibari.wear.HorizontalPageIndicator
import com.huanli233.hibari.wear.LevelIndicator
import com.huanli233.hibari.wear.MaterialTheme
import com.huanli233.hibari.wear.Picker
import com.huanli233.hibari.wear.ScreenScaffold
import com.huanli233.hibari.wear.SegmentedCircularProgressIndicator
import com.huanli233.hibari.wear.Slider
import com.huanli233.hibari.wear.Stepper
import com.huanli233.hibari.wear.Text
import com.huanli233.hibari.wear.Vignette
import com.huanli233.hibari.wear.VignettePosition
import com.huanli233.hibari.wear.lazy.ExpandableState
import com.huanli233.hibari.wear.lazy.ScalingLazyColumn
import com.huanli233.hibari.wear.lazy.expandableItems
import com.huanli233.hibari.wear.placeholder
import com.huanli233.hibari.wear.rememberPickerState
import com.huanli233.hibari.wear.rememberPlaceholderState

/**
 * Every component here has only ever been compiled from the inside. This activity calls them the way
 * a real consumer does, so a signature that is unusable — a required slot with no default, a
 * parameter type nothing can satisfy — surfaces as a compile error instead of as a bug report.
 */
class ApiSmokeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            HibariView(this) {
                var sliderValue by remember { mutableStateOf(2f) }
                var stepperValue by remember { mutableStateOf(3) }
                var page by remember { mutableStateOf(1) }
                val expandable = remember { ExpandableState() }
                val pickerState = rememberPickerState(initialNumberOfOptions = 5)
                val skeleton = rememberPlaceholderState(isVisible = true)

                MaterialTheme {
                    ScreenScaffold { contentPadding ->
                        Vignette(VignettePosition.TopAndBottom)
                        CurvedRow(modifier = Modifier.matchParentSize()) {
                            curvedText("API smoke", maxSweepAngle = 60f)
                        }
                        ScalingLazyColumn(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(contentPadding),
                        ) {
                            item {
                                Slider(
                                    value = sliderValue,
                                    onValueChange = { sliderValue = it },
                                    steps = 5,
                                )
                            }
                            item {
                                Stepper(
                                    value = stepperValue,
                                    onValueChange = { stepperValue = it },
                                    valueProgression = 1..10,
                                    decreaseIcon = { Text("-") },
                                    increaseIcon = { Text("+") },
                                    content = { Text("Step $stepperValue") },
                                )
                            }
                            item {
                                LevelIndicator(value = sliderValue / 5f)
                            }
                            item {
                                HorizontalPageIndicator(pageCount = 4, currentPage = page)
                            }
                            item {
                                val ring = Modifier.size(DpSize(56.dp, 56.dp))
                                SegmentedCircularProgressIndicator(
                                    segmentCount = 12,
                                    progress = 0.5f,
                                    modifier = ring,
                                )
                            }
                            item {
                                ArcProgressIndicator(modifier = Modifier.size(DpSize(56.dp, 56.dp)))
                            }
                            item {
                                ButtonGroup {
                                    Text("Left")
                                    Text("Right")
                                }
                            }
                            item {
                                Picker(
                                    state = pickerState,
                                    contentDescription = { "Option ${pickerState.selectedOptionIndex}" },
                                ) { index ->
                                    Text("Option $index")
                                }
                            }
                            item {
                                Box(modifier = Modifier.size(DpSize(140.dp, 40.dp)).placeholder(skeleton)) { }
                            }
                            expandableItems(state = expandable, key = "smoke", count = 3) { index ->
                                Text("Child $index")
                            }
                            item {
                                Text("Page $page / step $stepperValue")
                            }
                        }
                        // Pinned to the dial edge rather than listed, because the shape is the whole
                        // point of it and only an edge-hugging placement shows whether the arcs,
                        // the content window and the two fades are right.
                        EdgeButton(
                            onClick = { page = (page + 1) % 4 },
                            modifier = Modifier.gravity(Gravity.BOTTOM),
                            buttonSize = EdgeButtonSize.Medium,
                        ) {
                            val iconSize = EdgeButtonDefaults.iconSizeFor(EdgeButtonSize.Medium)
                            Box(modifier = Modifier.size(DpSize(iconSize, iconSize))) { }
                            Text("Next ${page + 1}")
                        }
                        EdgeButton(
                            onClick = { },
                            modifier = Modifier.gravity(Gravity.TOP),
                            enabled = false,
                        ) {
                            Text("Disabled")
                        }
                    }
                }
            }
        )
    }
}
