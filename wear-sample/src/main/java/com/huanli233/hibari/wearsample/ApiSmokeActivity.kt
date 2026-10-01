package com.huanli233.hibari.wearsample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.huanli233.hibari.foundation.Box
import com.huanli233.hibari.foundation.Column
import com.huanli233.hibari.foundation.Node
import com.huanli233.hibari.foundation.Row
import com.huanli233.hibari.foundation.attributes.matchParentSize
import com.huanli233.hibari.foundation.attributes.padding
import com.huanli233.hibari.foundation.attributes.size
import com.huanli233.hibari.runtime.HibariView
import com.huanli233.hibari.runtime.TunationLocalProvider
import com.huanli233.hibari.runtime.currentContext
import com.huanli233.hibari.runtime.effects.rememberCoroutineScope
import com.huanli233.hibari.runtime.getValue
import com.huanli233.hibari.runtime.locals.LocalLayoutDirection
import com.huanli233.hibari.runtime.mutableStateOf
import com.huanli233.hibari.runtime.remember
import com.huanli233.hibari.runtime.setValue
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.geometry.CircleShape
import com.huanli233.hibari.ui.graphics.Color
import com.huanli233.hibari.ui.layout.Alignment
import com.huanli233.hibari.ui.layout.Arrangement
import com.huanli233.hibari.ui.text.TextAlign
import com.huanli233.hibari.ui.thenViewAttribute
import com.huanli233.hibari.ui.uniqueKey
import com.huanli233.hibari.ui.unit.DpSize
import com.huanli233.hibari.ui.unit.LayoutDirection
import com.huanli233.hibari.ui.unit.PaddingValues
import com.huanli233.hibari.ui.unit.dp
import com.huanli233.hibari.ui.unit.sp
import com.huanli233.hibari.ui.unit.toPx
import com.huanli233.hibari.ui.viewClass
import com.huanli233.hibari.wear.ActiveFocusListener
import com.huanli233.hibari.wear.AlertDialog
import com.huanli233.hibari.wear.AlertDialogContent
import com.huanli233.hibari.wear.AlertDialogDefaults
import com.huanli233.hibari.wear.AmbientMode
import com.huanli233.hibari.wear.AmbientModeHost
import com.huanli233.hibari.wear.AnimatedPage
import com.huanli233.hibari.wear.AnimatedText
import com.huanli233.hibari.wear.AppCard
import com.huanli233.hibari.wear.AppCardContent
import com.huanli233.hibari.wear.ArcProgressIndicator
import com.huanli233.hibari.wear.BorderStroke
import com.huanli233.hibari.wear.Button
import com.huanli233.hibari.wear.ButtonContent
import com.huanli233.hibari.wear.ButtonDefaults
import com.huanli233.hibari.wear.ButtonGroup
import com.huanli233.hibari.wear.Card
import com.huanli233.hibari.wear.CardDefaults
import com.huanli233.hibari.wear.CheckboxButton
import com.huanli233.hibari.wear.ChildButton
import com.huanli233.hibari.wear.CircularProgressIndicator
import com.huanli233.hibari.wear.CircularProgressIndicatorDefaults
import com.huanli233.hibari.wear.CompactButton
import com.huanli233.hibari.wear.CompactButtonContent
import com.huanli233.hibari.wear.CompactButtonDefaults
import com.huanli233.hibari.wear.ConfirmationDialog
import com.huanli233.hibari.wear.ConfirmationDialogContent
import com.huanli233.hibari.wear.ConfirmationDialogDefaults
import com.huanli233.hibari.wear.CurvedAlignment
import com.huanli233.hibari.wear.CurvedBox
import com.huanli233.hibari.wear.CurvedColumn
import com.huanli233.hibari.wear.CurvedDirection
import com.huanli233.hibari.wear.CurvedLayout
import com.huanli233.hibari.wear.CurvedRow
import com.huanli233.hibari.wear.CurvedText
import com.huanli233.hibari.wear.CustomTouchSlopProvider
import com.huanli233.hibari.wear.DatePicker
import com.huanli233.hibari.wear.DatePickerType
import com.huanli233.hibari.wear.Dialog
import com.huanli233.hibari.wear.DialogProperties
import com.huanli233.hibari.wear.EdgeButton
import com.huanli233.hibari.wear.EdgeButtonDefaults
import com.huanli233.hibari.wear.EdgeButtonSize
import com.huanli233.hibari.wear.FailureConfirmationDialog
import com.huanli233.hibari.wear.FailureConfirmationDialogContent
import com.huanli233.hibari.wear.FilledIconButton
import com.huanli233.hibari.wear.FilledTonalButton
import com.huanli233.hibari.wear.FilledTonalIconButton
import com.huanli233.hibari.wear.FixedSizeIcon
import com.huanli233.hibari.wear.FontScaleIndependent
import com.huanli233.hibari.wear.FadingExpandingLabel
import com.huanli233.hibari.wear.HierarchicalFocusRequester
import com.huanli233.hibari.wear.HorizontalPageIndicator
import com.huanli233.hibari.wear.HorizontalPager
import com.huanli233.hibari.wear.HorizontalPagerScaffold
import com.huanli233.hibari.wear.Icon
import com.huanli233.hibari.wear.IconButton
import com.huanli233.hibari.wear.IconButtonDefaults
import com.huanli233.hibari.wear.IconDefaults
import com.huanli233.hibari.wear.IconToggleButton
import com.huanli233.hibari.wear.IconToggleButtonDefaults
import com.huanli233.hibari.wear.KeepScreenOn
import com.huanli233.hibari.wear.LevelIndicator
import com.huanli233.hibari.wear.LinearProgressIndicator
import com.huanli233.hibari.wear.LinearProgressIndicatorDefaults
import com.huanli233.hibari.wear.ListSubHeader
import com.huanli233.hibari.wear.MaterialTheme
import com.huanli233.hibari.wear.MinimumInteractiveComponentSize
import com.huanli233.hibari.wear.MotionScheme
import com.huanli233.hibari.wear.OpenOnPhoneDialog
import com.huanli233.hibari.wear.OpenOnPhoneDialogContent
import com.huanli233.hibari.wear.OpenOnPhoneDialogDefaults
import com.huanli233.hibari.wear.OutlinedButton
import com.huanli233.hibari.wear.OutlinedCard
import com.huanli233.hibari.wear.OutlinedIconButton
import com.huanli233.hibari.wear.PagerScaffoldDefaults
import com.huanli233.hibari.wear.Picker
import com.huanli233.hibari.wear.PickerGroup
import com.huanli233.hibari.wear.PrimaryActionButton
import com.huanli233.hibari.wear.ProgressIndicatorDefaults
import com.huanli233.hibari.wear.ProgressSpec
import com.huanli233.hibari.wear.RadioButton
import com.huanli233.hibari.wear.RevealDirection
import com.huanli233.hibari.wear.RevealValue
import com.huanli233.hibari.wear.ScreenScaffold
import com.huanli233.hibari.wear.ScrollIndicator
import com.huanli233.hibari.wear.SegmentedCircularProgressIndicator
import com.huanli233.hibari.wear.ScrollInfoProvider
import com.huanli233.hibari.wear.ScreenStage
import com.huanli233.hibari.wear.SecondaryActionButton
import com.huanli233.hibari.wear.Slider
import com.huanli233.hibari.wear.SplitButtonGroup
import com.huanli233.hibari.wear.SplitCheckboxButton
import com.huanli233.hibari.wear.SplitRadioButton
import com.huanli233.hibari.wear.SplitSwitchButton
import com.huanli233.hibari.wear.Stepper
import com.huanli233.hibari.wear.StepperLevelIndicator
import com.huanli233.hibari.wear.SuccessConfirmationDialog
import com.huanli233.hibari.wear.SuccessConfirmationDialogContent
import com.huanli233.hibari.wear.Surface
import com.huanli233.hibari.wear.SwipeToReveal
import com.huanli233.hibari.wear.SwitchButton
import com.huanli233.hibari.wear.Text
import com.huanli233.hibari.wear.TextButton
import com.huanli233.hibari.wear.TextButtonDefaults
import com.huanli233.hibari.wear.TextSeparator
import com.huanli233.hibari.wear.TextToggleButton
import com.huanli233.hibari.wear.TextToggleButtonDefaults
import com.huanli233.hibari.wear.TimePicker
import com.huanli233.hibari.wear.TimePickerSelection
import com.huanli233.hibari.wear.TimePickerType
import com.huanli233.hibari.wear.TitleCard
import com.huanli233.hibari.wear.TitleCardContent
import com.huanli233.hibari.wear.UndoActionButton
import com.huanli233.hibari.wear.VerticalPageIndicator
import com.huanli233.hibari.wear.VerticalPager
import com.huanli233.hibari.wear.VerticalPagerScaffold
import com.huanli233.hibari.wear.Vignette
import com.huanli233.hibari.wear.VignettePosition
import com.huanli233.hibari.wear.ambientMode
import com.huanli233.hibari.wear.confirmationDialogCurvedText
import com.huanli233.hibari.wear.currentTextStyle
import com.huanli233.hibari.wear.currentTouchSlop
import com.huanli233.hibari.wear.attributes.minimumInteractiveComponentSize
import com.huanli233.hibari.wear.currentSpToPx
import com.huanli233.hibari.wear.drawCircularProgressIndicator
import com.huanli233.hibari.wear.fontScaleIndependentTextStyle
import com.huanli233.hibari.wear.hierarchicalFocusGroup
import com.huanli233.hibari.wear.hierarchicalFocusRequester
import com.huanli233.hibari.wear.openOnPhoneDialogCurvedText
import com.huanli233.hibari.wear.rememberActiveFocusRequester
import com.huanli233.hibari.wear.lazy.ExpandableState
import com.huanli233.hibari.wear.lazy.ListTransformParams
import com.huanli233.hibari.wear.lazy.ScalingLazyColumn
import com.huanli233.hibari.wear.lazy.ScalingLazyListState
import com.huanli233.hibari.wear.lazy.TransformingLazyColumn
import com.huanli233.hibari.wear.lazy.expandableItems
import com.huanli233.hibari.wear.placeholder
import com.huanli233.hibari.wear.rememberAnimatedTextFontRegistry
import com.huanli233.hibari.wear.rememberPagerState
import com.huanli233.hibari.wear.rememberPickerState
import com.huanli233.hibari.wear.rememberPlaceholderState
import com.huanli233.hibari.wear.rememberRevealState
import com.huanli233.hibari.wear.requestFocusOnHierarchyActive
import com.huanli233.hibari.wear.scrollAway
import com.huanli233.hibari.wear.touchTargetAwareSize
import com.huanli233.hibari.wear.touchExplorationState
import com.huanli233.hibari.wear.view.CurvedAnchor
import com.huanli233.hibari.wear.view.CurvedTextOverflow
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.launch

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
                // The picker family at the bottom of the list. Hoisted here rather than written in the
                // item, because `PickerGroup`'s `selectedPickerState` has to be the very same state
                // object the selected column holds — a second `rememberPickerState` call with the same
                // numbers would be a different column, and the row would centre nothing.
                // `gearState` is the one non-repeating column here, which is what makes
                // `canScrollForward`/`canScrollBackward` ever go false.
                val gearState = rememberPickerState(
                    initialNumberOfOptions = 4,
                    initiallySelectedIndex = 1,
                    shouldRepeatOptions = false,
                )
                val dayState = rememberPickerState(
                    initialNumberOfOptions = 7,
                    initiallySelectedIndex = 3,
                )
                val hourState = rememberPickerState(
                    initialNumberOfOptions = 24,
                    initiallySelectedIndex = 9,
                )
                val minuteState = rememberPickerState(
                    initialNumberOfOptions = 60,
                    initiallySelectedIndex = 30,
                )
                val secondState = rememberPickerState(
                    initialNumberOfOptions = 60,
                    initiallySelectedIndex = 45,
                )
                // The two pagers overlaid at the bottom of the screen. `pageCount` is a provider, not
                // an Int, and it is re-read on every tune, so the count may close over changing state.
                val pagerState = rememberPagerState(pageCount = { 3 })
                val verticalPagerState = rememberPagerState(pageCount = { 2 })
                // `PagerState.scrollToPage`/`animateScrollToPage` are suspend, so a consumer needs a
                // scope of its own to move a page with a button — and it does, because the crown cannot:
                // `rotaryScrollableBehavior` is not ported for pagers.
                val pagerScrollScope = rememberCoroutineScope()
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
                // Written only by the picker's `onSelected`, which is the accessibility click — on a
                // real dial that is a tap on the column, not a scroll.
                val pickerTaps = remember { mutableStateOf(0) }
                // Which column of the `PickerGroup` below is selected: the row's centring target, the
                // thing the scroll accessibility actions aim at, and the only one left editable.
                val groupColumn = remember { mutableStateOf(0) }
                val groupOwnsFocus = remember { HierarchicalFocusRequester() }
                val pagerOpen = remember { mutableStateOf(false) }
                val verticalPagerOpen = remember { mutableStateOf(false) }
                val reveal = rememberRevealState(initialValue = RevealValue.Covered)
                // AnimatedText keys its derived state on this lambda, so it is remembered: a fresh
                // lambda identity per tune would rebuild that derived state on every retune.
                val morphFraction: () -> Float = remember { { if (morphed.value) 1f else 0f } }
                // Which of the full-dial forms is on screen. -1 is nothing; every other value is opened
                // by the row with that name in the list below, and the auto-dismissing ones write -1 back
                // through their own `onDismissRequest`. One int rather than fourteen booleans because
                // these forms are mutually exclusive by nature — each is a surface over the whole dial —
                // and because it makes the list of what is covered by a click readable in one place.
                val overlayForm = remember { mutableStateOf(-1) }
                // The `*Content` twins are not surfaces of their own: each roots at `matchParentSize()`,
                // so they live in boxes the caller sizes, behind this toggle rather than always on
                // screen. Flipping it inserts and removes rows, which is also the only way to see that
                // the rows below are the Content layouts and not the dialogs.
                val contentFormsShown = remember { mutableStateOf(false) }
                // Written only by `ActiveFocusListener`, i.e. by the hierarchical focus coordinator when
                // the active path enters or leaves this subtree — not by a click.
                val focusEvents = remember { mutableStateOf(0) }
                // Which of the three split-style selection controls currently owns its group.
                val splitSelected = remember { mutableStateOf(0) }
                // `Icon`'s `image: Any?` is whatever `Modifier.image` accepts — a resource id, a
                // `Drawable`, a `Bitmap` or null (attributes/ImageAttributes.kt:18-27). This is the
                // `Drawable` branch, and it is remembered on purpose: the attribute diffs on value
                // identity, so a fresh instance per tune would re-set the image on every retune.
                val swatch = remember { ColorDrawable(0xFF8AB4F8.toInt()) }
                // The state the state-driven `ScrollIndicator` reads, and the model of the list it
                // describes — the indicator has to be told the item count, because the state does not
                // publish `totalItemsCount` (ScrollIndicator.kt:236).
                val lazyListState = remember { ScalingLazyListState() }
                val smokeRows = remember {
                    listOf(
                        "Row one", "Row two", "Row three",
                        "Row four", "Row five", "Row six", "Row seven", "Row eight",
                    )
                }
                // The names of the overlay forms, in the order `overlayForm` numbers them. Row N of the
                // list below opens form N, so this list is also the index.
                val overlayTitles = remember {
                    listOf(
                        "AlertDialog: confirm + dismiss",
                        "AlertDialog: confirm + dismiss, transformationSpec",
                        "AlertDialog: buttonless",
                        "AlertDialog: buttonless, transformationSpec",
                        "AlertDialog: edge button",
                        "AlertDialog: edge button, transformationSpec",
                        "ConfirmationDialog: curved text",
                        "ConfirmationDialog: linear text",
                        "SuccessConfirmationDialog",
                        "FailureConfirmationDialog",
                        "OpenOnPhoneDialog",
                        "CurvedLayout",
                        "CurvedBox",
                        "CurvedColumn",
                    )
                }

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
                                // Upstream's own entry shape: the indicator takes the pager's state and
                                // reads the live page and offset off it, so this follows the pager below
                                // rather than an unrelated counter.
                                HorizontalPageIndicator(pagerState = pagerState)
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
                                // The determinate ring at its defaults, which is the point: leaving
                                // `strokeWidth` unset is what runs the 8/12 dp screen-size tier of
                                // `CircularProgressIndicatorDefaults.largeStrokeWidth`
                                // (ProgressIndicator.kt:373-374, upstream CircularProgressIndicator.kt:525-526)
                                // and the gap that follows from it, `calculateRecommendedGapSize(stroke)`
                                // = stroke / 3 (`:539-544`). The value rides the slider at the top of the
                                // list: that one runs 0..6 (its `valueRange` default is
                                // `0f..(steps + 1)` with `steps = 5`, Slider.kt:144), so the last two
                                // steps push the quotient past 1 — which is what exercises the coercion
                                // and, at exactly 1, the full-circle merge path.
                                CircularProgressIndicator(
                                    progress = sliderValue / 5f,
                                    modifier = Modifier.size(DpSize(56.dp, 56.dp)),
                                )
                            }
                            item {
                                // The indeterminate overload, given no size on purpose: it takes no
                                // `enabled` (upstream reads the enabled brushes straight,
                                // CircularProgressIndicator.kt:214-221), strokes at
                                // `IndeterminateStrokeWidth` (3 dp, `:552-553`), and with nothing
                                // measuring it the view's own `onMeasure` supplies the diameter — 24 dp
                                // while indeterminate (`WearCircularProgressView:146-156`), the counterpart
                                // of upstream forcing `Modifier.size(IndeterminateCircularIndicatorDiameter)`
                                // after the caller's modifier (`:202-203`, `:549-550`). The one asymmetry
                                // is deliberate and already documented on that view: a caller that does
                                // give a size wins here, because a View cannot overrule the MeasureSpec it
                                // is handed, while upstream's late `.size(…)` always wins.
                                CircularProgressIndicator()
                            }
                            item {
                                // The linear bar at its default `StrokeWidthLarge` (12 dp,
                                // LinearProgressIndicator.kt:197): the end dot (`DotRadius` 2 dp,
                                // `DotMargin` 4 dp) inside `OuterHorizontalMargin` (2 dp) of padding is
                                // what this renders. Upstream's canvas is `fillMaxWidth()` while a View
                                // measures `wrap_content` to at most 64 px
                                // (`WearLinearProgressView.getSuggestedMinimumWidth`), so the width is
                                // given explicitly here rather than left to the parent.
                                LinearProgressIndicator(
                                    progress = sliderValue / 5f,
                                    modifier = Modifier.size(DpSize(120.dp, 12.dp)),
                                )
                            }
                            item {
                                // The same bar in a right-to-left subtree. Upstream decides this once, in
                                // the composition: `LocalLayoutDirection.current == LayoutDirection.Rtl`
                                // (`LinearProgressIndicator.kt:135`) feeding
                                // `.scale(scaleX = -1f …)` (`:143`), and the port mirrors that shape —
                                // `linearProgressIsRtl()` (`ProgressIndicator.kt:216-217`) rides the same
                                // `LocalLayoutDirection` and lands in `ProgressSpec.flipHorizontal`, which
                                // is what flips the canvas (`WearLinearProgressView:108-110`). So the RTL
                                // demo has to provide that local over the subtree rather than touch the
                                // view: writing `View.layoutDirection` here would be read by nothing,
                                // because the direction is resolved at tune time.
                                // 0.9f is picked for the other half of the dot's behaviour: at 120 dp wide
                                // and a 12 dp stroke the drawing area is 120 - 2·2 = 116 dp, so
                                // progressPx = 0.9·(116 - 12) = 93.6 and the dot's distance to the line is
                                // 110 - 2 - 93.6 - 12 = 2.4 dp, i.e. below the 4 dp `DotMargin` —
                                // scaleFraction 0.6, a 1.2 dp dot at 60 % alpha (`:126-133`, upstream
                                // `:246-250`). The slider above only ever hands 0.8 or 1.0, which straddle
                                // that window, so this item is the only place the dot's shrink-and-fade
                                // renders.
                                TunationLocalProvider(
                                    LocalLayoutDirection provides LayoutDirection.Rtl
                                ) {
                                    LinearProgressIndicator(
                                        progress = 0.9f,
                                        modifier = Modifier.size(DpSize(120.dp, 12.dp)),
                                    )
                                }
                            }
                            item {
                                // The public, non-animating draw entry (`Canvas.drawCircularProgressIndicator`,
                                // port of `DrawScope.drawCircularProgressIndicator`,
                                // CircularProgressIndicator.kt:262-322). Upstream keeps it for a caller
                                // that drives its own drawing, and nothing in this module calls it — so
                                // this item is what puts it on a screen at all, through the caller-owned
                                // view below.
                                //
                                // 1.05f is not a typo and not a headroom value: upstream's composable
                                // feeds that function `coercedProgressWithGap(progress)` — `1f +
                                // GapExtraProgress`, i.e. 1.05f — for a full circle
                                // (`:402`, `:556-564`, `GapExtraProgress` `:611`), and only at that value
                                // does the merge branch resolve `gapFraction` to 0 and close the ring
                                // (ProgressIndicator.kt:285-299). A plain 1f renders a 1-degree break,
                                // which is what upstream does too for a caller that never pushes past 1.
                                val stroke = CircularProgressIndicatorDefaults.largeStrokeWidth
                                Node(
                                    modifier = Modifier
                                        .size(DpSize(56.dp, 56.dp))
                                        .viewClass(SmokeProgressRingView::class.java)
                                        .thenViewAttribute<SmokeProgressRingView, ProgressSpec>(
                                            uniqueKey,
                                            ProgressSpec(
                                                progress = 1.05f,
                                                indeterminate = false,
                                                enabled = true,
                                                // Off, because the full-circle merge branch this item
                                                // exists to render is gated on it
                                                // (`ProgressIndicator.kt:285`).
                                                allowProgressOverflow = false,
                                                colors = ProgressIndicatorDefaults.colors(),
                                                strokeWidth = stroke,
                                                gapSize = CircularProgressIndicatorDefaults
                                                    .calculateRecommendedGapSize(stroke),
                                                startAngle = CircularProgressIndicatorDefaults.StartAngle,
                                                endAngle = CircularProgressIndicatorDefaults.StartAngle,
                                                // Circular only, and false upstream too: the circular
                                                // indicator is not direction dependent
                                                // (`ProgressIndicator.kt:115`, `:154`).
                                                flipHorizontal = false,
                                            ),
                                        ) { spec = it },
                                )
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
                            // Picker, editable and non-repeating. `shouldRepeatOptions = false` is the
                            // branch where the column stops at its ends, so `canScrollForward` and
                            // `canScrollBackward` ever go false; `onSelected` is upstream's semantics
                            // click — a tap on the column, not a scroll — and the counter under it is
                            // the only visible proof it fired. The box is the caller's, because
                            // `Picker` sizes nothing of its own.
                            item {
                                Column {
                                    Picker(
                                        state = gearState,
                                        contentDescription = { "Gear ${gearState.selectedOptionIndex}" },
                                        modifier = Modifier.size(DpSize(150.dp, 64.dp)),
                                        onSelected = { pickerTaps.value += 1 },
                                    ) { index ->
                                        Text("Gear $index")
                                    }
                                    Text("picker taps: ${pickerTaps.value}")
                                }
                            }
                            // The same component with `readOnly = true`: only the selected option shows,
                            // under the read-only shim, and `readOnlyLabel` is overlaid above it. That
                            // stacking is the thing fixed today — the label was being composited under
                            // the shim, which a compile cannot see and a dial can — so this call site
                            // exists to be looked at. `userScrollEnabled` stays at its default true,
                            // which is upstream's advice for a read-only field a tap should open, and the
                            // label is given `Gravity.TOP or Gravity.CENTER_HORIZONTAL` through the slot's own `BoxScope`
                            // because it is an overlay, not a row above the value.
                            item {
                                Picker(
                                    state = dayState,
                                    contentDescription = { "Day ${dayState.selectedOptionIndex}" },
                                    modifier = Modifier.size(DpSize(150.dp, 64.dp)),
                                    readOnly = true,
                                    readOnlyLabel = {
                                        Text("Day", modifier = Modifier.gravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL))
                                    },
                                ) { index ->
                                    Text("Day $index")
                                }
                            }
                            // PickerGroup: three columns, exactly one selected. The two unselected ones
                            // are read-only through `PickerGroupItem`'s own `readOnly = !selected`, so
                            // the second column's `readOnlyLabel` is on screen the moment this row
                            // appears and disappears when that column is tapped.
                            // `selectedPickerState` is the row's auto-centring target and the picker its
                            // ACTION_SCROLL_FORWARD/BACKWARD actions move, so it tracks `groupColumn`
                            // rather than being a fourth state. The height is the caller's: the row
                            // hands it to every column, and with one row of height there is nothing to
                            // centre and no band for the label.
                            item {
                                PickerGroup(
                                    modifier = Modifier.size(DpSize(240.dp, 120.dp)),
                                    selectedPickerState = when (groupColumn.value) {
                                        1 -> minuteState
                                        2 -> secondState
                                        else -> hourState
                                    },
                                ) {
                                    PickerGroupItem(
                                        pickerState = hourState,
                                        selected = groupColumn.value == 0,
                                        onSelected = { groupColumn.value = 0 },
                                        contentDescription = { "Hour ${hourState.selectedOptionIndex}" },
                                    ) { index, selected ->
                                        Text("$index${if (selected) "*" else ""}")
                                    }
                                    PickerGroupItem(
                                        pickerState = minuteState,
                                        selected = groupColumn.value == 1,
                                        onSelected = { groupColumn.value = 1 },
                                        contentDescription = { "Minute ${minuteState.selectedOptionIndex}" },
                                        readOnlyLabel = {
                                            Text("Min", modifier = Modifier.gravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL))
                                        },
                                    ) { index, selected ->
                                        Text("$index${if (selected) "*" else ""}")
                                    }
                                    // The third column is the `focusRequester` branch: the caller owns
                                    // this column's focus, so `PickerGroupItem` binds the requester
                                    // instead of installing `requestFocusOnHierarchyActive`.
                                    // `hasFocus()` is the platform's answer and not a state read, so the
                                    // line under the group only refreshes on the tune that moved
                                    // `groupColumn`.
                                    PickerGroupItem(
                                        pickerState = secondState,
                                        selected = groupColumn.value == 2,
                                        onSelected = { groupColumn.value = 2 },
                                        contentDescription = { "Second ${secondState.selectedOptionIndex}" },
                                        focusRequester = groupOwnsFocus,
                                    ) { index, selected ->
                                        Text("$index${if (selected) "*" else ""}")
                                    }
                                }
                            }
                            item {
                                Column {
                                    Text(
                                        "column ${groupColumn.value}, third owns focus: " +
                                            "${groupOwnsFocus.hasFocus()}"
                                    )
                                    // The way into the two pager overlays at the bottom of this screen,
                                    // which are gated because a pager takes the whole dial.
                                    CompactButton(
                                        onClick = { pagerOpen.value = true },
                                        label = { Text("Open the pager") },
                                    )
                                    CompactButton(
                                        onClick = { verticalPagerOpen.value = true },
                                        label = { Text("Open the vertical pager") },
                                    )
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
                                    // ActiveFocusListener (HierarchicalFocusCoordinator.kt:349): the
                                    // invisible node that reports when the active path enters or leaves
                                    // this subtree. Deprecated as upstream deprecates it, so the counter
                                    // under it is the only proof the callback ever ran — it is written by
                                    // the coordinator, never by a click here. The lambda's receiver is a
                                    // `CoroutineScope`, and this uses it: the write goes through `launch`,
                                    // which is the shape upstream's `:59-60` has.
                                    @Suppress("DEPRECATION")
                                    ActiveFocusListener(onFocusChanged = { focused ->
                                        launch { focusEvents.value += if (focused) 1 else -1 }
                                    })
                                    Text(
                                        "column ${focusColumn.value} focused: ${requester.hasFocus()}"
                                    )
                                    Text("focus events: ${focusEvents.value}")
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
                            // Icon and FixedSizeIcon, the two entries the module's icon story rests on
                            // (Icon.kt:31 and :65). `image: Any?` is what `Modifier.image` accepts, so the
                            // `Drawable` branch goes here and the resource-id branch is what a real app
                            // with its own drawables writes. `tint` is left null on the first so the
                            // ambient content colour resolves in the body (Icon.kt:37), and set on the
                            // second so the explicit branch runs too. Both are sized by the caller:
                            // `Icon` measures whatever the ImageView is handed, and a `ColorDrawable` has
                            // no intrinsic size at all.
                            item {
                                Row {
                                    Icon(
                                        image = swatch,
                                        contentDescription = "ambient tint, caller's size",
                                        modifier = Modifier.size(DpSize(24.dp, 24.dp)),
                                    )
                                    FixedSizeIcon(
                                        image = swatch,
                                        contentDescription = "explicit size and tint",
                                        iconSize = IconDefaults.LargeSize,
                                        modifier = Modifier.padding(4.dp),
                                        tint = Color(0xFF2E4374),
                                    )
                                }
                            }
                            // The determinate ring with every parameter the default call above leaves
                            // unresolved: `strokeWidth` and `gapSize` are `Dp?` precisely because
                            // upstream's defaults read the theme and the parameter below it reads the
                            // stroke (`ProgressIndicator.kt:126, :137-138`), so naming both here is the
                            // only way to see that a caller can. `allowProgressOverflow = true` with the
                            // slider pushed past 1 is the wrap branch, which no other call in this file
                            // reaches (the ring above runs the coercion instead), and `enabled = false`
                            // on the half the row picks the disabled brushes.
                            item {
                                Row {
                                    CircularProgressIndicator(
                                        progress = sliderValue / 5f,
                                        modifier = Modifier.size(DpSize(56.dp, 56.dp)),
                                        enabled = stepperValue % 2 == 0,
                                        allowProgressOverflow = true,
                                        strokeWidth = 10.dp,
                                        gapSize = 4.dp,
                                    )
                                    CircularProgressIndicator(
                                        progress = sliderValue / 5f,
                                        modifier = Modifier.size(DpSize(56.dp, 56.dp)),
                                        startAngle = 90f,
                                        endAngle = 270f,
                                        strokeWidth = CircularProgressIndicatorDefaults.largeStrokeWidth,
                                        gapSize = CircularProgressIndicatorDefaults
                                            .calculateRecommendedGapSize(
                                                CircularProgressIndicatorDefaults.largeStrokeWidth,
                                            ),
                                    )
                                }
                            }
                            // The linear bar at `StrokeWidthSmall` — 8.dp, exactly the floor its `require`
                            // enforces (ProgressIndicator.kt:226), and therefore the boundary a caller has
                            // to know about: below it the call throws rather than clamping. The box height
                            // follows the stroke, since the bar draws centred in whatever it is measured.
                            item {
                                LinearProgressIndicator(
                                    progress = sliderValue / 5f,
                                    modifier = Modifier.size(DpSize(120.dp, 8.dp)),
                                    strokeWidth = LinearProgressIndicatorDefaults.StrokeWidthSmall,
                                    enabled = true,
                                )
                            }
                            // Both `StepperLevelIndicator` overloads (LevelIndicator.kt:103 and :133). The
                            // `Float` one maps `value` out of `valueRange`, which is why the range is the
                            // slider's real 0f..6f and not the 0f..1f default — the default would make
                            // this call indistinguishable from a plain `LevelIndicator`. The `Int` one
                            // takes an `IntProgression` and is fed the same `1..10` the `Stepper` at the
                            // top of this list runs on, so the two move together.
                            item {
                                Row {
                                    StepperLevelIndicator(
                                        value = sliderValue,
                                        valueRange = 0f..6f,
                                        strokeWidth = 4.dp,
                                        sweepAngle = 270f,
                                        reverseDirection = true,
                                    )
                                    StepperLevelIndicator(
                                        value = stepperValue,
                                        valueProgression = 1..10,
                                        modifier = Modifier.padding(8.dp),
                                    )
                                }
                            }
                            // SplitButtonGroup, the connected-corner sibling of the `ButtonGroup` further
                            // up (ButtonGroup.kt:141). The group's own scope is what is exercised here:
                            // `weight`, `minWidth` and `animateWidth` are only reachable from inside the
                            // content lambda, and two children with different weights is the case the
                            // width solve exists for. `contentPadding` is nullable and resolved by
                            // `ButtonGroup` in its body (ButtonGroup.kt:152), so a caller's value is the
                            // only way to replace the full-width default paddings.
                            item {
                                SplitButtonGroup(
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                ) {
                                    CompactButton(
                                        onClick = { page = 0 },
                                        modifier = Modifier.weight(2f).animateWidth(),
                                        label = { Text("Wide") },
                                    )
                                    CompactButton(
                                        onClick = { page = 1 },
                                        modifier = Modifier.minWidth(56.dp).animateWidth(),
                                        label = { Text("Min") },
                                    )
                                }
                            }
                            // The five button-shaped selection controls. The three rows
                            // (`CheckboxButton` CheckboxButton.kt:114, `SwitchButton` SwitchButton.kt:89,
                            // `RadioButton` RadioButton.kt:92) all end in a required `label` and share the
                            // same optional `icon`/`secondaryLabel` pair; the three `Split*` ones each add a
                            // required content description and a required `onContainerClick`, which is the
                            // whole point of the split shape — one control, two tappable halves — and the
                            // only part of their signatures a caller cannot leave at a default. Each rides
                            // state that something else on this screen also reads or writes, so the visible
                            // half of the flip is real.
                            //
                            // `RadioButton` is spelled with a `label` on purpose. Two public `RadioButton`s
                            // exist in this package after the bare-control rework — this row one and
                            // `SelectionControls.kt:204`, whose listener is now `onClick`, not `onSelect` —
                            // so the bare one is not a candidate for a call that names `onSelect`, and a
                            // call that names `label` is not a candidate for the row's sibling. That is the
                            // only thing keeping this pair unambiguous from a caller.
                            item {
                                Column {
                                    CheckboxButton(
                                        checked = starred.value,
                                        onCheckedChange = { starred.value = it },
                                        icon = {
                                            Box(modifier = Modifier.size(DpSize(24.dp, 24.dp))) { Text("#") }
                                        },
                                        secondaryLabel = { Text("with an icon slot") },
                                        label = { Text("CheckboxButton") },
                                    )
                                    SwitchButton(
                                        checked = keepAwake.value,
                                        onCheckedChange = { keepAwake.value = it },
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        secondaryLabel = { Text("drives KeepScreenOn above") },
                                        label = { Text("SwitchButton") },
                                    )
                                    RadioButton(
                                        selected = splitSelected.value == 0,
                                        onSelect = { splitSelected.value = 0 },
                                        enabled = stepperValue % 3 != 0,
                                        label = { Text("RadioButton row") },
                                    )
                                    SplitCheckboxButton(
                                        checked = bolded.value,
                                        onCheckedChange = { bolded.value = it },
                                        toggleContentDescription = "toggle the checkbox",
                                        onContainerClick = { picked.value = "checkbox container clicked" },
                                        secondaryLabel = { Text("two halves") },
                                        label = { Text("SplitCheckboxButton") },
                                    )
                                    SplitRadioButton(
                                        selected = splitSelected.value == 0,
                                        onSelectionClick = { splitSelected.value = 0 },
                                        selectionContentDescription = "select the first option",
                                        onContainerClick = { splitSelected.value = 1 },
                                        containerClickLabel = "or the container",
                                        secondaryLabel = { Text("selection and container") },
                                        label = { Text("SplitRadioButton") },
                                    )
                                    SplitSwitchButton(
                                        checked = keepAwake.value,
                                        onCheckedChange = { keepAwake.value = it },
                                        toggleContentDescription = "toggle the switch",
                                        onContainerClick = { morphed.value = !morphed.value },
                                        label = { Text("SplitSwitchButton, keep-awake state") },
                                    )
                                }
                            }
                            // ListSubHeader (ListHeader.kt:86) rather than ListHeader: it is the sibling
                            // with the extra `icon` slot and the 6.dp gap the component puts between the
                            // two slots, and it is the one this file never called.
                            item {
                                ListSubHeader(
                                    backgroundColor = Color(0xFF17213B),
                                    icon = { Text("[i]") },
                                    label = { Text("Sub-header") },
                                )
                            }
                            // TextSeparator (TimeText.kt:241) — the glyph `TimeText` puts between hours and
                            // minutes, on its own. `textStyle` stays unset on purpose: it is `TextStyle?`
                            // and its default resolves in the body to `arcMedium`, because the real
                            // default is a `@Tunable` theme read a hoisted default cannot make
                            // (TimeText.kt:235-239). A caller therefore cannot pass a styled object and
                            // expect its colour to ride along — see the KDoc on that parameter.
                            item {
                                Row {
                                    Text("09")
                                    TextSeparator(contentPadding = PaddingValues(horizontal = 2.dp))
                                    Text("30")
                                }
                            }
                            // The two `*Content` entries of the button family, each inside a container of
                            // the caller's own making: that is what they exist for (CompactButton.kt:136,
                            // Button.kt:170). `CompactButtonContent` takes no required parameter at all,
                            // so an argument-less call is legal and renders nothing — the label and icon
                            // slots are what this row pins down.
                            item {
                                Surface(
                                    modifier = Modifier.size(DpSize(200.dp, 48.dp)),
                                    shape = CircleShape,
                                    color = Color(0xFF2E4374),
                                ) {
                                    CompactButtonContent(
                                        icon = {
                                            Box(modifier = Modifier.size(DpSize(24.dp, 24.dp))) { Text("->") }
                                        },
                                        label = { Text("Compact, own container") },
                                    )
                                }
                            }
                            item {
                                Surface(
                                    modifier = Modifier.size(DpSize(200.dp, 64.dp)),
                                    color = Color(0xFF17213B),
                                    border = BorderStroke(1.dp, Color(0xFF8AB4F8)),
                                ) {
                                    ButtonContent(
                                        secondaryLabel = { Text("second line") },
                                        icon = {
                                            Box(modifier = Modifier.size(DpSize(24.dp, 24.dp))) { Text("[i]") }
                                        },
                                        label = { Text("ButtonContent") },
                                    )
                                }
                            }
                            // CurvedText as a standalone component (CurvedText.kt:68) rather than the
                            // `curvedText` member of a curved container, which this file already uses
                            // inside `CurvedRow`. It is a `WearCurvedTextView` node, so unlike the three
                            // curved containers below it does honour the caller's `modifier`, and the
                            // 180 x 60.dp box is what gives the arc a chord to bend over.
                            item {
                                CurvedText(
                                    text = "Standalone CurvedText",
                                    modifier = Modifier.size(DpSize(180.dp, 60.dp)),
                                    color = Color(0xFF8AB4F8),
                                    fontSize = 14.sp,
                                    clockwise = false,
                                    maxSweepAngle = 120f,
                                    overflow = CurvedTextOverflow.Ellipsis,
                                )
                            }
                            // TransformingLazyColumn and the `ScalingLazyListState` overload of
                            // ScrollIndicator, paired in one box because the indicator is only honest
                            // about a list it can name: it takes `itemCount` as a parameter, since the
                            // state publishes no `totalItemsCount` (ScrollIndicator.kt:236, :245-253), so
                            // `smokeRows.size` has to be the very count this list is built from. The
                            // shared `lazyListState` is what makes the thumb move with the list rather
                            // than beside it — and, per that KDoc, it only moves when something re-tunes,
                            // which here is the row's own scroll.
                            item {
                                Box(modifier = Modifier.size(DpSize(220.dp, 140.dp))) {
                                    TransformingLazyColumn(
                                        state = lazyListState,
                                        contentPadding = PaddingValues(vertical = 8.dp),
                                        transformParams = ListTransformParams(
                                            edgeScale = 0.85f,
                                            edgeAlpha = 0.7f,
                                            minTransitionArea = 0.4f,
                                        ),
                                    ) {
                                        items(smokeRows) { row ->
                                            Text(row)
                                        }
                                    }
                                    ScrollIndicator(
                                        state = lazyListState,
                                        itemCount = smokeRows.size,
                                        modifier = Modifier.gravity(Gravity.END),
                                        visibleItemCount = 3,
                                        reverseDirection = false,
                                    )
                                }
                            }
                            // CustomTouchSlopProvider (CustomTouchSlopProvider.kt:57) around the slider:
                            // upstream's own uses are always a multiplier around the current value, which
                            // is what `currentTouchSlop` is for — the read side, resolving `LocalTouchSlop`
                            // or the platform `ViewConfiguration` (that file, :73). 1.20x is the reveal's
                            // number (`material3/SwipeToReveal.kt:318`), so this is the shape a host with
                            // its own gesture code writes, not an invented one.
                            item {
                                CustomTouchSlopProvider(
                                    newTouchSlop = currentTouchSlop(currentContext) * 1.20f,
                                ) {
                                    Slider(
                                        value = sliderValue,
                                        onValueChange = { sliderValue = it },
                                        steps = 5,
                                    )
                                }
                            }
                            // VerticalPageIndicator (PageIndicator.kt:120) against the vertical pager's
                            // state, with all three colours named because each is `Color? = null` and
                            // resolves through `PageIndicatorDefaults` in the body (:127-129).
                            item {
                                VerticalPageIndicator(
                                    pagerState = verticalPagerState,
                                    selectedColor = Color(0xFF8AB4F8),
                                    unselectedColor = Color(0xFF444444),
                                    backgroundColor = Color(0xFF17213B),
                                )
                            }
                            // One row per form that needs the whole dial. `pos` is the overload of the
                            // list scope that hands the body an index, which is what lets one lambda open
                            // fourteen different entry points by number.
                            item {
                                Text("Full-dial forms")
                            }
                            pos(count = overlayTitles.size) { index ->
                                CompactButton(
                                    onClick = { overlayForm.value = index },
                                    label = { Text(overlayTitles[index]) },
                                )
                            }
                            item {
                                TextToggleButton(
                                    checked = contentFormsShown.value,
                                    onCheckedChange = { contentFormsShown.value = it },
                                ) {
                                    Text("Show the Content twins")
                                }
                            }
                            // The ten `*Content` twins of the dialog and card family: the same layouts
                            // with no window, no timer and no haptic, which is the half of the port a
                            // caller drives itself. Every one roots at `matchParentSize()`
                            // (AlertDialog.kt:902-906, ConfirmationDialog.kt:247/:614), so each needs a
                            // box with bounds of its own — inside a list row, that box is the caller's.
                            //
                            // The eight `curvedText` slots in this file — four in this block, four in the
                            // dialog overlays below — take a plain function type, and that is upstream's
                            // shape rather than a port gap: `material3/ConfirmationDialog.kt:592` is
                            // `public fun CurvedScope.confirmationDialogCurvedText(text: String,
                            // style: CurvedTextStyle)` with no `@Composable` on it and `style` required,
                            // and `CurvedLayout.kt:90-96` here declares `content: CurvedLayoutScope.() -> Unit`
                            // plain. So the helpers are legal inside the slot and a theme read is not: the
                            // tuner reaches a `@Tunable` call as a parameter its enclosing lambda owns, and
                            // a nested non-inline lambda has none
                            // (hibari-compiler/.../TunerParamTransformer.kt:626-647) — which the K2 checker
                            // will not tell you about, because it keeps walking outward and stops at the
                            // host `@Tunable` body (`TunableCallChecker.kt:113-121`). Hence the two-step
                            // below, and it is the only correct call shape: resolve
                            // `ConfirmationDialogDefaults.curvedTextStyle` / `OpenOnPhoneDialogDefaults.curvedTextStyle`
                            // (and `.text`) in the caller's own `@Tunable` body and pass the finished value
                            // in. Anything that read a theme from inside one of these slots compiled, ran
                            // without a tuner, and is being corrected in hibari-wear by the file's owner.
                            if (contentFormsShown.value) {
                                item {
                                    Box(modifier = Modifier.size(DpSize(220.dp, 170.dp))) {
                                        AlertDialogContent(
                                            confirmButton = {
                                                AlertDialogDefaults.ConfirmButton(
                                                    onClick = { picked.value = "confirmed" },
                                                    content = { Text("ok") },
                                                )
                                            },
                                            title = { Text("Content: confirm and dismiss") },
                                            dismissButton = {
                                                AlertDialogDefaults.DismissButton(
                                                    onClick = { picked.value = "dismissed" },
                                                    content = { Text("no") },
                                                )
                                            },
                                            icon = {
                                                FixedSizeIcon(
                                                    image = swatch,
                                                    contentDescription = "dialog icon",
                                                    iconSize = AlertDialogDefaults.IconSize,
                                                )
                                            },
                                            text = { Text("A message under the title.") },
                                            verticalArrangement = Arrangement.spacedBy(
                                                space = 6.dp,
                                                alignment = Alignment.CenterVertically,
                                            ),
                                        )
                                    }
                                }
                                // The same content with a `transformationSpec`, and with `content` — which
                                // is what makes the spec mean anything: `alertDialogSpecifiedContent`
                                // forwards to the fixed layout when `content` is null
                                // (AlertDialog.kt:1011-1026), so the spec only reaches a real list here.
                                item {
                                    Box(modifier = Modifier.size(DpSize(220.dp, 170.dp))) {
                                        AlertDialogContent(
                                            confirmButton = {
                                                AlertDialogDefaults.ConfirmButton(
                                                    onClick = { picked.value = "confirmed" },
                                                )
                                            },
                                            title = { Text("Content: listed, own spec") },
                                            dismissButton = {
                                                AlertDialogDefaults.DismissButton(
                                                    onClick = { picked.value = "dismissed" },
                                                )
                                            },
                                            transformationSpec = AlertDialogDefaults.AlertScalingParams,
                                            content = {
                                                item(key = "alert-c-1") { Text("Listed row one") }
                                                item(key = "alert-c-2") { Text("Listed row two") }
                                            },
                                        )
                                    }
                                }
                                item {
                                    Box(modifier = Modifier.size(DpSize(220.dp, 120.dp))) {
                                        AlertDialogContent(
                                            title = { Text("Content: buttonless") },
                                            text = { Text("No buttons at all.") },
                                            contentPadding = PaddingValues(all = 10.dp),
                                            content = {
                                                item(key = "alert-c-3") { Text("The caller seeks input here") }
                                            },
                                        )
                                    }
                                }
                                // The pair where upstream's `contentPadding` is a *function* of
                                // `isScrollable` (AlertDialog.kt:725-734). That is the one alert signature
                                // a caller can only satisfy with a lambda, and `content != null` is what
                                // the body feeds it (that overload, :735).
                                item {
                                    Box(modifier = Modifier.size(DpSize(220.dp, 150.dp))) {
                                        AlertDialogContent(
                                            title = { Text("Content: buttonless, own spec") },
                                            transformationSpec = ListTransformParams(edgeScale = 0.9f),
                                            icon = {
                                                FixedSizeIcon(
                                                    image = swatch,
                                                    contentDescription = "dialog icon",
                                                    iconSize = AlertDialogDefaults.IconSize,
                                                )
                                            },
                                            contentPadding = { isScrollable ->
                                                PaddingValues(all = if (isScrollable) 8.dp else 16.dp)
                                            },
                                            content = {
                                                item(key = "alert-c-4") { Text("Scrollable, so 8.dp") }
                                            },
                                        )
                                    }
                                }
                                item {
                                    Box(modifier = Modifier.size(DpSize(220.dp, 150.dp))) {
                                        AlertDialogContent(
                                            edgeButton = {
                                                AlertDialogDefaults.EdgeButton(
                                                    onClick = { picked.value = "edge button" },
                                                    content = { Text("Got it") },
                                                )
                                            },
                                            title = { Text("Content: edge button") },
                                            text = { Text("One-way acknowledgement.") },
                                        )
                                    }
                                }
                                item {
                                    Box(modifier = Modifier.size(DpSize(220.dp, 150.dp))) {
                                        AlertDialogContent(
                                            edgeButton = {
                                                AlertDialogDefaults.EdgeButton(
                                                    onClick = { picked.value = "edge button" },
                                                )
                                            },
                                            title = { Text("Content: edge button, own spec") },
                                            transformationSpec = ListTransformParams(),
                                            content = {
                                                item(key = "alert-c-5") { Text("Row above the edge button") }
                                            },
                                        )
                                    }
                                }
                                // The two `ConfirmationDialogContent` overloads — the curved label and the
                                // linear text — which are the same slot pair the dialog above them
                                // branches on, minus the dismiss timer.
                                item {
                                    val curvedLabelStyle = ConfirmationDialogDefaults.curvedTextStyle()
                                    Box(modifier = Modifier.size(DpSize(220.dp, 180.dp))) {
                                        ConfirmationDialogContent(
                                            curvedText = {
                                                confirmationDialogCurvedText(
                                                    text = "Curved",
                                                    style = curvedLabelStyle,
                                                )
                                            },
                                            content = { Text("*") },
                                        )
                                    }
                                }
                                item {
                                    Box(modifier = Modifier.size(DpSize(220.dp, 180.dp))) {
                                        ConfirmationDialogContent(
                                            text = { Text("Linear text") },
                                            modifier = Modifier.padding(8.dp),
                                            content = { Text("o") },
                                        )
                                    }
                                }
                                // The success and failure twins. Two things differ from the plain pair
                                // above: they have no `text`-slot overload, and their `content` is neither
                                // required nor nullable — it carries upstream's parameter default verbatim,
                                // `= { ConfirmationDialogDefaults.SuccessIcon() }`
                                // (ConfirmationDialog.kt:355) and `= { ...ConnectionFailureIcon() }` (:439),
                                // where the plain `ConfirmationDialogContent` still requires the slot
                                // (:213, :242). The failure box below therefore omits `content` entirely,
                                // which is what actually runs the default-artwork path.
                                item {
                                    val curvedLabelStyle = ConfirmationDialogDefaults.curvedTextStyle()
                                    Box(modifier = Modifier.size(DpSize(220.dp, 180.dp))) {
                                        SuccessConfirmationDialogContent(
                                            curvedText = {
                                                confirmationDialogCurvedText(
                                                    text = "Saved",
                                                    style = curvedLabelStyle,
                                                )
                                            },
                                            content = { Text(":)") },
                                        )
                                    }
                                }
                                item {
                                    val curvedLabelStyle = ConfirmationDialogDefaults.curvedTextStyle()
                                    Box(modifier = Modifier.size(DpSize(220.dp, 180.dp))) {
                                        FailureConfirmationDialogContent(
                                            curvedText = {
                                                confirmationDialogCurvedText(
                                                    text = "Nope",
                                                    style = curvedLabelStyle,
                                                )
                                            },
                                        )
                                    }
                                }
                                // OpenOnPhoneDialogContent: the one content entry whose `durationMillis`
                                // has no default either, because the dialog hands it down rather than
                                // letting the ring read one (`OpenOnPhoneDialog.kt:168-174`). `content`
                                // is where upstream's default icon goes — a null `content` at this entry
                                // point draws no icon at all, and `OpenOnPhoneDialogDefaults.Icon` is the
                                // artwork, not a default this caller can inherit.
                                item {
                                    // Both of this label's two arguments are `@Tunable` reads upstream
                                    // would have made in the slot itself (`material3`'s own default is
                                    // `{ OpenOnPhoneDialogDefaults.text }` fed to a theme-styled label), so
                                    // both are resolved here and passed in.
                                    val openOnPhoneLabel = OpenOnPhoneDialogDefaults.text
                                    val curvedLabelStyle = OpenOnPhoneDialogDefaults.curvedTextStyle()
                                    Box(modifier = Modifier.size(DpSize(220.dp, 180.dp))) {
                                        OpenOnPhoneDialogContent(
                                            curvedText = {
                                                openOnPhoneDialogCurvedText(
                                                    text = openOnPhoneLabel,
                                                    style = curvedLabelStyle,
                                                )
                                            },
                                            durationMillis = OpenOnPhoneDialogDefaults.DurationMillis,
                                            content = { OpenOnPhoneDialogDefaults.Icon() },
                                        )
                                    }
                                }
                                // The card `*Content` twins, inside `Card`'s generic content slot, which is
                                // the placement their own KDoc asks for (TitleCard.kt:187, AppCard.kt:182).
                                item {
                                    Card(
                                        modifier = Modifier.size(DpSize(220.dp, 150.dp)),
                                        minHeight = 120.dp,
                                    ) {
                                        TitleCardContent(
                                            title = { Text("TitleCardContent") },
                                            time = { Text("9:30") },
                                            subtitle = { Text("subtitle") },
                                            content = { Text("body") },
                                        )
                                    }
                                }
                                item {
                                    Card(
                                        modifier = Modifier.size(DpSize(220.dp, 150.dp)),
                                        colors = CardDefaults.cardColors(),
                                    ) {
                                        val appIcon = CardDefaults.AppImageSize
                                        AppCardContent(
                                            appName = { Text("Hibari") },
                                            title = { Text("AppCardContent") },
                                            appImage = {
                                                Box(modifier = Modifier.size(DpSize(appIcon, appIcon))) { }
                                            },
                                            time = { Text("11:12") },
                                            content = { Text("body") },
                                        )
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
                        // PagerScaffold + HorizontalPager + AnimatedPage, gated and entered from the two
                        // buttons in the list above. It has to be an overlay: `PagerImpl` and
                        // `PagerScaffoldImpl` both apply `matchParentSize()` *after* the caller's
                        // modifier, so a pager cannot be sized down or listed — which makes this the
                        // same one-boolean shape as the dialog below, and for the same reason.
                        // `pageIndicator` is deliberately left at its default, because the default is
                        // the interesting half: it is upstream's `{ HorizontalPageIndicator(pagerState) }`
                        // (`PagerScaffold.kt:80`), and the indicator binds the pager's own scroll channel
                        // on itself, the way upstream reads the page and offset inside its draw pass.
                        // `pageIndicatorAnimationSpec` is the one
                        // non-default — with it the dots are hidden while the pager is settled and show
                        // only during a page turn.
                        // `AnimatedPage` is the port's `pageTransform`: scale 1 -> 0.55 around the far
                        // edge plus a half-alpha scrim, clipped to a circle. The crown does not reach a
                        // pager at all (upstream's `rotaryScrollableBehavior` is not ported), so
                        // "Next page" is the programmatic half of this state rather than a convenience:
                        // `animateScrollToPage` is suspend, which is what a consumer needs the scope for.
                        if (pagerOpen.value) {
                            HorizontalPagerScaffold(
                                pagerState = pagerState,
                                pageIndicatorAnimationSpec = PagerScaffoldDefaults.FadeOutAnimationSpec,
                            ) {
                                HorizontalPager(state = pagerState) { pagerPage ->
                                    AnimatedPage(pageIndex = pagerPage, pagerState = pagerState) {
                                        Column {
                                            Text("Page ${pagerPage + 1} of ${pagerState.pageCount}")
                                            TextButton(
                                                onClick = {
                                                    pagerScrollScope.launch {
                                                        pagerState.animateScrollToPage(
                                                            (pagerPage + 1) % pagerState.pageCount,
                                                        )
                                                    }
                                                },
                                            ) {
                                                Text("Next page")
                                            }
                                            TextButton(onClick = { pagerOpen.value = false }) {
                                                Text("Back to the list")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // The vertical sibling, and the two differences upstream has: `VerticalPager`
                        // takes no gesture-inclusion parameter at all, and the scaffold's default
                        // indicator is the *end*-aligned `VerticalPageIndicator` rather than the
                        // bottom-centre one. No `AnimatedPage` here, so the two overlays are not the
                        // same screenshot, and `scrollToPage` is the instant twin of the animated call
                        // above.
                        if (verticalPagerOpen.value) {
                            VerticalPagerScaffold(pagerState = verticalPagerState) {
                                VerticalPager(state = verticalPagerState) { pagerPage ->
                                    Column {
                                        Text("Vertical page ${pagerPage + 1}")
                                        TextButton(
                                            onClick = {
                                                pagerScrollScope.launch {
                                                    verticalPagerState.scrollToPage(
                                                        (pagerPage + 1) % verticalPagerState.pageCount,
                                                    )
                                                }
                                            },
                                        ) {
                                            Text("Scroll to the next page")
                                        }
                                        TextButton(onClick = { verticalPagerOpen.value = false }) {
                                            Text("Back to the list")
                                        }
                                    }
                                }
                            }
                        }
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
                        // The fraction overload of ScrollIndicator, on the bezel circle the component is
                        // documented for (ScrollIndicator.kt:168-169) rather than beside a list: it sizes
                        // itself from `screenWidthDp` inside the body (:285-292), so the caller's modifier
                        // only places it. The slider drives it for real, and it is here rather than in the
                        // list because a curve over the dial is the only place its shape reads correctly.
                        ScrollIndicator(
                            positionFraction = sliderValue / 6f,
                            sizeFraction = 0.3f,
                            modifier = Modifier.gravity(Gravity.CENTER_VERTICAL),
                            reverseDirection = stepperValue % 2 == 0,
                        )
                        // The six `AlertDialog` overloads (AlertDialog.kt:279, :338, :392, :447, :514,
                        // :563), each gated by its own `visible` — which is what upstream's `show` does at
                        // this entry point, and the reason all six can sit in the tree at once: an
                        // invisible one emits nothing (that file, :252-254). All six are written from the
                        // signatures, not from memory, and each one names something the others leave at
                        // its default, so the six really are six call shapes.
                        //
                        // The three that take a `transformationSpec` also pass `content`, and that is not
                        // decoration: `alertDialogSpecifiedContent` forwards to the fixed layout when
                        // `content` is null (:1011-1026), so the spec reaches a real list only with it.
                        AlertDialog(
                            visible = overlayForm.value == 0,
                            onDismissRequest = { overlayForm.value = -1 },
                            confirmButton = {
                                AlertDialogDefaults.ConfirmButton(
                                    onClick = {
                                        picked.value = "confirmed on form 0"
                                        overlayForm.value = -1
                                    },
                                    content = { Text("ok") },
                                )
                            },
                            title = { Text("Confirm or dismiss") },
                            modifier = Modifier.padding(8.dp),
                            dismissButton = {
                                AlertDialogDefaults.DismissButton(
                                    onClick = {
                                        picked.value = "dismissed on form 0"
                                        overlayForm.value = -1
                                    },
                                    content = { Text("no") },
                                )
                            },
                            icon = {
                                FixedSizeIcon(
                                    image = swatch,
                                    contentDescription = "dialog icon",
                                    iconSize = AlertDialogDefaults.IconSize,
                                )
                            },
                            text = { Text("The confirm/dismiss overload, with both buttons.") },
                            properties = DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = false,
                            ),
                        )
                        AlertDialog(
                            visible = overlayForm.value == 1,
                            onDismissRequest = { overlayForm.value = -1 },
                            confirmButton = {
                                AlertDialogDefaults.ConfirmButton(
                                    onClick = {
                                        picked.value = "confirmed on form 1"
                                        overlayForm.value = -1
                                    },
                                )
                            },
                            title = { Text("Listed with the caller's spec") },
                            transformationSpec = ListTransformParams(
                                edgeScale = 0.9f,
                                edgeAlpha = 0.8f,
                            ),
                            dismissButton = {
                                AlertDialogDefaults.DismissButton(
                                    onClick = { overlayForm.value = -1 },
                                )
                            },
                            content = {
                                item(key = "alert-1-a") { Text("A listed row") }
                                item(key = "alert-1-b") { Text("Another listed row") }
                            },
                        )
                        AlertDialog(
                            visible = overlayForm.value == 2,
                            onDismissRequest = { overlayForm.value = -1 },
                            title = { Text("Buttonless") },
                            text = { Text("The caller seeks input through content.") },
                            contentPadding = PaddingValues(all = 12.dp),
                            content = {
                                item(key = "alert-2") {
                                    Slider(
                                        value = sliderValue,
                                        onValueChange = { sliderValue = it },
                                        steps = 5,
                                    )
                                }
                            },
                        )
                        // The overload whose `contentPadding` is a function of `isScrollable`
                        // (AlertDialog.kt:447-459). It is the only alert parameter that cannot be answered
                        // with a value, and the Boolean it receives is `content != null` (:460).
                        AlertDialog(
                            visible = overlayForm.value == 3,
                            onDismissRequest = { overlayForm.value = -1 },
                            title = { Text("Buttonless, listed, own spec") },
                            transformationSpec = AlertDialogDefaults.AlertScalingParams,
                            icon = {
                                FixedSizeIcon(
                                    image = swatch,
                                    contentDescription = "dialog icon",
                                    iconSize = AlertDialogDefaults.IconSize,
                                )
                            },
                            verticalArrangement = Arrangement.spacedBy(
                                space = 6.dp,
                                alignment = Alignment.CenterVertically,
                            ),
                            contentPadding = { isScrollable ->
                                PaddingValues(all = if (isScrollable) 8.dp else 20.dp)
                            },
                            properties = DialogProperties(dismissOnBackPress = false),
                            content = {
                                item(key = "alert-3") { Text("Scrollable, so 8.dp of padding") }
                            },
                        )
                        AlertDialog(
                            visible = overlayForm.value == 4,
                            onDismissRequest = { overlayForm.value = -1 },
                            edgeButton = {
                                AlertDialogDefaults.EdgeButton(
                                    onClick = {
                                        picked.value = "edge button on form 4"
                                        overlayForm.value = -1
                                    },
                                    content = { Text("Got it") },
                                )
                            },
                            title = { Text("One-way acknowledgement") },
                            text = { Text("A single edge button at the bottom edge.") },
                        )
                        AlertDialog(
                            visible = overlayForm.value == 5,
                            onDismissRequest = { overlayForm.value = -1 },
                            edgeButton = {
                                AlertDialogDefaults.EdgeButton(
                                    onClick = { overlayForm.value = -1 },
                                )
                            },
                            title = { Text("Edge button, listed, own spec") },
                            transformationSpec = ListTransformParams(reduceMotion = true),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            content = {
                                item(key = "alert-5") { Text("Row above the edge button") }
                            },
                        )
                        // The two `ConfirmationDialog` overloads (ConfirmationDialog.kt:166 and :189), the
                        // curved label and the linear text. Both take `curvedText` / `text` as a *required*
                        // nullable with no default at all, so the argument is never optional here, and both
                        // take `content` as a required `@Tunable () -> Unit` (:172, :195) — the plain
                        // dialog has no artwork to default to, unlike the success and failure siblings
                        // below, which do. The dismiss is on the timer, so `durationMillis` below 4000 is
                        // what a caller can actually feel. `style` is read here, in this `@Tunable` body,
                        // and passed into the slot as a value — the slot itself is plain, so it cannot host
                        // the read; the note above the Content block has the mechanism.
                        if (overlayForm.value == 6) {
                            val curvedLabelStyle = ConfirmationDialogDefaults.curvedTextStyle()
                            ConfirmationDialog(
                                onDismissRequest = { overlayForm.value = -1 },
                                curvedText = {
                                    confirmationDialogCurvedText(
                                        text = "Saved",
                                        style = curvedLabelStyle,
                                    )
                                },
                                durationMillis = 2500L,
                                content = {
                                    FixedSizeIcon(
                                        image = swatch,
                                        contentDescription = "confirmation icon",
                                        iconSize = ConfirmationDialogDefaults.IconSize,
                                    )
                                },
                            )
                        }
                        if (overlayForm.value == 7) {
                            ConfirmationDialog(
                                onDismissRequest = { overlayForm.value = -1 },
                                text = {
                                    Text(
                                        "Linear text, up to " +
                                            ConfirmationDialogDefaults.LinearContentMaxLines +
                                            " lines",
                                    )
                                },
                                modifier = Modifier.padding(8.dp),
                                colors = ConfirmationDialogDefaults.colors(
                                    iconColor = Color(0xFF8AB4F8),
                                ),
                                content = {
                                    FixedSizeIcon(
                                        image = swatch,
                                        contentDescription = "confirmation icon",
                                        iconSize = ConfirmationDialogDefaults.SmallIconSize,
                                    )
                                },
                            )
                        }
                        // The success and failure dialogs. Both hand their colours to the body, so `colors`
                        // stays null and the variant's own `successColors` / `failureColors` resolution runs
                        // (ConfirmationDialog.kt:332, :418) — that is the part of the pair a caller cannot
                        // see from the signature. `content` is not required on these two: it carries
                        // upstream's parameter default verbatim, `SuccessIcon()` at :330 and
                        // `ConnectionFailureIcon()` at :416. So the success call below omits it, which is
                        // the default-artwork path, and the failure call passes `GenericFailureIcon`
                        // explicitly over it, which is the replacement path. `FailureIcon`, called in the
                        // row further up, is upstream's deprecated alias of `ConnectionFailureIcon` and is
                        // called under `@Suppress("DEPRECATION")` — no live site in the module uses it, so
                        // this row is its only external caller.
                        if (overlayForm.value == 8) {
                            val curvedLabelStyle = ConfirmationDialogDefaults.curvedTextStyle()
                            SuccessConfirmationDialog(
                                onDismissRequest = { overlayForm.value = -1 },
                                curvedText = {
                                    confirmationDialogCurvedText(
                                        text = "Sent",
                                        style = curvedLabelStyle,
                                    )
                                },
                            )
                        }
                        if (overlayForm.value == 9) {
                            val curvedLabelStyle = ConfirmationDialogDefaults.curvedTextStyle()
                            FailureConfirmationDialog(
                                onDismissRequest = { overlayForm.value = -1 },
                                curvedText = {
                                    confirmationDialogCurvedText(
                                        text = "Failed",
                                        style = curvedLabelStyle,
                                    )
                                },
                                durationMillis = 2500L,
                                content = {
                                    FixedSizeIcon(
                                        image = swatch,
                                        contentDescription = "failure icon",
                                        iconSize = ConfirmationDialogDefaults.IconSize,
                                    )
                                },
                            )
                        }
                        // OpenOnPhoneDialog (OpenOnPhoneDialog.kt:128): the one dialog of this family that
                        // *does* default `curvedText` to null, and `content` to null too — where upstream
                        // defaults it to its own icon. That is not cosmetic here: a null `content` draws no
                        // icon at all, so this call passes `OpenOnPhoneDialogDefaults.Icon` explicitly,
                        // exactly as that file's KDoc instructs (:120-125, :137-145).
                        if (overlayForm.value == 10) {
                            val openOnPhoneLabel = OpenOnPhoneDialogDefaults.text
                            val curvedLabelStyle = OpenOnPhoneDialogDefaults.curvedTextStyle()
                            OpenOnPhoneDialog(
                                onDismissRequest = { overlayForm.value = -1 },
                                curvedText = {
                                    openOnPhoneDialogCurvedText(
                                        text = openOnPhoneLabel,
                                        style = curvedLabelStyle,
                                    )
                                },
                                durationMillis = 2500L,
                                content = { OpenOnPhoneDialogDefaults.Icon() },
                            )
                        }
                        // The three curved containers, as roots. They are full-dial overlays because a
                        // caller cannot size them down: `CurvedLayout`'s own KDoc says a layout attribute
                        // on the call never reaches the host's `LayoutParams` (CurvedLayout.kt:70-78), so
                        // `matchParentSize()` is the only modifier that means anything, which is also what
                        // the `CurvedRow` at the top of this screen uses. Each is switched to the bottom
                        // anchor (90 degrees is 6 o'clock, :80-82) with the non-default alignment and
                        // direction parameters named, and each fills the scope with the container members
                        // that only exist on `CurvedLayoutScope` — `curvedRow`, `curvedBox`,
                        // `curvedColumn`, `curvedComposable`, `curvedText`.
                        if (overlayForm.value == 11) {
                            CurvedLayout(
                                modifier = Modifier.matchParentSize(),
                                anchor = 90f,
                                anchorType = CurvedAnchor.Start,
                                radialAlignment = CurvedAlignment.Radial.Inner,
                                angularDirection = CurvedDirection.Angular.Reversed,
                            ) {
                                curvedText(text = "CurvedLayout", maxSweepAngle = 90f)
                                curvedRow(radialAlignment = CurvedAlignment.Radial.Outer) {
                                    curvedText(text = " inner row", maxSweepAngle = 40f)
                                }
                                curvedComposable(rotationLocked = true) {
                                    Text("locked upright", fontSize = 12.sp)
                                }
                            }
                        }
                        if (overlayForm.value == 12) {
                            CurvedBox(
                                modifier = Modifier.matchParentSize(),
                                anchor = 90f,
                                anchorType = CurvedAnchor.Center,
                                radialAlignment = CurvedAlignment.Radial.Center,
                                angularAlignment = CurvedAlignment.Angular.Start,
                                angularDirection = CurvedDirection.Angular.CounterClockwise,
                            ) {
                                curvedText(text = "CurvedBox", maxSweepAngle = 120f)
                                curvedText(text = " on top", maxSweepAngle = 60f)
                            }
                        }
                        if (overlayForm.value == 13) {
                            CurvedColumn(
                                modifier = Modifier.matchParentSize(),
                                anchor = 90f,
                                anchorType = CurvedAnchor.End,
                                radialDirection = CurvedDirection.Radial.InsideOut,
                                angularAlignment = CurvedAlignment.Angular.End,
                            ) {
                                curvedText(text = "Curved", maxSweepAngle = 80f)
                                curvedText(text = " column", maxSweepAngle = 80f)
                                curvedBox(angularAlignment = CurvedAlignment.Angular.Center) {
                                    curvedText(text = " and a box", maxSweepAngle = 40f)
                                }
                            }
                        }
                        // None of the three above dismisses itself, so the way out is the one thing a
                        // caller can still place over them: a later sibling of the same root.
                        if (overlayForm.value >= 11) {
                            CompactButton(
                                onClick = { overlayForm.value = -1 },
                                modifier = Modifier.gravity(Gravity.BOTTOM),
                                label = { Text("Close") },
                            )
                        }
                    }
                }
            }
        )
    }
}

/**
 * A caller-owned host for the module's public `Canvas.drawCircularProgressIndicator`.
 *
 * Upstream keeps that entry point (`DrawScope.drawCircularProgressIndicator`,
 * `CircularProgressIndicator.kt:262-322`) for a caller that drives its own drawing, and nothing inside
 * `hibari-wear` calls it — `WearCircularProgressView.onDraw` (`:165-226`) runs the same maths through
 * the `internal` `drawIndicatorSegment` instead. This view is therefore the first caller the entry
 * point has, which is what puts it on a screen: an entry no one calls is an entry whose signature can
 * rot without a build noticing.
 *
 * The [Paint] is the caller's and is reused across frames — a draw routine must not allocate one per
 * call — and it carries `STROKE` with round caps, the same setup `WearCircularProgressView` gives its
 * own arc paint (`:54-57`). The entry point only ever writes `color` and `strokeWidth` on it, so this
 * is not something the draw call can be relied on to fix up.
 */
internal class SmokeProgressRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    /**
     * One frame's state, pushed in by the tune through the attribute on this node's modifier.
     * `spec.indeterminate` and `spec.flipHorizontal` are not read: the non-animating entry point has no
     * indeterminate mode and no direction handling — upstream's `DrawScope` version has neither, and the
     * circular indicator is not direction dependent (`ProgressIndicator.kt:115`, `:154`).
     */
    var spec: ProgressSpec? = null
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        val s = spec ?: return
        canvas.drawCircularProgressIndicator(
            progress = s.progress,
            colors = s.colors,
            strokeWidth = s.strokeWidth,
            paint = paint,
            density = resources.displayMetrics.density,
            enabled = s.enabled,
            // From the spec rather than left to the parameter default, so this view honours the same
            // field every other indicator in the module honours.
            allowProgressOverflow = s.allowProgressOverflow,
            startAngle = s.startAngle,
            endAngle = s.endAngle,
            gapSize = s.gapSize,
        )
    }
}
