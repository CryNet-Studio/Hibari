package com.huanli233.hibari.wearsample

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.huanli233.hibari.foundation.Box
import com.huanli233.hibari.foundation.Column
import com.huanli233.hibari.foundation.Row
import com.huanli233.hibari.foundation.attributes.matchParentSize
import com.huanli233.hibari.foundation.attributes.padding
import com.huanli233.hibari.foundation.attributes.size
import com.huanli233.hibari.runtime.HibariView
import com.huanli233.hibari.runtime.currentContext
import com.huanli233.hibari.runtime.getValue
import com.huanli233.hibari.runtime.mutableStateOf
import com.huanli233.hibari.runtime.remember
import com.huanli233.hibari.runtime.setValue
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.geometry.CircleShape
import com.huanli233.hibari.ui.graphics.Color
import com.huanli233.hibari.ui.layout.Alignment
import com.huanli233.hibari.ui.text.TextAlign
import com.huanli233.hibari.ui.unit.DpSize
import com.huanli233.hibari.ui.unit.PaddingValues
import com.huanli233.hibari.ui.unit.dp
import com.huanli233.hibari.ui.unit.sp
import com.huanli233.hibari.ui.unit.toPx
import com.huanli233.hibari.wear.AmbientMode
import com.huanli233.hibari.wear.AmbientModeHost
import com.huanli233.hibari.wear.AnimatedText
import com.huanli233.hibari.wear.AppCard
import com.huanli233.hibari.wear.ArcProgressIndicator
import com.huanli233.hibari.wear.BorderStroke
import com.huanli233.hibari.wear.Button
import com.huanli233.hibari.wear.ButtonDefaults
import com.huanli233.hibari.wear.ButtonGroup
import com.huanli233.hibari.wear.Card
import com.huanli233.hibari.wear.CardDefaults
import com.huanli233.hibari.wear.ChildButton
import com.huanli233.hibari.wear.CompactButton
import com.huanli233.hibari.wear.CompactButtonDefaults
import com.huanli233.hibari.wear.CurvedRow
import com.huanli233.hibari.wear.DatePicker
import com.huanli233.hibari.wear.DatePickerType
import com.huanli233.hibari.wear.Dialog
import com.huanli233.hibari.wear.DialogProperties
import com.huanli233.hibari.wear.EdgeButton
import com.huanli233.hibari.wear.EdgeButtonDefaults
import com.huanli233.hibari.wear.EdgeButtonSize
import com.huanli233.hibari.wear.FilledIconButton
import com.huanli233.hibari.wear.FilledTonalButton
import com.huanli233.hibari.wear.FilledTonalIconButton
import com.huanli233.hibari.wear.FontScaleIndependent
import com.huanli233.hibari.wear.FadingExpandingLabel
import com.huanli233.hibari.wear.HorizontalPageIndicator
import com.huanli233.hibari.wear.IconButton
import com.huanli233.hibari.wear.IconButtonDefaults
import com.huanli233.hibari.wear.IconToggleButton
import com.huanli233.hibari.wear.IconToggleButtonDefaults
import com.huanli233.hibari.wear.KeepScreenOn
import com.huanli233.hibari.wear.LevelIndicator
import com.huanli233.hibari.wear.MaterialTheme
import com.huanli233.hibari.wear.MinimumInteractiveComponentSize
import com.huanli233.hibari.wear.MotionScheme
import com.huanli233.hibari.wear.OutlinedButton
import com.huanli233.hibari.wear.OutlinedCard
import com.huanli233.hibari.wear.OutlinedIconButton
import com.huanli233.hibari.wear.Picker
import com.huanli233.hibari.wear.PrimaryActionButton
import com.huanli233.hibari.wear.RevealDirection
import com.huanli233.hibari.wear.RevealValue
import com.huanli233.hibari.wear.ScreenScaffold
import com.huanli233.hibari.wear.SegmentedCircularProgressIndicator
import com.huanli233.hibari.wear.ScrollInfoProvider
import com.huanli233.hibari.wear.ScreenStage
import com.huanli233.hibari.wear.SecondaryActionButton
import com.huanli233.hibari.wear.Slider
import com.huanli233.hibari.wear.Stepper
import com.huanli233.hibari.wear.Surface
import com.huanli233.hibari.wear.SwipeToReveal
import com.huanli233.hibari.wear.Text
import com.huanli233.hibari.wear.TextButton
import com.huanli233.hibari.wear.TextButtonDefaults
import com.huanli233.hibari.wear.TextToggleButton
import com.huanli233.hibari.wear.TextButton
import com.huanli233.hibari.wear.TextToggleButtonDefaults
import com.huanli233.hibari.wear.TimePicker
import com.huanli233.hibari.wear.TimePickerSelection
import com.huanli233.hibari.wear.TimePickerType
import com.huanli233.hibari.wear.TitleCard
import com.huanli233.hibari.wear.UndoActionButton
import com.huanli233.hibari.wear.Vignette
import com.huanli233.hibari.wear.VignettePosition
import com.huanli233.hibari.wear.ambientMode
import com.huanli233.hibari.wear.currentTextStyle
import com.huanli233.hibari.wear.attributes.minimumInteractiveComponentSize
import com.huanli233.hibari.wear.currentSpToPx
import com.huanli233.hibari.wear.fontScaleIndependentTextStyle
import com.huanli233.hibari.wear.hierarchicalFocusGroup
import com.huanli233.hibari.wear.hierarchicalFocusRequester
import com.huanli233.hibari.wear.rememberActiveFocusRequester
import com.huanli233.hibari.wear.lazy.ExpandableState
import com.huanli233.hibari.wear.lazy.ScalingLazyColumn
import com.huanli233.hibari.wear.lazy.expandableItems
import com.huanli233.hibari.wear.placeholder
import com.huanli233.hibari.wear.rememberAnimatedTextFontRegistry
import com.huanli233.hibari.wear.rememberPickerState
import com.huanli233.hibari.wear.rememberPlaceholderState
import com.huanli233.hibari.wear.rememberRevealState
import com.huanli233.hibari.wear.requestFocusOnHierarchyActive
import com.huanli233.hibari.wear.scrollAway
import com.huanli233.hibari.wear.touchTargetAwareSize
import com.huanli233.hibari.wear.touchExplorationState
import java.time.LocalDate
import java.time.LocalTime

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
                // State for the sections below. Every one of these is written by a real gesture —
                // nothing here is a constant the component could never leave.
                val starred = remember { mutableStateOf(false) }
                val bolded = remember { mutableStateOf(false) }
                val morphed = remember { mutableStateOf(false) }
                val keepAwake = remember { mutableStateOf(false) }
                val focusColumn = remember { mutableStateOf(0) }
                val revealRuns = remember { mutableStateOf(0) }
                val dialogOpen = remember { mutableStateOf(false) }
                val dialogShowsDate = remember { mutableStateOf(false) }
                val picked = remember { mutableStateOf("nothing picked yet") }
                val reveal = rememberRevealState(initialValue = RevealValue.Covered)
                // AnimatedText keys its derived state on this lambda, so it is remembered: a fresh
                // lambda identity per tune would rebuild that derived state on every retune.
                val morphFraction: () -> Float = remember { { if (morphed.value) 1f else 0f } }

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
                                // TextButton sizes nothing of its own upstream, so the size and the
                                // matching text style are the caller's, which is what this pins down.
                                TextButton(
                                    onClick = { page = 0 },
                                    modifier = Modifier.size(
                                        DpSize(
                                            TextButtonDefaults.LargeButtonSize,
                                            TextButtonDefaults.DefaultButtonSize,
                                        ),
                                    ).padding(8.dp),
                                    shapes = TextButtonDefaults.animatedShapes(),
                                    colors = TextButtonDefaults.filledTonalTextButtonColors(),
                                ) {
                                    Text("Text", style = TextButtonDefaults.defaultButtonTextStyle)
                                }
                            }
                            item {
                                // The fourth one runs on animatedShapes(), so the pressed-shape
                                // morph is the thing being exercised here rather than the colour.
                                val iconSize =
                                    IconButtonDefaults.iconSizeFor(IconButtonDefaults.DefaultButtonSize)
                                Row {
                                    IconButton(onClick = { page = 0 }) { Text("i") }
                                    FilledIconButton(onClick = { page = 1 }) { Text("f") }
                                    FilledTonalIconButton(onClick = { page = 2 }) { Text("t") }
                                    OutlinedIconButton(
                                        onClick = { page = 3 },
                                        shapes = IconButtonDefaults.animatedShapes(),
                                    ) {
                                        Box(modifier = Modifier.size(DpSize(iconSize, iconSize))) {
                                            Text("o")
                                        }
                                    }
                                }
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

                            // Toggle family, each row driven off its own real `checked` state so a tap
                            // flips container colour, content colour and the checked corner in the same
                            // tune. The caller's `Modifier.size` is the point: the component applies its
                            // default size first precisely so this one wins. `variantAnimatedShapes()` is
                            // the checked-corner pair, `animatedShapes()` the press-only morph.
                            item {
                                val large = IconToggleButtonDefaults.LargeSize
                                Row {
                                    IconToggleButton(
                                        checked = starred.value,
                                        onCheckedChange = { starred.value = it },
                                        modifier = Modifier.size(DpSize(large, large)),
                                        colors = IconToggleButtonDefaults.colors(
                                            checkedContainerColor = Color(0xFF2E4374),
                                        ),
                                        shapes = IconToggleButtonDefaults.variantAnimatedShapes(),
                                        border = BorderStroke(1.dp, Color(0xFF8AB4F8)),
                                    ) {
                                        val icon = IconToggleButtonDefaults.iconSizeFor(large)
                                        Box(modifier = Modifier.size(DpSize(icon, icon))) {
                                            Text(if (starred.value) "*" else "o")
                                        }
                                    }
                                    TextToggleButton(
                                        checked = bolded.value,
                                        onCheckedChange = { bolded.value = it },
                                        shapes = TextToggleButtonDefaults.animatedShapes(),
                                    ) {
                                        Text(if (bolded.value) "Bold" else "Regular")
                                    }
                                    // `enabled = false` on a checked toggle is the disabled-checked
                                    // colour pair, which no other branch reaches.
                                    TextToggleButton(
                                        checked = true,
                                        onCheckedChange = { },
                                        enabled = false,
                                        colors = TextToggleButtonDefaults.colors(
                                            disabledCheckedContainerColor = Color(0xFF17213B),
                                        ),
                                    ) {
                                        Text("Off")
                                    }
                                }
                            }
                            // ChildButton: secondaryLabel + icon slots and the colour-overriding
                            // `childButtonColors(contentColor = …)` overload; ButtonDefaults.Height is
                            // its floor too, shared with the filled family above. Both label slots are
                            // named, not trailing: the two overloads differ only by which RowScope slot
                            // comes last, so `ChildButton(onClick) { … }` compiles against one of them
                            // silently and would leave the other unproven.
                            item {
                                ChildButton(
                                    onClick = { stepperValue -= 1 },
                                    secondaryLabel = { Text("secondary line") },
                                    icon = { Text("[i]") },
                                    colors = ButtonDefaults.childButtonColors(
                                        contentColor = Color(0xFF8AB4F8),
                                    ),
                                    label = { Text("Child label") },
                                )
                            }
                            // ChildButton's other overload: one content slot, no secondaryLabel and no
                            // icon, so the label row is centred instead of stacked.
                            item {
                                ChildButton(
                                    onClick = { stepperValue += 1 },
                                    content = { Text("Child, content only") },
                                )
                            }
                            // CompactButton: the two arrangements upstream branches on — icon + label and
                            // icon-only. The first takes the filled variant's colours, the second the
                            // *filled* button's 14/6 contentPadding instead of its own 12/0, which is the
                            // only way this row shows the padding is the caller's and not baked in.
                            item {
                                Row {
                                    CompactButton(
                                        onClick = { page = 1 },
                                        icon = { Text("->") },
                                        label = { Text("Labeled") },
                                        colors = ButtonDefaults.filledTonalButtonColors(),
                                    )
                                    CompactButton(
                                        onClick = { page = 2 },
                                        icon = {
                                            val iconOnly = CompactButtonDefaults.SmallIconSize
                                            Box(modifier = Modifier.size(DpSize(iconOnly, iconOnly))) {
                                                Text("x")
                                            }
                                        },
                                        contentPadding = ButtonDefaults.ContentPadding,
                                    )
                                }
                            }
                            // SwipeToReveal with all four action slots live, so both the action row and
                            // the undo row get built; Bidirectional + hasPartiallyRevealedState = false
                            // are the two non-default gesture switches, and the spacing is not the
                            // default 4.dp. `reveal.currentValue` in the content is what a real consumer
                            // reads, and the counters move on both the click and the full-swipe route.
                            item {
                                SwipeToReveal(
                                    primaryAction = {
                                        PrimaryActionButton(
                                            onClick = { revealRuns.value += 1 },
                                            icon = { Text("->") },
                                            text = { Text("Archive") },
                                            containerColor = Color(0xFF2E4374),
                                        )
                                    },
                                    onSwipePrimaryAction = { revealRuns.value += 1 },
                                    secondaryAction = {
                                        SecondaryActionButton(
                                            onClick = { revealRuns.value = 0 },
                                            icon = { Text("s") },
                                            contentColor = Color(0xFF8AB4F8),
                                        )
                                    },
                                    undoPrimaryAction = {
                                        UndoActionButton(
                                            onClick = { revealRuns.value -= 1 },
                                            text = { Text("Undo") },
                                            icon = { Text("<-") },
                                        )
                                    },
                                    // The fourth slot is what makes the port build *both* undo rows and
                                    // hide one by RevealState.lastActionType, so the secondary marker is
                                    // the branch only this argument switches on.
                                    undoSecondaryAction = {
                                        UndoActionButton(
                                            onClick = { revealRuns.value = 0 },
                                            text = { Text("Undo secondary") },
                                        )
                                    },
                                    revealState = reveal,
                                    revealDirection = RevealDirection.Bidirectional,
                                    hasPartiallyRevealedState = false,
                                    actionContentSpacing = 12.dp,
                                    content = {
                                        Box(
                                            modifier = Modifier
                                                .size(DpSize(140.dp, 48.dp))
                                                .padding(8.dp),
                                        ) {
                                            Text("${reveal.currentValue} / runs ${revealRuns.value}")
                                        }
                                    },
                                )
                            }
                            // AnimatedText: the morph is driven by the toggle beside it, so the
                            // fraction is a real 0f/1f pair and not a constant. Both variation axes and
                            // both font sizes are non-default, and the colour overrides the ambient one.
                            item {
                                val registry = rememberAnimatedTextFontRegistry(
                                    startFontVariationSettings = "'wght' 400",
                                    endFontVariationSettings = "'wght' 700",
                                    startFontSize = 14.sp,
                                    endFontSize = 22.sp,
                                    color = Color(0xFF8AB4F8),
                                )
                                Row {
                                    IconToggleButton(
                                        checked = morphed.value,
                                        onCheckedChange = { morphed.value = it },
                                    ) {
                                        Text("Aa")
                                    }
                                    AnimatedText(
                                        text = "Hibari",
                                        fontRegistry = registry,
                                        progressFraction = morphFraction,
                                        modifier = Modifier.size(DpSize(120.dp, 40.dp)),
                                        contentAlignment = Alignment.CenterEnd,
                                    )
                                }
                            }
                            // FadingExpandingLabel under a nested MaterialTheme(motionScheme =): the
                            // spec handed to `animationSpec` is read back through
                            // MaterialTheme.motionScheme, so the provider parameter and the getter are
                            // both on the path. `text` changes length with `page`, which is what starts
                            // the height animation, and maxLines is not the default.
                            item {
                                MaterialTheme(motionScheme = MotionScheme.expressive()) {
                                    Column {
                                        FadingExpandingLabel(
                                            text = if (page == 0) {
                                                "Short label"
                                            } else {
                                                "A much longer label that has to wrap over more than " +
                                                    "one line to fit the dial at all"
                                            },
                                            fontSize = 14.sp,
                                            maxLines = 3,
                                            textAlign = TextAlign.Center,
                                            animationSpec =
                                                MaterialTheme.motionScheme.slowEffectsSpec<Float>(),
                                        )
                                        TextButton(onClick = { page = (page + 1) % 4 }) {
                                            Text("Change the label")
                                        }
                                    }
                                }
                            }
                            // Surface: `color` has no default and sits after one, so this call is the
                            // proof the signature is usable; `contentColor` is left null to hit the
                            // contentColorFor(color) resolution, and the shape is not RectangleShape.
                            item {
                                Surface(
                                    modifier = Modifier.size(DpSize(120.dp, 48.dp)),
                                    shape = CircleShape,
                                    color = Color(0xFF17213B),
                                    border = BorderStroke(1.dp, Color(0xFF8AB4F8)),
                                ) {
                                    Text("Surface")
                                }
                            }
                            // The 48.dp touch-target trio: the constant, the px-floor modifier the
                            // constant feeds, and touchTargetAwareSize, which trades a 24.dp drawing
                            // against the same 48.dp box.
                            item {
                                val floorPx = MinimumInteractiveComponentSize.toPx(currentContext)
                                Row {
                                    Box(
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize(floorPx)
                                            .padding(4.dp),
                                    ) {
                                        Text("48 floor")
                                    }
                                    Box(modifier = Modifier.touchTargetAwareSize(24.dp)) {
                                        Text("24")
                                    }
                                }
                            }
                            // FontScaleIndependent: currentSpToPx resolves through the wrapper's
                            // fontScale = 1f density, which is the number
                            // Modifier.fontScaleIndependentTextStyle needs. The second Text is the
                            // recorded gap — TextView's own scaledDensity path the wrapper cannot reach.
                            item {
                                FontScaleIndependent {
                                    val spToPx = currentSpToPx(1f)
                                    Text(
                                        "scale free",
                                        modifier = Modifier.fontScaleIndependentTextStyle(
                                            currentTextStyle(),
                                            spToPx,
                                        ),
                                    )
                                    Text("font scaled", fontSize = 14.sp)
                                }
                            }
                            // AmbientModeHost publishes the display's mode and ambientMode() reads it
                            // back one level down; the Ambient branch is the one that only a dozing
                            // panel can take, so both flags are printed rather than assumed.
                            item {
                                AmbientModeHost {
                                    val mode = ambientMode()
                                    Text(
                                        if (mode is AmbientMode.Ambient) {
                                            "ambient, low-bit ${mode.isLowBitAmbientSupported}, " +
                                                "burn-in ${mode.isBurnInProtectionRequired}"
                                        } else {
                                            "interactive"
                                        },
                                    )
                                }
                            }
                            // KeepScreenOn enters and leaves with the toggle: the flag is added when the
                            // remembered holder lands in the tree and cleared when the branch stops
                            // rendering, so this is the lifecycle half and not just the setFlags half.
                            item {
                                Row {
                                    TextToggleButton(
                                        checked = keepAwake.value,
                                        onCheckedChange = { keepAwake.value = it },
                                        shapes = TextToggleButtonDefaults.variantAnimatedShapes(),
                                    ) {
                                        Text("Keep on")
                                    }
                                    if (keepAwake.value) {
                                        KeepScreenOn()
                                    }
                                }
                            }
                            // Hierarchical focus: two sibling groups, exactly one active, switched by
                            // the buttons. requestFocusOnHierarchyActive is the live path;
                            // rememberActiveFocusRequester + hierarchicalFocusRequester is the
                            // deprecated-but-public one, and hasFocus() reads the platform's answer.
                            item {
                                @Suppress("DEPRECATION")
                                val requester = rememberActiveFocusRequester()
                                Column {
                                    Row {
                                        TextButton(onClick = { focusColumn.value = 0 }) {
                                            Text("Col 1")
                                        }
                                        TextButton(onClick = { focusColumn.value = 1 }) {
                                            Text("Col 2")
                                        }
                                    }
                                    Row {
                                        Box(
                                            modifier = Modifier.hierarchicalFocusGroup(
                                                active = focusColumn.value == 0,
                                            ),
                                        ) {
                                            Text(
                                                "One",
                                                modifier = Modifier.requestFocusOnHierarchyActive(),
                                            )
                                        }
                                        Box(
                                            modifier = Modifier.hierarchicalFocusGroup(
                                                active = focusColumn.value == 1,
                                            ),
                                        ) {
                                            Text(
                                                "Two",
                                                modifier = Modifier.hierarchicalFocusRequester(requester),
                                            )
                                        }
                                    }
                                    Text("column ${focusColumn.value} focused: ${requester.hasFocus()}")
                                }
                            }
                            // touchExplorationState: the AccessibilityManager's live value, read as a
                            // delegate so a service change retunes this row.
                            item {
                                val exploring by touchExplorationState(currentContext)
                                Text(if (exploring) "touch exploration on" else "touch exploration off")
                            }
                            // The two pickers live in the dialog at the bottom of this screen, so these
                            // two buttons are the only way in — and `picked` is what they write back.
                            // label without icon is the third CompactButton arrangement.
                            item {
                                Column {
                                    CompactButton(
                                        onClick = {
                                            dialogShowsDate.value = false
                                            dialogOpen.value = true
                                        },
                                        label = { Text("Pick a time") },
                                    )
                                    CompactButton(
                                        onClick = {
                                            dialogShowsDate.value = true
                                            dialogOpen.value = true
                                        },
                                        label = { Text("Pick a date") },
                                    )
                                    Text(picked.value)
                                }
                            }
                            // TitleCard: every optional slot filled at once (time + subtitle + content)
                            // and `enabled = false`, which is the only state where the click attribute
                            // must drop the handler instead of greying out.
                            item {
                                TitleCard(
                                    onClick = { page = (page + 1) % 4 },
                                    title = { Text("Title slot") },
                                    time = { Text("9:30") },
                                    subtitle = { Text("Subtitle slot") },
                                    enabled = false,
                                    border = BorderStroke(2.dp, Color(0xFF8AB4F8)),
                                    contentPadding = PaddingValues(all = 6.dp),
                                    content = { Text("Body content slot") },
                                )
                            }
                            // TitleCard's non-clickable twin: no onClick, no enabled, so the container
                            // chain runs without a click attribute at all. `time` without `content` is
                            // the other layout branch (time above the title, 4.dp Spacer).
                            item {
                                TitleCard(
                                    title = { Text("No click") },
                                    time = { Text("now") },
                                    shape = CircleShape,
                                    colors = CardDefaults.outlinedCardColors(),
                                )
                            }
                            // AppCard: appImage + appName + time + title + content, i.e. both RowScope
                            // icon slots and the ColumnScope body, sized off CardDefaults.AppImageSize.
                            item {
                                val appIcon = CardDefaults.AppImageSize
                                AppCard(
                                    onClick = { page = (page + 3) % 4 },
                                    appName = { Text("Hibari") },
                                    title = { Text("AppCard title") },
                                    appImage = {
                                        Box(modifier = Modifier.size(DpSize(appIcon, appIcon))) { }
                                    },
                                    time = { Text("11:12") },
                                    content = { Text("AppCard body") },
                                )
                            }
                            // Card's uncovered slots: an explicit `minHeight` under the content's own
                            // height, a caller `shape`, and outlined colours on the filled component.
                            item {
                                Card(
                                    onClick = { stepperValue += 1 },
                                    shape = CircleShape,
                                    colors = CardDefaults.outlinedCardColors(),
                                    minHeight = 100.dp,
                                ) {
                                    Text("Card minHeight 100.dp")
                                }
                            }
                            // OutlinedCard's two resolutions that live in the body, not the signature:
                            // `border ?: CardDefaults.outlinedCardBorder()` and `shape ?: CardDefaults.shape`.
                            item {
                                OutlinedCard(
                                    enabled = false,
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                ) {
                                    Text("OutlinedCard, default resolved border")
                                }
                            }
                            // The 52.dp floor family: every variant routes through Button's
                            // `minHeight(ButtonDefaults.Height)`, and the short label below is what makes
                            // the floor the thing setting the height. contentPadding swapped for the
                            // compact pair so the horizontal inset is not the default either.
                            item {
                                Column {
                                    Button(
                                        onClick = { page = 0 },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2E4374),
                                        ),
                                        contentPadding = ButtonDefaults.CompactContentPadding,
                                    ) {
                                        Text("Filled")
                                    }
                                    FilledTonalButton(
                                        onClick = { page = 1 },
                                        enabled = false,
                                        shape = CircleShape,
                                    ) {
                                        Text("Tonal, disabled")
                                    }
                                    OutlinedButton(
                                        onClick = { page = 2 },
                                        colors = ButtonDefaults.outlinedButtonColors().copy(
                                            border = BorderStroke(2.dp, Color(0xFF8AB4F8)),
                                            disabledBorder = BorderStroke(2.dp, Color(0xFF444444)),
                                        ),
                                    ) {
                                        Text("Outlined, own border")
                                    }
                                }
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
                        // scrollAway needs the five scroll facts from whoever owns the scrolling
                        // thing, and Hibari has no factory that can produce them, so the Picker's own
                        // observable state is what feeds this: spinning it moves the offset for real,
                        // and ScreenStage.Scrolling is read from isScrollInProgress rather than being a
                        // constant. Built fresh each tune on purpose — a remembered provider would
                        // snapshot the offset once and the header would never move.
                        val scrollInfo = object : ScrollInfoProvider {
                            override val isScrollAwayValid = true
                            override val isScrollable =
                                pickerState.canScrollForward || pickerState.canScrollBackward
                            override val isScrollInProgress = pickerState.isScrollInProgress
                            override val anchorItemOffset =
                                (9.dp * pickerState.selectedOptionIndex).toPx(currentContext).toFloat()
                            override val lastItemOffset = 0f
                        }
                        Text(
                            "scrolls away",
                            modifier = Modifier
                                .gravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                                .scrollAway(scrollInfo) {
                                    if (pickerState.isScrollInProgress) {
                                        ScreenStage.Scrolling
                                    } else {
                                        ScreenStage.Idle
                                    }
                                },
                        )
                        // Last child of the screen root, because with no window there is nothing to lift
                        // this box above a sibling that comes later. dismissOnBackPress is the live half
                        // of DialogProperties here; dismissOnClickOutside is carried and inert, and the
                        // swipe-to-dismiss on the scaffold behind it still wins a right-swipe.
                        Dialog(
                            visible = dialogOpen.value,
                            onDismissRequest = { dialogOpen.value = false },
                            properties = DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = false,
                            ),
                        ) {
                            // Both pickers are java.time, so API 26 is their gate and the sample's floor.
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (dialogShowsDate.value) {
                                    DatePicker(
                                        initialDate = LocalDate.of(2026, 9, 28),
                                        onDatePicked = { date ->
                                            picked.value = "date $date"
                                            dialogOpen.value = false
                                        },
                                        minValidDate = LocalDate.of(2026, 1, 1),
                                        maxValidDate = LocalDate.of(2027, 12, 31),
                                        datePickerType = DatePickerType.MonthDayYear,
                                    )
                                } else {
                                    TimePicker(
                                        initialTime = LocalTime.of(9, 30),
                                        onTimePicked = { time ->
                                            picked.value = "time $time"
                                            dialogOpen.value = false
                                        },
                                        timePickerType = TimePickerType.HoursMinutesAmPm12H,
                                        initialSelection = TimePickerSelection.Minute,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
