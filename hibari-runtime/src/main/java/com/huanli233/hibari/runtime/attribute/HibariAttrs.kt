package com.huanli233.hibari.runtime.attribute

import com.huanli233.hibari.runtime.attribute.entity.AttributeItem
import com.huanli233.hibari.ui.Modifier

/**
 * Collects runtime XML attributes for [Modifier.attrs]. Mirrors Hikage's `AttributeScope`: values
 * given as strings are parsed into typed resource values (references `@pkg:type/name`, theme
 * references `?attr/name`, colors, dimensions, enum/flag symbols, ...); the Int/Boolean/Float
 * overloads pass a raw typed value.
 *
 * [name] may carry a namespace prefix (`"android:text"`); a bare name uses [defaultNamespace]
 * (framework `android` unless overridden).
 */
class AttrsScope internal constructor(private var defaultNamespace: String = ANDROID) {

    private val items = mutableListOf<AttributeItem>()

    internal fun build(): List<AttributeItem> = items.toList()

    /** Set the namespace used for subsequent bare (unprefixed) [set] calls. */
    fun namespace(value: String) {
        defaultNamespace = value
    }

    fun set(name: String, value: String) {
        items += AttributeItem.from(name, AttributeItem.Value.Str(value), defaultNamespace)
    }

    fun set(name: String, value: Int) {
        items += AttributeItem.from(name, AttributeItem.Value.Raw(value), defaultNamespace)
    }

    fun set(name: String, value: Boolean) {
        items += AttributeItem.from(name, AttributeItem.Value.Bool(value), defaultNamespace)
    }

    fun set(name: String, value: Float) {
        items += AttributeItem.from(name, AttributeItem.Value.Real(value), defaultNamespace)
    }

    internal companion object {
        const val ANDROID = "android"
        const val APP = "app"
    }
}

/**
 * Runtime XML attributes to feed the View's `(Context, AttributeSet)` constructor. A pure carrier
 * like [com.huanli233.hibari.ui.AttrsAttribute]; the Renderer synthesizes an AttributeSet from
 * [items] at construction time.
 */
data class RuntimeAttrsAttribute(val items: List<AttributeItem>) : Modifier.Element

/**
 * Build a View with the given runtime XML attributes, applied as if they came from a layout file.
 *
 * This is the runtime-attribute escape hatch for attributes that have no dedicated `Modifier.*`
 * function yet (custom View attrs, framework attrs not yet wrapped). For an existing compiled XML
 * resource, prefer [com.huanli233.hibari.ui.attrs].
 */
fun Modifier.attrs(content: AttrsScope.() -> Unit): Modifier =
    this.then(RuntimeAttrsAttribute(AttrsScope().apply(content).build()))
