package com.huanli233.hibari.runtime

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Xml
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewGroup.getChildMeasureSpec
import androidx.annotation.Px
import androidx.annotation.XmlRes
import androidx.core.view.ViewCompat
import com.highcapable.yukireflection.factory.buildOf
import com.highcapable.yukireflection.factory.constructor
import com.highcapable.yukireflection.factory.current
import com.highcapable.yukireflection.factory.notExtends
import com.highcapable.yukireflection.type.android.AttributeSetClass
import com.highcapable.yukireflection.type.android.ContextClass
import com.highcapable.yukireflection.type.android.ViewGroup_LayoutParamsClass
import com.highcapable.yukireflection.type.java.IntType
import com.huanli233.hibari.runtime.bypass.XmlBlockBypass
import com.huanli233.hibari.runtime.attribute.AttributeSetResolver
import com.huanli233.hibari.runtime.attribute.RuntimeAttrsAttribute
import com.huanli233.hibari.ui.Attribute
import com.huanli233.hibari.ui.AttrsAttribute
import com.huanli233.hibari.ui.HibariFactory
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.RefModifier
import com.huanli233.hibari.ui.ViewClassAttribute
import com.huanli233.hibari.ui.ViewCreatingParams
import com.huanli233.hibari.ui.flattenToList
import com.huanli233.hibari.ui.layout.ParentDataModifier
import com.huanli233.hibari.ui.node.Measurable
import com.huanli233.hibari.ui.node.MeasurePolicy
import com.huanli233.hibari.ui.node.Node
import com.huanli233.hibari.ui.node.Placeable
import com.huanli233.hibari.ui.unit.Constraints
import org.xmlpull.v1.XmlPullParser
import java.lang.reflect.Constructor
import java.util.UUID

class Renderer(
    val factories: List<HibariFactory>,
    val parent: ViewGroup
) {
    companion object {
        private val viewConstructors = mutableMapOf<String, ViewConstructor>()
        internal var attrSets = HashMap<Int, CachedAttrSet>()

        /**
         * An `AttributeSet` is parsed against a Context, so it carries that context's resources and
         * configuration. The cache used to be keyed by resource id alone, which meant the first
         * Activity to ask for a given `@XmlRes` pinned its context for the life of the process and
         * every later view of that id read the configuration it was built with.
         */
        internal class CachedAttrSet(val context: Context, val set: AttributeSet)

        val hibariNodeKey = R.id.hibari_node_key

        val viewIds = mutableMapOf<String, Int>()

        fun generateViewId(id: String?): Pair<String, Int> {
            // Only named ids can be resolved back through `String.viewId` / [findViewByHibariId],
            // so only those are worth registering: memoizing an anonymous id would add one entry per
            // rendered view to a static map that is never evicted from.
            if (id == null) return generateRandomViewId() to ViewCompat.generateViewId()
            synchronized(this) {
                viewIds[id]?.let { return id to it }
                return id to ViewCompat.generateViewId().also { viewIds[id] = it }
            }
        }

        fun generateRandomViewId() = "anonymous@${UUID.randomUUID()}"

        /**
         * `ParentClass$LayoutParams` and its `(int, int)` constructor used to be resolved for every
         * single view created. Both are stable per parent class — a miss included, which is why a
         * failed lookup is cached as `null` instead of throwing again for each sibling.
         */
        private val layoutParamsConstructors = HashMap<String, Constructor<*>?>()

        private fun layoutParamsConstructor(parentClass: Class<*>): Constructor<*>? {
            val name = parentClass.name
            if (layoutParamsConstructors.containsKey(name)) return layoutParamsConstructors[name]
            val constructor = try {
                Class.forName("${name}\$LayoutParams")
                    .constructor { param(IntType, IntType) }.ignored().give()
            } catch (_: ClassNotFoundException) {
                null
            }
            layoutParamsConstructors[name] = constructor
            return constructor
        }
    }

    fun render(node: Node, parent: ViewGroup): View {
        TuneStats.markViewCreated()
        if (node.measurePolicy != null) {
            return LayoutNodeHost(parent.context).apply {
                this.node = node
                setTag(hibariNodeKey, node.key)
            }
        }

        val modifierAttrs = node.flattened

        val viewClass = (modifierAttrs.firstOrNull { it is ViewClassAttribute } as? ViewClassAttribute)?.viewClass
            ?: hibariRuntimeError("The view class cannot be null.")
        val attrXml = (modifierAttrs.firstOrNull { it is AttrsAttribute } as? AttrsAttribute)?.attrs ?: -1
        val runtimeAttrs = modifierAttrs.firstOrNull { it is RuntimeAttrsAttribute } as? RuntimeAttrsAttribute
        val id = (modifierAttrs.firstOrNull { it is IdAttribute } as? IdAttribute)?.id
        val intId = (modifierAttrs.firstOrNull { it is IntIdAttribute } as? IntIdAttribute)?.id

        // The int below wins, so a chain that already carries one must not also be given a name:
        // `Modifier.constraint { }` on a child without an explicit `id` mints a fresh anonymous name
        // every tune, and naming it through [generateViewId] memoised each of those names - with an
        // int no one would have used - in a static map nothing ever leaves.
        val viewId = when {
            intId != null -> intId
            id != null -> generateViewId(id).second
            else -> ViewCompat.generateViewId()
        }

        // A runtime `Modifier.attrs { }` synthesizes an AttributeSet from name/value pairs (no
        // compiled resource); it takes precedence over the @XmlRes path and is released as soon as
        // the (Context, AttributeSet) constructor has consumed it.
        val resolver = runtimeAttrs?.let { AttributeSetResolver.from(parent.context) }
        val runtimeParser = resolver?.let { it.newParser(runtimeAttrs!!.items) }
        val attrs = runtimeParser ?: createAttributeSet(parent.context, attrXml)
        val hasAttrs = runtimeAttrs != null || attrXml != -1
        val view = createViewFromFactory(viewClass, parent.context, attrs) ?: getViewConstructor(viewClass, hasAttrs)?.build(parent.context, attrs)
        if (runtimeParser != null && resolver != null) {
            resolver.release(runtimeParser)
            resolver.close()
        }

        view?.id = viewId
        view?.let { view ->
            view.setTag(hibariViewId, id)

            view.layoutParams = layoutParamsConstructor(parent.javaClass)
                ?.newInstance(LayoutParamsWrapContent, LayoutParamsWrapContent) as? ViewGroup.LayoutParams
                ?: parent.current(ignored = true).method {
                    name = "generateDefaultLayoutParams"
                    emptyParam()
                    superClass()
                }.invoke() ?: ViewGroup.LayoutParams(
                    LayoutParamsWrapContent,
                    LayoutParamsWrapContent
                )

            view.setTag(hibariNodeKey, node.key)

            // Walked in place rather than through filtered intermediate lists: this ran per view
            // created, and both passes are cheap enough to fold into one traversal each.
            var attributesApplied = 0
            modifierAttrs.forEach { attribute ->
                (attribute as? Attribute<*>)?.let {
                    it.applyTo(view)
                    attributesApplied++
                }
            }
            TuneStats.addAttributeWrites(attributesApplied)
            modifierAttrs.forEach { if (it is RefModifier) it.block(view) }
        }

        return view ?: hibariRuntimeError("The view class ${viewClass.name} must have a constructor with two parameters of type Context and AttributeSet to apply attributes.")
    }

    fun applyAttributes(view: View, attrs: List<Attribute<*>>) {
        TuneStats.addAttributeWrites(attrs.size)
        attrs.forEach {
            it.applyTo(view)
        }
    }

    private fun <V : View> getViewConstructor(
        viewClass: Class<V>,
        hasAttr: Boolean = false
    ): ViewConstructor? {
        val cacheKey = "${viewClass.name}-${hasAttr}"
        return viewConstructors[cacheKey] ?: run {
            var parameterCount = 0
            val twoParams = viewClass.constructor {
                param(ContextClass, AttributeSetClass)
            }.ignored().give()
            val onceParam = viewClass.constructor {
                param(ContextClass)
            }.ignored().give()
            val constructor = if (hasAttr) {
                twoParams?.apply { parameterCount = 2 } ?: hibariRuntimeError("The view class ${viewClass.name} must have a constructor with two parameters of type Context and AttributeSet to apply attributes.")
            } else {
                onceParam?.apply { parameterCount = 1 }
                    ?: twoParams?.apply { parameterCount = 2 }
            }
            val viewConstructor = constructor?.let { ViewConstructor(it, parameterCount) }
            // The lookup key has to be the one used above, otherwise every entry written here is
            // unreachable and each view creation pays for two reflective constructor searches.
            if (viewConstructor != null) viewConstructors[cacheKey] = viewConstructor
            viewConstructor
        }
    }

    private fun <V : View> createViewFromFactory(viewClass: Class<V>, context: Context, attrs: AttributeSet): V? {
        var processed: V? = null
        factories.forEach { factory ->
            val params = ViewCreatingParams(viewClass as Class<*>, attrs)
            val view = factory(parent, processed, context, params)
            if (view != null && view.javaClass notExtends viewClass) hibariRuntimeError(
                "HikageFactory cannot cast the created view type \"${view.javaClass}\" to \"${viewClass.name}\", " +
                        "please confirm that the view type you created is correct."
            )
            @Suppress("UNCHECKED_CAST")
            if (view != null) processed = view as? V?
        }; return processed
    }

    internal fun createAttributeSet(context: Context, @XmlRes attrXml: Int): AttributeSet {
        // Only a set built against this very context is reusable: it carries the resources and
        // configuration of the context it came from.
        attrSets[attrXml]?.let { if (it.context === context) return it.set }

        val created = if (attrXml == -1) {
            XmlBlockBypass.newAttrSet(context)
        } else {
            runCatching {
                val parser = context.resources.getXml(attrXml)
                var type = parser.eventType
                while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
                    type = parser.next()
                }
                if (type != XmlPullParser.START_TAG) {
                    hibariRuntimeError("No start tag found for XML resource $attrXml")
                }
                Xml.asAttributeSet(parser)
            }.getOrElse { hibariRuntimeError("Failed to create attribute set", it) }
        }
        attrSets[attrXml] = CachedAttrSet(context, created)
        return created
    }

    /**
     * Not an `inner` class: instances are cached in the static [viewConstructors] table, so an outer
     * reference here would pin this renderer - and with it its `parent` view group, the whole tree
     * hanging off that group and the context behind it - for the life of the process, one tree per
     * distinct view class the app ever builds.
     */
    private class ViewConstructor(
        private val instance: Constructor<*>,
        private val parameterCount: Int
    ) {

        @Suppress("UNCHECKED_CAST")
        fun <V : View> build(
            context: Context,
            attrs: AttributeSet
        ) = when (parameterCount) {
            2 -> instance.newInstance(context, attrs)
            1 -> instance.newInstance(context)
            else -> null
        } as? V?
    }

}

const val LayoutParamsMatchParent = ViewGroup.LayoutParams.MATCH_PARENT

const val LayoutParamsWrapContent = ViewGroup.LayoutParams.WRAP_CONTENT

class LayoutParams private constructor(
    private val lpClass: Class<ViewGroup.LayoutParams>,
    private val parent: ViewGroup?
) {

    private class BodyBuilder(
        val width: Int,
        val height: Int,
        val matchParent: Boolean,
        val widthMatchParent: Boolean,
        val heightMatchParent: Boolean,
    )

    private class WrapperBuilder(
        val delegate: LayoutParams?,
        val lparams: ViewGroup.LayoutParams?
    )

    private var bodyBuilder: BodyBuilder? = null

    private var wrapperBuilder: WrapperBuilder? = null

    @PublishedApi
    internal companion object {

        @Suppress("UNCHECKED_CAST")
        fun <LP : ViewGroup.LayoutParams> from(
            lpClass: Class<LP>,
            parent: ViewGroup?,
            width: Int,
            height: Int,
            matchParent: Boolean,
            widthMatchParent: Boolean,
            heightMatchParent: Boolean,
        ) = LayoutParams(lpClass as Class<ViewGroup.LayoutParams>, parent).apply {
            bodyBuilder = BodyBuilder(
                width, height, matchParent, widthMatchParent, heightMatchParent
            )
        }

        @Suppress("UNCHECKED_CAST")
        fun <LP : ViewGroup.LayoutParams> from(
            lpClass: Class<LP>,
            parent: ViewGroup?,
            delegate: LayoutParams?,
            lparams: ViewGroup.LayoutParams? = null
        ) = LayoutParams(lpClass as Class<ViewGroup.LayoutParams>, parent).apply {
            wrapperBuilder = WrapperBuilder(delegate, lparams)
        }
    }

    private fun createDefaultLayoutParams(lparams: ViewGroup.LayoutParams? = null): ViewGroup.LayoutParams {
        if (lparams != null && lpClass.isInstance(lparams)) return lparams
        val wrapped = lparams?.let {
            parent?.current(ignored = true)?.method {
                name = "generateLayoutParams"
                param(ViewGroup_LayoutParamsClass)
                superClass()
            }?.invoke<ViewGroup.LayoutParams?>(it)
        }
        return wrapped
            ?: lpClass.buildOf<ViewGroup.LayoutParams>(LayoutParamsWrapContent, LayoutParamsWrapContent) {
                param(IntType, IntType)
            } ?: hibariRuntimeError("Create default layout params failed.")
    }

    fun create(): ViewGroup.LayoutParams {
        if (bodyBuilder == null && wrapperBuilder == null) hibariRuntimeError("No layout params builder found.")
        return bodyBuilder?.let {
            val lparams = ViewLayoutParams(lpClass, it.width, it.height, it.matchParent, it.widthMatchParent, it.heightMatchParent)
            lparams
        } ?: wrapperBuilder?.let {
            val lparams = it.delegate?.create() ?: it.lparams
            createDefaultLayoutParams(lparams)
        } ?: hibariRuntimeError("Internal error of build layout params.")
    }
}

private const val LayoutParamsUnspecified = LayoutParamsWrapContent - 1

@JvmOverloads
fun <VGLP : ViewGroup.LayoutParams> ViewLayoutParams(
    lpClass: Class<VGLP>,
    @Px width: Int = LayoutParamsUnspecified,
    @Px height: Int = LayoutParamsUnspecified,
    matchParent: Boolean = false,
    widthMatchParent: Boolean = false,
    heightMatchParent: Boolean = false
): VGLP {
    val absWidth = when {
        width != LayoutParamsUnspecified -> width
        matchParent || widthMatchParent -> LayoutParamsMatchParent
        else -> LayoutParamsWrapContent
    }
    val absHeight = when {
        height != LayoutParamsUnspecified -> height
        matchParent || heightMatchParent -> LayoutParamsMatchParent
        else -> LayoutParamsWrapContent
    }
    return lpClass.buildOf<VGLP>(absWidth, absHeight) {
        param(IntType, IntType)
    } ?: error(
        "Create ViewGroup.LayoutParams failed. " +
                "Could not found the default constructor LayoutParams(width, height) in $lpClass."
    )
}

fun Constraints.Companion.fromMeasureSpec(widthMeasureSpec: Int, heightMeasureSpec: Int): Constraints {
    val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
    val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
    val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
    val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

    val minWidth = if (widthMode == View.MeasureSpec.EXACTLY) widthSize else 0
    val maxWidth = if (widthMode == View.MeasureSpec.UNSPECIFIED) Constraints.Infinity else widthSize
    val minHeight = if (heightMode == View.MeasureSpec.EXACTLY) heightSize else 0
    val maxHeight = if (heightMode == View.MeasureSpec.UNSPECIFIED) Constraints.Infinity else heightSize

    return Constraints(minWidth, maxWidth, minHeight, maxHeight)
}

interface LayoutModifierNode : Modifier.Element {
    fun measure(measurable: Measurable, constraints: Constraints): Placeable
}

internal class LayoutNodeHost(context: Context) : ViewGroup(context) {
    var node: Node? = null
        set(value) {
            field = value
            measurePolicy = value?.measurePolicy
            requestLayout()
        }
    private var measurePolicy: MeasurePolicy? = null
    private var rootPlaceable: Placeable? = null
    private val childMeasurables = mutableListOf<ViewMeasurable>()

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val policy = measurePolicy
        if (policy == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        if (childMeasurables.size != childCount || measurablesOutOfSync()) {
            childMeasurables.clear()
            (0 until childCount).forEach { i ->
                val childView = getChildAt(i)
                val childNode = node?.children?.getOrNull(i)
                childMeasurables.add(ViewMeasurable(childView, childNode))
            }
        }

        childMeasurables.forEach { it.invalidateMeasureCache() }

        val constraints = Constraints.fromMeasureSpec(widthMeasureSpec, heightMeasureSpec)
        rootPlaceable = policy.measure(childMeasurables, constraints)
        setMeasuredDimension(rootPlaceable!!.width, rootPlaceable!!.height)
    }

    /**
     * The patcher replaces a child in place when a node's view class changed, which keeps
     * [childCount] untouched — so the child count alone cannot say whether the cached measurables
     * still wrap the live children. Without this check the host goes on measuring the detached old
     * view and lays out nothing in its place.
     */
    private fun measurablesOutOfSync(): Boolean {
        val children = node?.children
        if (children?.size != childCount) return true
        return (0 until childCount).any { i ->
            childMeasurables[i].view !== getChildAt(i) || childMeasurables[i].node !== children?.getOrNull(i)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        rootPlaceable?.placeAt(0, 0)
    }

    /**
     * The children of a measure policy host are ordinary views, and a modifier like margin writes
     * through `MarginLayoutParams`. Handing out bare `ViewGroup.LayoutParams` made
     * `updateLayoutParams` throw a ClassCastException on the first such child, and left every child
     * creation here paying a failed `Class.forName` before falling back to this method reflectively.
     */
    class LayoutParams : MarginLayoutParams {
        constructor() : super(WRAP_CONTENT, WRAP_CONTENT)
        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.LayoutParams) : super(source)
        constructor(c: Context, attrs: AttributeSet) : super(c, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams()
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams {
        return LayoutParams(p)
    }

    override fun checkLayoutParams(lp: ViewGroup.LayoutParams?): Boolean {
        return lp is LayoutParams
    }
}

open class ViewMeasurable(
    val view: View,
    val node: Node?
) : Measurable {

    /**
     * The last constraints this measurable was asked for and the placeable built for them. A measure
     * pass asks for the same child twice (wrap first, then the space left), and a fresh measurable
     * comes with every patched subtree, so this used to be one [LinkedHashMap] per child per round:
     * two objects each, cleared again at the start of every pass.
     *
     * A second question with different constraints re-measures instead of finding an older entry,
     * which costs nothing on this path - [BasePlaceable] hands the work to [View.measure], and the
     * view keeps its own measured-with cache - and it still cannot answer with a stale size, because
     * the whole slot is dropped at the start of every pass.
     */
    private var cachedConstraints: Constraints? = null
    private var cachedPlaceable: Placeable? = null

    private val chainedMeasure: (Constraints) -> Placeable

    override val context: Context
        get() = view.context

    override val parentData: Any? by lazy {
        node?.flattened
            ?.filterIsInstance<ParentDataModifier>()
            ?.fold(null as Any?) { currentData, modifier ->
                modifier.modifyParentData(currentData)
            }
    }

    init {
        val layoutModifiers = node?.flattened?.filterIsInstance<LayoutModifierNode>() ?: emptyList()

        val baseMeasure: (Constraints) -> Placeable = { c ->
            BasePlaceable(view, c)
        }
        chainedMeasure = layoutModifiers.foldRight(baseMeasure) { modifier, next ->
            { c ->
                val measurableProxy = object : Measurable {
                    override val parentData: Any? = this@ViewMeasurable.parentData
                    override val context: Context = this@ViewMeasurable.context
                    override fun measure(constraints: Constraints): Placeable {
                        return next(constraints)
                    }
                }
                modifier.measure(measurableProxy, c)
            }
        }
    }

    override fun measure(constraints: Constraints): Placeable {
        cachedPlaceable?.let { if (cachedConstraints == constraints) return it }
        return chainedMeasure(constraints).also {
            cachedConstraints = constraints
            cachedPlaceable = it
        }
    }

    fun invalidateMeasureCache() {
        cachedConstraints = null
        cachedPlaceable = null
    }
}

private class BasePlaceable(
    private val view: View,
    constraints: Constraints
) : Placeable() {
    init {
        if (view is LayoutNodeHost) {
            view.measure(
                constraints.toWidthMeasureSpec(),
                constraints.toHeightMeasureSpec()
            )
        } else {
            val lp = view.layoutParams
            val childWidthMeasureSpec = getChildMeasureSpec(
                constraints.toWidthMeasureSpec(),
                view.paddingLeft + view.paddingRight,
                lp.width
            )
            val childHeightMeasureSpec = getChildMeasureSpec(
                constraints.toHeightMeasureSpec(),
                view.paddingTop + view.paddingBottom,
                lp.height
            )
            view.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        }
        this.width = view.measuredWidth
        this.height = view.measuredHeight
    }

    override fun placeAt(x: Int, y: Int) {
        view.layout(x, y, x + width, y + height)
    }
}