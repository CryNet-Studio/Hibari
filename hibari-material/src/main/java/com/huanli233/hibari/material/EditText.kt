package com.huanli233.hibari.material

import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.widget.doOnTextChanged
import com.huanli233.hibari.foundation.Node
import com.huanli233.hibari.runtime.Tunable
import com.huanli233.hibari.runtime.invokeSetKeyedTag
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.thenViewAttribute
import com.huanli233.hibari.ui.thenViewAttributeIfNotNull
import com.huanli233.hibari.ui.uniqueKey
import com.huanli233.hibari.ui.viewClass

// Keyed-tag slot holding the single TextWatcher this component installed, so re-applying the
// listener attribute replaces it instead of stacking another one (see EditText() below).
private val editTextWatcherKey = ViewCompat.generateViewId()

@Tunable
fun EditText(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: CharSequence? = null,
    inputType: Int? = null,
    maxLines: Int? = null,
) {
    Node(
        modifier = modifier
            .viewClass(AppCompatEditText::class.java)
            .thenViewAttribute<AppCompatEditText, String>(uniqueKey, value) {
                if (this.text.toString() != it) {
                    setText(it)
                    setSelection(it.length)
                }
            }
            .thenViewAttribute<AppCompatEditText, (String) -> Unit>(uniqueKey, onValueChange) { listener ->
                val currentValue = value
                // The listener lambda is a fresh instance every recomposition, so this attribute is
                // re-applied on every tune. Without removing the previous watcher, doOnTextChanged
                // (which adds) would stack one watcher per reconfigure -> N callbacks per keystroke
                // and a leak. Swap it out first.
                (getTag(editTextWatcherKey) as? TextWatcher)?.let { removeTextChangedListener(it) }
                val watcher = doOnTextChanged { text, _, _, _ ->
                    val newText = text.toString()
                    if (newText != currentValue) {
                        listener(newText)
                    }
                }
                invokeSetKeyedTag(this, editTextWatcherKey, watcher)
            }
            .thenViewAttributeIfNotNull<AppCompatEditText, CharSequence>(uniqueKey, hint) { this.hint = it }
            .thenViewAttributeIfNotNull<AppCompatEditText, Int>(uniqueKey, inputType) { this.inputType = it }
            .thenViewAttributeIfNotNull<AppCompatEditText, Int>(uniqueKey, maxLines) { this.maxLines = it }
    )
}